package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.Response.TblBtrDataDTO;
import cdti.aidea.earas.contract.Response.TblBtrDetailsResponse;
import cdti.aidea.earas.contract.Response.ValidationResponse;
import cdti.aidea.earas.contract.ValidationErrorResponse;
import cdti.aidea.earas.model.Btr_models.*;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterVillage;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import cdti.aidea.earas.repository.Btr_repo.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.sl.draw.geom.GuideIf;
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
    private final TblMasterVillageRepository tblMasterVillageRepository;


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
            System.out.println("kl -------------- >        -----------");
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

        // Compute agriculture year window
        int agriYear = LocalDate.now().getYear();
        LocalDate agriYearStart = LocalDate.of(agriYear, 6, 1);
        LocalDate agriYearEnd = agriYearStart.plusYears(1).minusDays(1);

// Count existing KeyPlots for this zone in this agri year
        long existingCount = keyPlotsRepository.countKeyPlotsInYear(
                zone.getZoneId(), agriYearStart, agriYearEnd);

        if (existingCount >= 100) {
            throw new RuntimeException(
                    "Maximum limit of 100 KeyPlots reached for this zone in the current agricultural year."
            );
        }

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

//        int agriYear = LocalDate.now().getYear();

// In Kerala or India, agri year may start in June — adjust accordingly
        // Start of agri year: 1st June at 00:00
        LocalDateTime startDateTime = LocalDate.of(agriYear, 6, 1).atStartOfDay();
        
        LocalDateTime endDateTime = startDateTime.plusYears(1).minusSeconds(1);

        Optional<Integer> maxClusterNumberOpt = clusterMasterRepository
                .findMaxClusterNumberByZoneAndDateRange(zone.getZoneId(), startDateTime, endDateTime);

        int nextClusterNumber = maxClusterNumberOpt.orElse(0) + 1;




        // 4️⃣ Save ClusterMaster
        ClusterMaster clusterMaster = new ClusterMaster();
        clusterMaster.setKeyPlot(keyPlot);
        clusterMaster.setClusterNumber(nextClusterNumber);
        clusterMaster.setStatus("Not Started");
        clusterMaster.setIsReject(false);
        clusterMaster.setIs_active(true);
        clusterMaster.setIs_editable(true);
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
        System.out.println("test "+dtoList);
        for (TblBtrDataDTO dto : dtoList) {
            // Required validation
            List<String> requiredErrors = validateRequiredFields(dto);
            if (!requiredErrors.isEmpty()) {
                allErrors.add(
                        new ValidationErrorResponse(
                                dto.getResvno(), dto.getResbdno(), dto.getWardno(),dto.getHouseno(),dto.getTotCent(),String.join(", ", requiredErrors)));
            }

            // Duplicate validation
            System.out.println("DTo >>   "+dto.getBtrtype());

            ValidationErrorResponse duplicateError = validateDuplicate(dto);
            if (duplicateError != null) {
                System.out.println("tpy not null");
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
                entity.setWardnumber(dto.getWardno());
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
                entity.setOldsvno(dto.getOldsvno());
                entity.setOldsubno(dto.getOldsubno());
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

    private boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    // ---------------- Duplicate Validation ----------------
    private ValidationErrorResponse validateDuplicate(TblBtrDataDTO dto) {
        System.out.println("ssss   " + dto);

        boolean existsRes = false;
        System.out.println("dto  "+dto);

        if (dto.getResvno() != null ){
        if (dto.getResbdno() != null && notEmpty(dto.getResbdno())) {

            existsRes = tblBtrDataRepository.existsByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndResvnoAndResbdno(
                    dto.getDcode(), dto.getTcode(), dto.getVcode(), dto.getBcode(), dto.getLbcode(),
                    dto.getResvno(), dto.getResbdno());
        } else {

            existsRes = tblBtrDataRepository.existsByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndResvno(
                    dto.getDcode(), dto.getTcode(), dto.getVcode(), dto.getBcode(), dto.getLbcode(), dto.getResvno());


        }
            if (existsRes) {
                return new ValidationErrorResponse(
                        dto.getResvno(),
                        dto.getResbdno(),
                        dto.getWardno(),
                        dto.getHouseno(),
                        dto.getTotCent(),
                        "Duplicate entry already exists for resvno=" + dto.getResvno()
                                + (dto.getResbdno() != null ? " and resbdno=" + dto.getResbdno() : ""));
            }}


        // ---------------- Type 1 ----------------
        if (dto.getBtrtype() == 1) {
            System.out.println("Btr List val" + 1);

            boolean exists = tblBtrDataRepository.existsByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndResvnoAndResbdno(
                    dto.getDcode(), dto.getTcode(), dto.getVcode(),
                    dto.getBcode(), dto.getLbcode(), dto.getResvno(), dto.getResbdno());

            if (exists) {
                System.out.println("Btr List val" + 1);
                return new ValidationErrorResponse(
                        dto.getResvno(), dto.getResbdno(),
                        dto.getWardno(), dto.getHouseno(),
                        dto.getTotCent(),
                        "Duplicate entry already exists for resvno=" + dto.getResvno()
                                + " and resbdno=" + dto.getResbdno());
            }

        } else if (dto.getBtrtype() == 2) {
            System.out.println("House List val" + 2);

            // --------- Added resvno/resbdno check ----------


            boolean exists = tblBtrDataRepository.existsByDcodeAndLbcodeAndWardnumberAndHouseno(
                    dto.getDcode(), dto.getLbcode(), dto.getWardno(), dto.getHouseno());

            if (exists) {
                System.out.println("House List val" + 2);
                return new ValidationErrorResponse(
                        dto.getResvno(), dto.getResbdno(),
                        dto.getWardno(), dto.getHouseno(),
                        dto.getTotCent(),
                        "Duplicate entry already exists for Ward Number=" + dto.getWardno()
                                + " and House No=" + dto.getHouseno());
            }

        } else if (dto.getBtrtype() == 3) {
            System.out.println("CL List val" + 3);

            // --------- Added resvno/resbdno check ----------
//            boolean existsRes = false;
//            if (dto.getResbdno() != null) {
//                existsRes = tblBtrDataRepository.existsByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndResvnoAndResbdno(
//                        dto.getDcode(), dto.getTcode(), dto.getVcode(), dto.getBcode(), dto.getLbcode(),
//                        dto.getResvno(), dto.getResbdno());
//            } else {
//                existsRes = tblBtrDataRepository.existsByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndResvno(
//                        dto.getDcode(), dto.getTcode(), dto.getVcode(), dto.getBcode(), dto.getLbcode(), dto.getResvno());
//            }
//
//            if (existsRes) {
//                return new ValidationErrorResponse(
//                        dto.getResvno(), dto.getResbdno(),
//                        dto.getWardno(), dto.getHouseno(),
//                        dto.getTotCent(),
//                        "Duplicate entry already exists for resvno=" + dto.getResvno()
//                                + (dto.getResbdno() != null ? " and resbdno=" + dto.getResbdno() : ""));
//            }

            boolean exists = tblBtrDataRepository.existsByDcodeAndTcodeAndLbcodeAndVcodeAndBcodeAndOwnernameAndAddressAndTotCent(
                    dto.getDcode(), dto.getTcode(), dto.getLbcode(), dto.getVcode(),
                    dto.getBcode(), dto.getOwnername(), dto.getAddress(), dto.getTotCent());

            if (exists) {
                System.out.println("CL List val" + 3);
                return new ValidationErrorResponse(
                        dto.getResvno(), dto.getResbdno(),
                        dto.getWardno(), dto.getHouseno(),
                        dto.getTotCent(),
                        "Duplicate entry already exists for LandOwner name=" + dto.getOwnername()
                                + " and landowner Address=" + dto.getAddress());
            }

        } else if (dto.getBtrtype() == 4) {
            System.out.println("TP List val " + 4);

            // --------- Added resvno/resbdno check ----------
//            boolean existsRes = false;
//            if (dto.getResbdno() != null) {
//                existsRes = tblBtrDataRepository.existsByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndResvnoAndResbdno(
//                        dto.getDcode(), dto.getTcode(), dto.getVcode(), dto.getBcode(), dto.getLbcode(),
//                        dto.getResvno(), dto.getResbdno());
//            } else {
//                existsRes = tblBtrDataRepository.existsByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndResvno(
//                        dto.getDcode(), dto.getTcode(), dto.getVcode(), dto.getBcode(), dto.getLbcode(), dto.getResvno());
//            }
//
//            if (existsRes) {
//                return new ValidationErrorResponse(
//                        dto.getResvno(), dto.getResbdno(),
//                        dto.getWardno(), dto.getHouseno(),
//                        dto.getTotCent(),
//                        "Duplicate entry already exists for resvno=" + dto.getResvno()
//                                + (dto.getResbdno() != null ? " and resbdno=" + dto.getResbdno() : ""));
//            }

            boolean exists;
            if (dto.getTbsubdivisionno() != null) {
                exists = tblBtrDataRepository.existsByDcodeAndTcodeAndVcodeAndBcodeAndTpnoAndTbsubdivisionno(
                        dto.getDcode(), dto.getTcode(), dto.getVcode(),
                        dto.getBcode(), dto.getTpno(), dto.getTbsubdivisionno());
            } else {
                exists = tblBtrDataRepository.existsByDcodeAndTcodeAndVcodeAndBcodeAndTpno(
                        dto.getDcode(), dto.getTcode(), dto.getVcode(), dto.getBcode(), dto.getTpno());
            }

            if (exists) {
                System.out.println("TP List val " + 4);
                return new ValidationErrorResponse(
                        dto.getResvno(), dto.getResbdno(),
                        dto.getWardno(), dto.getHouseno(),
                        dto.getTotCent(),
                        "Duplicate entry already exists for Tp No=" + dto.getTpno()
                                + " and Tp Sub no=" + dto.getTbsubdivisionno());
            }

        } else if (dto.getBtrtype() == 5) {
            System.out.println("Old survey " + 5);

            // --------- Added resvno/resbdno check ----------
//            boolean existsRes = false;
//            if (dto.getResbdno() != null) {
//                existsRes = tblBtrDataRepository.existsByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndResvnoAndResbdno(
//                        dto.getDcode(), dto.getTcode(), dto.getVcode(), dto.getBcode(), dto.getLbcode(),
//                        dto.getResvno(), dto.getResbdno());
//            } else {
//                existsRes = tblBtrDataRepository.existsByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndResvno(
//                        dto.getDcode(), dto.getTcode(), dto.getVcode(), dto.getBcode(), dto.getLbcode(), dto.getResvno());
//            }
//
//            if (existsRes) {
//                return new ValidationErrorResponse(
//                        dto.getResvno(), dto.getResbdno(),
//                        dto.getWardno(), dto.getHouseno(),
//                        dto.getTotCent(),
//                        "Duplicate entry already exists for resvno=" + dto.getResvno()
//                                + (dto.getResbdno() != null ? " and resbdno=" + dto.getResbdno() : ""));
//            }

            boolean exists;
            if (dto.getOldsubno() != null) {
                exists = tblBtrDataRepository.existsByDcodeAndTcodeAndVcodeAndBcodeAndOldsvnoAndOldsubno(
                        dto.getDcode(), dto.getTcode(), dto.getVcode(), dto.getBcode(),
                        dto.getOldsvno(), dto.getOldsubno());
            } else {
                exists = tblBtrDataRepository.existsByDcodeAndTcodeAndVcodeAndBcodeAndOldsvno(
                        dto.getDcode(), dto.getTcode(), dto.getVcode(), dto.getBcode(), dto.getOldsvno());
            }

            if (exists) {
                System.out.println("Old Survey val " + 5);
                return new ValidationErrorResponse(
                        dto.getResvno(), dto.getResbdno(),
                        dto.getWardno(), dto.getHouseno(),
                        dto.getTotCent(),
                        "Duplicate entry already exists for Old Survey Number=" + dto.getOldsvno()
                                + " and Old Subdivision Number=" + dto.getOldsubno());
            }
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


    public ValidationResponse validateDuplicateForCluster(TblBtrDataDTO dto) {
        String cleanedResbdno = dto.getResbdno() != null ?
                dto.getResbdno().trim().replaceFirst("^0+(?!$)", "") : null;

        List<TblBtrData> plots;
        TblMasterZone zone = tblMasterZoneRepository.findById(dto.getZoneId())
                .orElseThrow(() -> new RuntimeException("Zone not found"));

        Optional<TblMasterVillage> village  = tblMasterVillageRepository.findByVillageId(dto.getVcode());
        // Handle both cases: with and without subdivision
        if (cleanedResbdno != null && !cleanedResbdno.isEmpty()) {
            System.out.println("ssss :  "+ dto.getResvno()+"  : "+cleanedResbdno);
            System.out.println(dto.getDcode()+" "+village.get().getRevTalukId()+" "+dto.getVcode()+" "
            +dto.getBcode()+" "+dto.getLbcode()+" "+dto.getResvno()+" "+cleanedResbdno);


            // Case 1: User provided both survey number AND subdivision
            plots = tblBtrDataRepository.findByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndResvnoAndResbdno(
                    zone.getDistId(),
                    village.get().getRevTalukId(),
                    dto.getVcode(),
                    dto.getBcode(),
                    dto.getLbcode(),
                    dto.getResvno(),
                    cleanedResbdno);

            if (plots.isEmpty()) {
                // If exact match not found, check if survey number exists with any subdivision
                List<TblBtrData> surveyOnlyPlots = tblBtrDataRepository.findByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndResvno(
                        zone.getDistId(),
                        zone.getDesTalukId(),
                        dto.getVcode(),
                        dto.getBcode(),
                        dto.getLbcode(),
                        dto.getResvno());

                if (!surveyOnlyPlots.isEmpty()) {
                    return createSurveyWithSubdivisionsResponse(surveyOnlyPlots, dto.getResvno());
                }
                return null; // No plot found at all
            }
        } else {
            // Case 2: User provided only survey number (no subdivision)
            System.out.println("only survey  :  "+ dto.getResvno()+"  : "+cleanedResbdno);
            System.out.println(dto.getDcode()+" "+village.get().getRevTalukId()+" "+dto.getVcode()+" "
                    +dto.getBcode()+" "+dto.getLbcode()+" "+dto.getResvno()+" "+cleanedResbdno);
            plots = tblBtrDataRepository.findByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndResvnoAndResbdno(
                    zone.getDistId(),
                    zone.getDesTalukId(),
                    dto.getVcode(),
                    dto.getBcode(),
                    dto.getLbcode(),
                    dto.getResvno(),
                    null);

            if (plots.isEmpty()) {
                return null; // No plot found
            }

            // If multiple subdivisions exist for this survey number, show options
            if (plots.size() > 1) {
                return createSurveyWithSubdivisionsResponse(plots, dto.getResvno());
            }
        }

        // Calculate remaining area for single plot case
        return calculateRemainingAreaForPlot(plots.get(0), dto.getResvno(), cleanedResbdno);
    }


    public ValidationResponse validateDuplicateForNonBtrCluster(TblBtrDataDTO dto) {
        TblMasterZone zone = tblMasterZoneRepository.findById(dto.getZoneId())
                .orElseThrow(() -> new RuntimeException("Zone not found"));
        System.out.println(">>>>   "+dto);
        String lbcode = dto.getLbcode() != null ? dto.getLbcode() : getLbcodeFromZone(zone);

        Integer type = Math.toIntExact(dto.getBtrtype());
        System.out.println("Validating Non-BTR Duplicate for Type: " + type);

        switch (type) {
            case 2:
                return validateHouseListDuplicate(dto, zone, lbcode);
            case 3:
                return validateCultivatorsListDuplicate(dto, zone, lbcode);
            case 4:
                return validateThandaperDuplicate(dto, zone, lbcode);
            case 5:
                return validateOldSurveyDuplicate(dto, zone, lbcode);
            default:
                throw new RuntimeException("Invalid Non-BTR Type: " + type);
        }
    }


    private ValidationResponse validateHouseListDuplicate(TblBtrDataDTO dto, TblMasterZone zone, String lbcode) {
        System.out.println(zone.getDistId()+" " +lbcode +" " +dto.getWardno()+" " +dto.getHouseno()+" "+zone);
        if (dto.getWardno() == null || dto.getHouseno() == null) {
            throw new RuntimeException("Ward number and House number are required for House List validation");
        }
        Optional<TblMasterVillage> village  = tblMasterVillageRepository.findByVillageId(dto.getVcode());
        // Use query method that returns actual data instead of existsBy
        List<TblBtrData> plots = tblBtrDataRepository.findByDcodeAndTcodeAndLbcodeAndWardnumberAndHouseno(
                zone.getDistId(),  village.get().getRevTalukId(), lbcode, dto.getWardno(), dto.getHouseno());

        if (!plots.isEmpty()) {
            TblBtrData plot = plots.get(0);
            return calculateRemainingAreaForPlot(plot, null, "House: Ward " + dto.getWardno() + ", House " + dto.getHouseno());
        }

        // Optional: check survey number/subdivision (like in BTR)
        return validateSurveyNumberIfProvided(dto, zone);
    }

    private ValidationResponse validateCultivatorsListDuplicate(TblBtrDataDTO dto, TblMasterZone zone, String lbcode) {
        if (dto.getOwnername() == null || dto.getAddress() == null || dto.getTotCent() <= 0) {
            throw new RuntimeException("Owner name, address, and total area are required for Cultivators List validation");
        }
        Optional<TblMasterVillage> village  = tblMasterVillageRepository.findByVillageId(dto.getVcode());
        // Use query method that returns actual data instead of existsBy
        List<TblBtrData> plots = tblBtrDataRepository.findByDcodeAndTcodeAndLbcodeAndVcodeAndBcodeAndOwnernameAndAddressAndTotCent(
                zone.getDistId(), village.get().getRevTalukId(), lbcode, dto.getVcode(),
                dto.getBcode(), dto.getOwnername(), dto.getAddress(), dto.getTotCent());

        if (!plots.isEmpty()) {
            TblBtrData plot = plots.get(0);
            return calculateRemainingAreaForPlot(plot, null, "Cultivator: " + dto.getOwnername());
        }

        // Optional: also validate survey number if provided
        return validateSurveyNumberIfProvided(dto, zone);
    }


    private ValidationResponse validateThandaperDuplicate(TblBtrDataDTO dto, TblMasterZone zone, String lbcode) {
        if (dto.getTpno() == null) {
            throw new RuntimeException("Thandaper number is required for Thandaper validation");
        }

        List<TblBtrData> plots;
        Optional<TblMasterVillage> village  = tblMasterVillageRepository.findByVillageId(dto.getVcode());
        if (dto.getTbsubdivisionno() != null) {
            plots = tblBtrDataRepository.findByDcodeAndTcodeAndVcodeAndBcodeAndTpnoAndTbsubdivisionno(
                    zone.getDistId(), village.get().getRevTalukId(), dto.getVcode(), dto.getBcode(),
                    dto.getTpno(), dto.getTbsubdivisionno());
        } else {
            plots = tblBtrDataRepository.findByDcodeAndTcodeAndVcodeAndBcodeAndTpnoAndTbsubdivisionno(
                    zone.getDistId(), village.get().getRevTalukId(), dto.getVcode(), dto.getBcode(),
                    dto.getTpno(),null);
        }

        if (plots.isEmpty()) return null;

        if (plots.size() > 1 && dto.getTbsubdivisionno() == null) {
            return createThandaperSubdivisionsResponse(plots, dto.getTpno());
        }

        TblBtrData plot = plots.get(0);
        return calculateRemainingAreaForPlot(plot, null,
                dto.getTbsubdivisionno() != null ? dto.getTbsubdivisionno().toString() : null);
    }

    private ValidationResponse validateOldSurveyDuplicate(TblBtrDataDTO dto, TblMasterZone zone, String lbcode) {
        if (dto.getOldsvno() == null) {
            throw new RuntimeException("Old survey number is required for Old Survey validation");
        }

        List<TblBtrData> plots;
        Optional<TblMasterVillage> village  = tblMasterVillageRepository.findByVillageId(dto.getVcode());
        if (dto.getOldsubno() != null && !dto.getOldsubno().isEmpty()) {
            plots = tblBtrDataRepository.findByDcodeAndTcodeAndVcodeAndBcodeAndOldsvnoAndOldsubno(
                    zone.getDistId(), village.get().getRevTalukId(), dto.getVcode(), dto.getBcode(),
                    dto.getOldsvno(), dto.getOldsubno());
        } else {
            plots = tblBtrDataRepository.findByDcodeAndTcodeAndVcodeAndBcodeAndOldsvno(
                    zone.getDistId(), village.get().getRevTalukId(), dto.getVcode(), dto.getBcode(),
                    dto.getOldsvno());
        }

        if (plots.isEmpty()) return null;

        if (plots.size() > 1 && (dto.getOldsubno() == null || dto.getOldsubno().isEmpty())) {
            return createOldSurveySubdivisionsResponse(plots, dto.getOldsvno());
        }

        TblBtrData plot = plots.get(0);
        return calculateRemainingAreaForPlot(plot, dto.getOldsvno(), dto.getOldsubno());
    }



    // Helper method to validate survey number if provided (common for all types)
    private ValidationResponse validateSurveyNumberIfProvided(TblBtrDataDTO dto, TblMasterZone zone) {
        if (dto.getResvno() == null) return null;

        String cleanedResbdno = dto.getResbdno() != null ?
                dto.getResbdno().trim().replaceFirst("^0+(?!$)", "") : null;

        List<TblBtrData> surveyPlots;
        Optional<TblMasterVillage> village  = tblMasterVillageRepository.findByVillageId(dto.getVcode());
        if (cleanedResbdno != null && !cleanedResbdno.isEmpty()) {
            surveyPlots = tblBtrDataRepository.findByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndResvnoAndResbdno(
                    zone.getDistId(),  village.get().getRevTalukId(), dto.getVcode(), dto.getBcode(), dto.getLbcode(),
                    dto.getResvno(), cleanedResbdno);
        } else {
            surveyPlots = tblBtrDataRepository.findByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndResvno(
                    zone.getDistId(),  village.get().getRevTalukId(), dto.getVcode(), dto.getBcode(),dto.getLbcode(), dto.getResvno());
        }

        if (!surveyPlots.isEmpty()) {
            if (surveyPlots.size() > 1 && (cleanedResbdno == null || cleanedResbdno.isEmpty())) {
                return createSurveyWithSubdivisionsResponse(surveyPlots, dto.getResvno());
            }
            return calculateRemainingAreaForPlot(surveyPlots.get(0), Integer.valueOf(dto.getResvno().toString()), cleanedResbdno);
        }

        return null;
    }

    // Helper methods for subdivision responses
    private ValidationResponse createThandaperSubdivisionsResponse(List<TblBtrData> plots, Integer tpno) {
        List<String> subdivisions = plots.stream()
                .map(plot -> plot.getTbsubdivisionno() != null ? plot.getTbsubdivisionno().toString() : "No Subdivision")
                .distinct()
                .collect(Collectors.toList());

        ValidationResponse response = new ValidationResponse();
        response.setMessage("Multiple subdivisions found for Thandaper number: " + tpno + ". Please select one.");
        response.setAvailableSubdivisions(subdivisions);
        response.setTotalcent(plots.get(0).getTotCent());
        return response;
    }

    private ValidationResponse createOldSurveySubdivisionsResponse(List<TblBtrData> plots, Integer oldsvno) {
        List<String> subdivisions = plots.stream()
                .map(plot -> plot.getOldsubno() != null ? plot.getOldsubno() : "No Subdivision")
                .distinct()
                .collect(Collectors.toList());

        ValidationResponse response = new ValidationResponse();
        response.setMessage("Multiple subdivisions found for Old Survey number: " + oldsvno + ". Please select one.");
        response.setAvailableSubdivisions(subdivisions);
        response.setTotalcent(plots.get(0).getTotCent());
        return response;
    }

    private String getLbcodeFromZone(TblMasterZone zone) {
        // Implement logic to get lbcode from zone
        // This might involve another repository call or mapping
        return "default_lbcode"; // Replace with actual implementation
    }

    private ValidationResponse createSurveyWithSubdivisionsResponse(List<TblBtrData> plots, Integer resvno) {
        // Collect all available subdivisions for this survey number
        List<String> availableSubdivisions = plots.stream()
                .map(plot -> plot.getResbdno() != null ? plot.getResbdno() : "")
                .distinct()
                .collect(Collectors.toList());

        String subdivisionsList = String.join(", ", availableSubdivisions);

        return new ValidationResponse(
                null,
                resvno,
                null, // No specific subdivision selected
                calculateTotalArea(plots),
                "Survey number found with multiple subdivisions. Available: " + subdivisionsList + ". Please select one.",
                -1.0, // Negative value indicates subdivision selection needed
                availableSubdivisions // New field to pass available options
        );
    }

    private ValidationResponse calculateRemainingAreaForPlot(TblBtrData plot, Integer identifier, String subdivision) {
        int currentYear = java.time.LocalDate.now().getYear();
        int nextYear = currentYear + 1;

        double totalEnumerated = 0.0;
        double totalArea = plot.getTotCent() != null ? plot.getTotCent() : 0.0;

        List<ClusterFormData> clusterDataList = clusterFormDataRepository.findByPlotAndCreatedAtBetween(
                plot,
                java.time.LocalDate.of(currentYear, 7, 1).atStartOfDay(),
                java.time.LocalDate.of(nextYear, 6, 30).atTime(23, 59, 59));

        totalEnumerated += clusterDataList.stream()
                .mapToDouble(cd -> cd.getEnumeratedArea() != null ? cd.getEnumeratedArea() : 0.0)
                .sum();

        double remainingArea = totalArea - totalEnumerated;

        // For non-survey cases, identifier might be null
        Integer resvno = (identifier != null) ? identifier : null;
        String resbdno = subdivision;

        if (remainingArea <= 0) {
            return new ValidationResponse(
                    plot.getId(),
                    resvno,
                    resbdno,
                    totalArea,
                    "This " + getPlotType(plot) + " cannot be selected for this agricultural year (no remaining area)",
                    remainingArea,
                    null
            );
        } else if (remainingArea > 0 && totalEnumerated > 0) {
            return new ValidationResponse(
                    plot.getId(),
                    resvno,
                    resbdno,
                    totalArea,
                    "Remaining area available for reuse in this agricultural year",
                    remainingArea,
                    null
            );
        } else {
            return new ValidationResponse(
                    plot.getId(),
                    resvno,
                    resbdno,
                    totalArea,
                    "Duplicate entry already exists but has available area",
                    remainingArea,
                    null
            );
        }
    }


    private String getPlotType(TblBtrData plot) {
        if (plot.getResvno() != null) return "survey plot";
        if (plot.getTpno() != null) return "thandaper";
        if (plot.getOldsvno() != null) return "old survey plot";
        if (plot.getWardnumber() != null && plot.getHouseno() != null) return "house";
        if (plot.getOwnername() != null) return "cultivator plot";
        return "plot";
    }

    private Double calculateTotalArea(List<TblBtrData> plots) {
        return plots.stream()
                .mapToDouble(plot -> plot.getTotCent() != null ? plot.getTotCent() : 0.0)
                .sum();
    }


    public TblBtrDetailsResponse getBtrDetails(Long btrId) {
        TblBtrData entity = tblBtrDataRepository.findById(btrId)
                .orElseThrow(() -> new RuntimeException("BTR record not found with ID: " + btrId));

        return new TblBtrDetailsResponse(
                entity.getResvno(),
                entity.getResbdno(),
                entity.getTotCent(),
                entity.getAddress(),
                entity.getWardnumber(),  // assuming field name in entity is wardnumber
                entity.getHouseno(),
                entity.getOldsvno(),
                entity.getOldsubno(),
                entity.getOwnername(),
                entity.getTpno(),
                entity.getTbsubdivisionno(),
                entity.getBtrtype().getBTypeId(),
                2L
        );
    }

}
