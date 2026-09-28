package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.Response.TblBtrDataDTO;
import cdti.aidea.earas.contract.Response.TblBtrDetailsResponse;
import cdti.aidea.earas.contract.Response.ValidationResponse;
import cdti.aidea.earas.contract.Response.ZoneLocationResponse;
import cdti.aidea.earas.contract.ValidationErrorResponse;
import cdti.aidea.earas.model.Btr_models.*;
import cdti.aidea.earas.model.Btr_models.Masters.TblLocalBody;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterVillage;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import cdti.aidea.earas.repository.Btr_repo.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import cdti.aidea.earas.utils.AgriYearUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.sl.draw.geom.GuideIf;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TblBtrDataService {

    // Constraint name enforced at the DB level on KeyPlots (btr_id, agri_start_year, agri_end_year).
    // Kept as a constant so the "is this a KeyPlot duplicate?" check has a single source of truth.
    private static final String KEYPLOT_BTR_YEAR_CONSTRAINT = "uq_keyplot_btr_year";

    private final TblBtrDataRepository tblBtrDataRepository;
    private final KeyPlotsRepository keyPlotsRepository;
    private final ClusterMasterRepository clusterMasterRepository;
    private final ClusterFormDataRepository clusterFormDataRepository;
    private final TblMasterZoneRepository tblMasterZoneRepository;
    private final TblNonBtrRepository tblNonBtrRepository;
    private final TblMasterVillageRepository tblMasterVillageRepository;
    private final TblMasterVillageRepository villageRepository;
    private final LocalBodyRepository localBodyRepository;
    private final Zone_Service zoneService;
    private final cdti.aidea.earas.service.TblBtrDataMapper tblBtrDataMapper;
    private final KeyPlotTransactionService keyPlotTransactionService;


    @Transactional
    public Map<String, Object> saveAllData(List<TblBtrDataDTO> dtoList) {
        List<ValidationErrorResponse> allErrors = new ArrayList<>();
        System.out.println("sto   "+dtoList);
        // Validate all DTOs first
        for (TblBtrDataDTO dto : dtoList) {
            // Required validation
            List<String> requiredErrors = validateRequiredFields(dto);

            if (!requiredErrors.isEmpty()) {
                allErrors.add(new ValidationErrorResponse(
                        dto.getResvno(), dto.getResbdno(), dto.getWardno(),
                        dto.getHouseno(), dto.getTotCent(),
                        String.join(", ", requiredErrors)
                ));
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

        // ===============================
        // ✅ Check zone limit for bulk save
        // ===============================
        if (!dtoList.isEmpty()) {
            TblBtrDataDTO firstDto = dtoList.get(0);
            TblMasterZone zone = tblMasterZoneRepository
                    .findById(Math.toIntExact(firstDto.getZoneId()))
                    .orElseThrow(() -> new RuntimeException("Zone not found"));


            LocalDate startDate =
                    AgriYearUtil.getAgriYearStart(dtoList.get(0).getAgriYear());

            LocalDate endDate =
                    AgriYearUtil.getAgriYearEnd(dtoList.get(0).getAgriYear());

            // Check current count
            long currentCount = keyPlotsRepository.countKeyPlotsForUpdate(
                    zone.getZoneId(), startDate, endDate);

            // ✅ Do NOT assume every submitted DTO consumes a new slot.
            // A DTO that reuses an existing BTR (dto.getId() != null) which already has a
            // KeyPlot for this exact agricultural year is a duplicate and will be skipped
            // later — it must not count against the 100-KeyPlot capacity here.
            long alreadyKeyPlottedCount = dtoList.stream()
                    .filter(d -> d.getId() != null)
                    .filter(d -> keyPlotsRepository.existsByBtrDataIdAndAgriStartYearAndAgriEndYear(
                            d.getId(),
                            AgriYearUtil.getAgriYearStart(d.getAgriYear()),
                            AgriYearUtil.getAgriYearEnd(d.getAgriYear())))
                    .count();

            long genuinelyNewCount = dtoList.size() - alreadyKeyPlottedCount;

            // Check if we can save all genuinely-new requested records
            if (currentCount + genuinelyNewCount > 100) {
                int availableSlots = (int) (100 - currentCount);
                throw new RuntimeException(
                        "Cannot save " + genuinelyNewCount + " new records. " +
                                "Only " + availableSlots + " slots available for this zone in current agricultural year."
                );
            }
        }

        // ✅ Save each record independently. Every plot gets its own REQUIRES_NEW
        // transaction via KeyPlotTransactionService, so a duplicate BTR+agri-year
        // KeyPlot for one plot cannot roll back the plots that already committed.
        List<Long> savedIds = new ArrayList<>();
        List<Long> duplicateIds = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (TblBtrDataDTO dto : dtoList) {
            try {
                Long savedId = keyPlotTransactionService.saveSinglePlot(dto);
                savedIds.add(savedId);
            } catch (DataIntegrityViolationException ex) {
                if (isKeyPlotBtrYearDuplicate(ex)) {
                    // Genuine uq_keyplot_btr_year violation -> this specific plot is a
                    // duplicate for this agri year. Skip it, keep processing the rest.
                    log.warn("Duplicate KeyPlot skipped for BTR id={}, agriYear={}",
                            dto.getId(), dto.getAgriYear());
                    duplicateIds.add(dto.getId());
                } else {
                    // Some other DB constraint failed — do not hide it as a duplicate.
                    log.error("Unexpected data integrity violation for BTR id={}", dto.getId(), ex);
                    errors.add("BTR id " + dto.getId() + ": " + rootCauseMessage(ex));
                }
            } catch (Exception ex) {
                log.error("Unexpected error saving plot for BTR id={}", dto.getId(), ex);
                errors.add("BTR id " + dto.getId() + ": " + ex.getMessage());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "COMPLETED");
        response.put("message", savedIds.size() + " plot(s) saved successfully, "
                + duplicateIds.size() + " duplicate plot(s) skipped, "
                + errors.size() + " plot(s) failed.");
        response.put("savedCount", savedIds.size());
        response.put("duplicateCount", duplicateIds.size());
        response.put("errorCount", errors.size());
        response.put("savedIds", savedIds);
        response.put("duplicateIds", duplicateIds);
        response.put("errors", errors);
        return response;
    }

    private boolean isKeyPlotBtrYearDuplicate(DataIntegrityViolationException ex) {
        Throwable cause = ex;
        while (cause != null) {
            String message = cause.getMessage();
            if (message != null && message.contains(KEYPLOT_BTR_YEAR_CONSTRAINT)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private String rootCauseMessage(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }

    private boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private ValidationErrorResponse validateDuplicate(TblBtrDataDTO dto) {

        LocalDate startDate =
                AgriYearUtil.getAgriYearStart(dto.getAgriYear());

        LocalDate endDate =
                AgriYearUtil.getAgriYearEnd(dto.getAgriYear());
        Optional<Integer> maxClusterNumberOpt =
                clusterMasterRepository.findMaxClusterNumberByAgriYear(
                        Math.toIntExact(dto.getZoneId()),
                        startDate,
                        endDate
                );
        System.out.println("dto   >>>>>>>>>>>>>   "+dto.getId());
        if (dto.getId() != null) {
            int nextClusterNumber = maxClusterNumberOpt.orElse(0) + 1;

            if (nextClusterNumber > 100) {
                return new ValidationErrorResponse(
                        0,
                        null,
                        0,
                        null,
                        0.0,
                        "Keyplots limit for current year is 100"
                );
            }
            return null;
        }
        boolean existsRes = false;
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


        int nextClusterNumber = maxClusterNumberOpt.orElse(0) + 1;

        if (nextClusterNumber > 100) {
            return new ValidationErrorResponse(
                    0,
                    null,
                    0,
                    null,
                    0.0,
                    "Keyplots limit for current year is 100"
            );
        }
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
            // --------- Added resvno/resbdno check ----------
            boolean exists = tblBtrDataRepository.existsByDcodeAndLbcodeAndWardnumberAndHouseno(
                    dto.getDcode(), dto.getLbcode(), dto.getWardno(), dto.getHouseno());
            if (exists) {
                return new ValidationErrorResponse(
                        dto.getResvno(), dto.getResbdno(),
                        dto.getWardno(), dto.getHouseno(),
                        dto.getTotCent(),
                        "Duplicate entry already exists for Ward Number=" + dto.getWardno()
                                + " and House No=" + dto.getHouseno());
            }

        } else if (dto.getBtrtype() == 3) {
            System.out.println("CL List val" + 3);


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



            boolean exists;
            if (dto.getOldsubno() != null) {
                System.out.println(dto.getDcode()+" " + dto.getTcode()+ " " +dto.getVcode()+" "+dto.getBcode()+
                        " "+dto.getOldsvno()+" "+dto.getOldsubno());
                exists = tblBtrDataRepository.existsByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndOldsvnoAndOldsubno(
                        dto.getDcode(), dto.getTcode(), dto.getVcode(), dto.getBcode(),dto.getLbcode(),
                        dto.getOldsvno(), dto.getOldsubno());
            } else {
                System.out.println(dto.getDcode()+" " + dto.getTcode()+ " " +dto.getVcode()+" "+dto.getBcode()+
                        " "+dto.getOldsvno()+" "+dto.getOldsubno());
                exists = tblBtrDataRepository.existsByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndOldsvno(
                        dto.getDcode(), dto.getTcode(), dto.getVcode(), dto.getBcode(), dto.getLbcode(),dto.getOldsvno());
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
        TblMasterZone zone = tblMasterZoneRepository.findById(Math.toIntExact(dto.getZoneId()))
                .orElseThrow(() -> new RuntimeException("Zone not found"));

        Optional<TblMasterVillage> village  = tblMasterVillageRepository.findByVillageId(dto.getVcode());
        // Handle both cases: with and without subdivision
        if (cleanedResbdno != null && !cleanedResbdno.isEmpty()) {
            System.out.println("ssss :  "+ dto.getResvno()+"  : "+cleanedResbdno);
            System.out.println(dto.getDcode()+" "+village.get().getRevTalukId()+" "+dto.getVcode()+" "
                    +dto.getBcode()+" "+dto.getLbcode()+" "+dto.getResvno()+" "+cleanedResbdno+ "  "+zone.getDistId());


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
            System.out.println(zone.getDistId()+" "+village.get().getRevTalukId()+" "+dto.getVcode()+" "
                    +dto.getBcode()+" "+dto.getLbcode()+" "+dto.getResvno()+" "+cleanedResbdno);
            plots = tblBtrDataRepository.findByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndResvnoAndResbdno(
                    zone.getDistId(),
                    village.get().getRevTalukId(),
                    dto.getVcode(),
                    dto.getBcode(),
                    dto.getLbcode(),
                    dto.getResvno(),
                    "");

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
        TblMasterZone zone = tblMasterZoneRepository.findById(Math.toIntExact(dto.getZoneId()))
                .orElseThrow(() -> new RuntimeException("Zone not found"));

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
                null,
                availableSubdivisions // New field to pass available options
        );
    }

    private ValidationResponse calculateRemainingAreaForPlot(TblBtrData plot, Integer identifier, String subdivision) {
        int currentYear = java.time.LocalDate.now().getYear();
        int nextYear = currentYear + 1;
        System.out.println("plots >>>  "+plot);
        double totalEnumerated = 0.0;
        double totalArea = plot.getTotCent() != null ? plot.getTotCent() : 0.0;
        LocalDate today = LocalDate.now();
        LocalDateTime startDate;
        LocalDateTime endDate;

        if (today.getMonthValue() >= 7) {
            // July to December
            startDate = LocalDate.of(today.getYear(), 7, 1).atStartOfDay();
            endDate = LocalDate.of(today.getYear() + 1, 6, 30).atTime(23, 59, 59);
        } else {
            // January to June
            startDate = LocalDate.of(today.getYear() - 1, 7, 1).atStartOfDay();
            endDate = LocalDate.of(today.getYear(), 6, 30).atTime(23, 59, 59);
        }

        List<ClusterFormData> clusterDataList = clusterFormDataRepository.findByPlotAndCreatedAtBetween(
                plot,
                startDate,
                endDate
        );

        totalEnumerated += clusterDataList.stream()
                .mapToDouble(cd -> cd.getEnumeratedArea() != null ? cd.getEnumeratedArea() : 0.0)
                .sum();
        System.out.println("total >>>  "+totalArea);
        System.out.println("Emureted  >>> "+totalEnumerated);
        double remainingArea = totalArea - totalEnumerated;
        System.out.println("ccccc >>  "+clusterDataList);
        System.out.println("remain "+remainingArea);
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
                    plot.getLtype(),
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
                    plot.getLtype(),
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
                    plot.getLtype(),
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
                .orElseThrow(() ->
                        new RuntimeException("BTR record not found with ID: " + btrId));

        TblMasterVillage village = villageRepository.findById(entity.getVcode())
                .orElseThrow(() ->
                        new RuntimeException("Village not found with ID: " + entity.getVcode()));

        TblLocalBody localBody = localBodyRepository.findByCodeApi(entity.getLbcode())
                .orElseThrow(() ->
                        new RuntimeException("Local body not found with code: " + entity.getLbcode()));

        ZoneLocationResponse zoneLocation =
                zoneService.getZoneLocationDetails(entity.getZone().intValue(), null);

        return new TblBtrDetailsResponse(
                entity.getResvno(),
                entity.getResbdno(),
                entity.getBcode(),
                entity.getTotCent(),
                entity.getAddress(),
                entity.getWardnumber(),
                entity.getHouseno(),
                entity.getOldsvno(),
                entity.getOldsubno(),
                entity.getOwnername(),
                entity.getTpno(),
                entity.getTbsubdivisionno(),
                entity.getBtrtype().getBTypeId(),
                entity.getCl_no(),
                entity.getLbcode(),
                entity.getLtype(),

                village.getVillageId(),
                village.getVillageNameEn(),

                localBody.getLocalbodyId(),
                localBody.getLocalbodyNameEn(),

                zoneLocation.getDistrictId(),
                zoneLocation.getDistrictName(),
                zoneLocation.getTalukId(),
                zoneLocation.getTalukName(),
                zoneLocation.getBlockId(),
                zoneLocation.getBlockName(),
                zoneLocation.getZoneName()
        );
    }

    @Transactional
    public String updateTotCentAndHandleClusterData(Long btrId, Double newTotCent, UUID userId) {

        // 1️⃣ Get total area currently used in clusters
        Double totalClusterArea = clusterFormDataRepository.getTotalEnumeratedAreaByBtrId(btrId);
        if (totalClusterArea == null) totalClusterArea = 0.0;

        // 2️⃣ Prepare values
        LocalDateTime now = LocalDateTime.now();

        // 3️⃣ Update all fields
        int updatedRows = tblBtrDataRepository.updateTotCent(btrId, newTotCent, now, userId);
        if (updatedRows == 0) {
            throw new EntityNotFoundException("TblBtrData with id " + btrId + " not found");
        }

        // 4️⃣ Business logic
        if (newTotCent < totalClusterArea) {
            clusterFormDataRepository.deleteByBtrId(btrId);
            return "New totCent is less than total enumerated area. ClusterFormData entries deleted, totCent updated.";
        }

        return "totCent updated successfully. No cluster entries deleted.";
    }

}