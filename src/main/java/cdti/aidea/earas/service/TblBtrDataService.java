package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.Response.TblBtrDataDTO;
import cdti.aidea.earas.contract.ValidationErrorResponse;
import cdti.aidea.earas.model.Btr_models.*;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import cdti.aidea.earas.repository.Btr_repo.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// import org.apache.poi.sl.draw.geom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TblBtrDataService {

    private final TblBtrDataRepository tblBtrDataRepository;
    private final KeyPlotsRepository keyPlotsRepository;
    private final ClusterMasterRepository clusterMasterRepository;
    private final ClusterFormDataRepository clusterFormDataRepository;
    private final TblMasterZoneRepository tblMasterZoneRepository;
    private final TblNonBtrRepository tblNonBtrRepository;

    // ---------------- Single Save ----------------
    // ---------------- Single Save ----------------
    @Transactional
    public Map<String, Object> saveData(TblBtrDataDTO dto) {
        // ✅ Validate required fields
        List<String> requiredErrors = validateRequiredFields(dto);
        if (!requiredErrors.isEmpty()) {
            throw new RuntimeException("Validation failed: " + String.join(", ", requiredErrors));
        }

        // ✅ Validate duplicates
        ValidationErrorResponse duplicateError = validateDuplicate(dto);
        if (duplicateError != null) {
            throw new RuntimeException("Duplicate entry detected: " + duplicateError.getMessage());
        }

        // 1️⃣ Save TblBtrData
        TblBtrData btrData = tblBtrDataRepository.save(mapToEntity(dto));

        // 2️⃣ Fetch zone by UUID
        Integer zoneUuid = dto.getZoneId();
        TblMasterZone zone =
                tblMasterZoneRepository
                        .findById(zoneUuid)
                        .orElseThrow(() -> new RuntimeException("Zone not found"));

        // 3️⃣ Save KeyPlots
        KeyPlots keyPlot = new KeyPlots();
        keyPlot.setBtrData(btrData);
        keyPlot.setZone(zone);
        keyPlot.setIntervals(1);
        keyPlot.setAgriStartYear(LocalDate.now());
        keyPlot.setAgriEndYear(LocalDate.now().plusYears(1));
        keyPlot.setIsRejected(false);
        keyPlot.setStatus(true);
        keyPlot.setLandType(btrData.getLtype());
        keyPlot.setCreated_by(UUID.randomUUID());
        keyPlot = keyPlotsRepository.save(keyPlot);

        String lbcode = btrData.getLbcode();
        String landType = btrData.getLtype(); // "Wet" or "Dry"

        int agriYear = LocalDate.now().getYear();

// In Kerala or India, agri year may start in June — adjust accordingly
        LocalDate startDate = LocalDate.of(agriYear, 6, 1);
        LocalDate endDate = startDate.plusYears(1).minusDays(1); // May 31 of next year

        Optional<Integer> maxClusterNumberOpt = clusterMasterRepository
                .findMaxClusterNumberByLbcodeAndLandTypeAndDateRange(lbcode, landType, startDate, endDate);

        int nextClusterNumber = maxClusterNumberOpt.orElse(0) + 1;


        // 4️⃣ Save ClusterMaster
        ClusterMaster clusterMaster = new ClusterMaster();
        clusterMaster.setKeyPlot(keyPlot);
        clusterMaster.setClusterNumber(nextClusterNumber);
        clusterMaster.setStatus("Not Started");
        clusterMaster.setIsReject(false);
        clusterMaster.setIs_active(true);
        clusterMaster.setZone(zone);
        clusterMasterRepository.save(clusterMaster);

        // 5️⃣ Save ClusterFormData
        ClusterFormData clusterFormData = new ClusterFormData();
        clusterFormData.setClusterMaster(clusterMaster);
        clusterFormData.setPlot(btrData);
        clusterFormData.setPlotLabel("K");
        clusterFormData.setEnumeratedArea(btrData.getTotCent());
        clusterFormData.setCreatedBy(UUID.randomUUID());
        clusterFormData.setStatus(true);
        clusterFormDataRepository.save(clusterFormData);

        // ✅ Return saved entity id
        Map<String, Object> response = new HashMap<>();
        response.put("id", btrData.getId());
        return response;
    }

    // ---------------- Save All ----------------
    @Transactional
    public Map<String, Object> saveAllData(List<TblBtrDataDTO> dtoList) {
        List<ValidationErrorResponse> allErrors = new ArrayList<>();

        for (TblBtrDataDTO dto : dtoList) {
            // Required validation
            List<String> requiredErrors = validateRequiredFields(dto);
            if (!requiredErrors.isEmpty()) {
                allErrors.add(
                        new ValidationErrorResponse(
                                dto.getResvno(), dto.getResbdno(), dto.getTotCent(),String.join(", ", requiredErrors)));
            }

            // Duplicate validation
            ValidationErrorResponse duplicateError = validateDuplicate(dto);
            if (duplicateError != null) {
                allErrors.add(duplicateError);
            }
        }


        if (!allErrors.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "Validation Failed");
            response.put("errors", allErrors);
            return response;
        }

        // ✅ Save all and collect IDs
        List<Long> savedIds =
                dtoList.stream().map(dto -> (Long) saveData(dto).get("id")).collect(Collectors.toList());

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("status", "Success");
        successResponse.put("message", "All records saved successfully");
        successResponse.put("ids", savedIds);
        return successResponse;
    }

    // ---------------- DTO -> Entity Mapper ----------------
    private TblBtrData mapToEntity(TblBtrDataDTO dto) {
        TblBtrData entity = new TblBtrData();
        entity.setDcode(dto.getDcode());
        entity.setTcode(dto.getTcode());
        entity.setVcode(dto.getVcode());
        entity.setBcode(dto.getBcode());
        entity.setLbcode(dto.getLbcode());
        entity.setLtype(dto.getLtype());
        entity.setLsgcode(dto.getLsgcode());
        entity.setTotCent(dto.getTotCent());
        entity.setResvno(dto.getResvno());
        entity.setResbdno(dto.getResbdno());
// 🧩 Determine type-based mapping
        if (dto.getBtrtype() != null) {
            long typeId = dto.getBtrtype();
            Optional<TblNonBtr> nonBtr = tblNonBtrRepository.findById(dto.getBtrtype());
            // Type 1 → dcode to resbdno
            if (typeId == 1) {
                entity.setBtrtype(nonBtr.get());
            }
            // Type 2 → dcode to totcent + ownername, address, houseno
            else if (typeId == 2) {
                entity.setOwnername(dto.getOwnername());
                entity.setAddress(dto.getAddress());
                entity.setHouseno(dto.getHouseno());
                entity.setBtrtype(nonBtr.get());
            }
            // Type 3 → dcode to totcent + ownername, address
            // but not resvno/resbdno
            else if (typeId == 3) {
                entity.setOwnername(dto.getOwnername());
                entity.setAddress(dto.getAddress());
                entity.setBtrtype(nonBtr.get());
            }
            // Type 4 → dcode to totcent + ownername, address, tpno, tpsubdno
            // (mapped to mainno and subno)
            else if (typeId == 4) {
                entity.setOwnername(dto.getOwnername());
                entity.setAddress(dto.getAddress());
                entity.setTpno(dto.getTpno());
                entity.setTbsubdivisionno(dto.getTbsubdivisionno());
                entity.setBtrtype(nonBtr.get());
            }
            // Type 5 → dcode to totcent + ownername, address, mainno, subno
            // but not resvno/resbdno
            else if (typeId == 5) {
                entity.setOwnername(dto.getOwnername());
                entity.setMainno(dto.getMainno());
                entity.setSubno(dto.getSubno());
                entity.setBtrtype(nonBtr.get());
            }
        }
        // ✅ NEW BLOCK — add this
        LocalDate now = LocalDate.now();
        entity.setInsertionTime(LocalDateTime.now());
        entity.setUpdationTime(LocalDateTime.now());

        // Agreement year logic
        LocalDate agreStart = LocalDate.of(now.getYear(), 7, 1); // July 1 of current year
        LocalDate agreEnd = LocalDate.of(now.getYear() + 1, 6, 30); // June 30 of next year
        entity.setAgreStartYear(agreStart);
        entity.setAgreEndYear(agreEnd);
        // ✅ END NEW BLOCK

        return entity;
    }


    // ---------------- Duplicate Validation ----------------
    private ValidationErrorResponse validateDuplicate(TblBtrDataDTO dto) {
        boolean exists =
                tblBtrDataRepository.existsByDcodeAndTcodeAndVcodeAndBcodeAndResvnoAndResbdno(
                        dto.getDcode(),
                        dto.getTcode(),
                        dto.getVcode(),
                        dto.getBcode(),
                        dto.getResvno(),
                        dto.getResbdno());

        if (exists) {
            return new ValidationErrorResponse(
                    dto.getResvno(),
                    dto.getResbdno(),
                    dto.getTotCent(),
                    "Duplicate entry already exists for resvno="
                            + dto.getResvno()
                            + " and resbdno="
                            + dto.getResbdno());
        }
        return null;
    }

    // ---------------- Required Fields Validation ----------------
    private List<String> validateRequiredFields(TblBtrDataDTO dto) {
        List<String> errors = new ArrayList<>();

        if (dto.getDcode() == null) errors.add("District code (dcode) is required.");
        if (dto.getTcode() == null) errors.add("Taluk code (tcode) is required.");
        if (dto.getVcode() == null) errors.add("Village code (vcode) is required.");
        if (dto.getBcode() == null || dto.getBcode().trim().isEmpty())
            errors.add("Block code (bcode) is required.");
        if (dto.getBtrtype() == 1) {
            if (dto.getResvno() == null) errors.add("Reservation number (resvno) is required.");
        }
        if (dto.getZoneId() == null) errors.add("Zone Id (zoneId) is required.");

        return errors;
    }


    public ValidationErrorResponse validateDuplicateForCluster(TblBtrDataDTO dto) {

        String cleanedResbdno = dto.getResbdno() != null
                ? dto.getResbdno().trim().replaceFirst("^0+(?!$)", "")
                : null;

        Optional<TblBtrData> exists = tblBtrDataRepository.findByDcodeAndTcodeAndVcodeAndBcodeAndResvnoAndResbdno(
                dto.getDcode(),
                dto.getTcode(),
                dto.getVcode(),
                dto.getBcode(),
                dto.getResvno(),
                cleanedResbdno
        );


        System.out.println("dto >..  "+dto);
        if (exists.isPresent()) {
            return new ValidationErrorResponse(
                    dto.getResvno(),
                    dto.getResbdno(),
                    exists.get().getTotCent(),

                    "Duplicate entry already exists for resvno=" + dto.getResvno() +
                            " and resbdno=" + dto.getResbdno());
        }

        return null; // or Optional<ValidationErrorResponse>
    }
}