package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.Projection.BtrStatsProjection;
import cdti.aidea.earas.contract.RequestsDTOs.TblWorkAllocationDTO;
import cdti.aidea.earas.contract.RequestsDTOs.WorkAllocationVerificationRequest;
import cdti.aidea.earas.contract.Response.KeyPlotResponse;
import cdti.aidea.earas.model.Btr_models.Masters.*;
import cdti.aidea.earas.model.Btr_models.TblWorkAllocation;
import cdti.aidea.earas.model.Btr_models.TblWorkAllocationApproval;
import cdti.aidea.earas.model.Btr_models.TblWorkAllocationVerification;
import cdti.aidea.earas.repository.Btr_repo.*;
import cdti.aidea.earas.utils.AgriYearUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkallocationService {

    private final TblWorkAllocationRepository tblWorkAllocationRepository;
    private final TblWorkAllocationApprovalRepository tblWorkAllocationApprovalRepository;
    private final TblMasterZoneRepository tblMasterZoneRepository;
    private final AgriYearUtil agriYearUtil;
    private final TblWorkAllocationVerificationRepository tblWorkAllocationVerificationRepository;

    private final LocalBodyRepository localBodyRepository;
    private final LocalBodyTypeRepository localBodyTypeRepository;
    private final ZoneLocalbodyBlockMappingRepository zoneLocalbodyBlockMappingRepository;
    private final DistrictMasterRepository districtMasterRepository;
    private final DesTalukRepository desTalukRepository;
    private final MasterBlockRepository masterBlockRepository;
    private final TblZoneLocalbodyMappingRepository tblZoneLocalbodyMappingRepository;

    public Object ZoneDetailsWorkAllocation(Integer zone_id) {

        // ✅ Validate Zone
        TblMasterZone zone = tblMasterZoneRepository.findById(zone_id)
                .orElseThrow(() -> new RuntimeException("Zone not available"));

        // ==========================================================
        // Get mapped Local Bodies for this Zone
        // ==========================================================

        List<TblZoneLocalbodyMapping> mappings =
                tblZoneLocalbodyMappingRepository.findByZoneAndIsValid(zone_id, true);

        List<Integer> localBodyIds = mappings.stream()
                .map(TblZoneLocalbodyMapping::getLocalbody)
                .distinct()
                .toList();

        List<TblLocalBody> localBodies =
                localBodyRepository.findByLocalbodyIdInOrderByLocalbodyNameEnAsc(localBodyIds);

        // ==========================================================
        // Local Body Type
        // ==========================================================

        List<Long> typeIds = localBodies.stream()
                .map(TblLocalBody::getLocalbodyType)
                .filter(Objects::nonNull)
                .map(Short::longValue)
                .distinct()
                .toList();

        List<LocalBodyType> bodyTypes =
                localBodyTypeRepository.findByIdIn(typeIds);

        Map<Long, String> typeMap = new HashMap<>();

        for (LocalBodyType type : bodyTypes) {
            typeMap.put(type.getId(), type.getName());
        }

        // ==========================================================
        // Response Data
        // ==========================================================

        List<Map<String, Object>> responseData = new ArrayList<>();

        List<String> lbCodes = new ArrayList<>();

        for (TblLocalBody lb : localBodies) {

            Map<String, Object> row = new HashMap<>();

            row.put("localbodyId", lb.getLocalbodyId());
            row.put("lbcode", lb.getCodeApi());
            row.put("p_name", lb.getLocalbodyNameEn());

            String typeName = "";

            if (lb.getLocalbodyType() != null) {
                typeName = typeMap.getOrDefault(
                        Long.valueOf(lb.getLocalbodyType()),
                        ""
                );
            }

            row.put("localbodytype", typeName);

            responseData.add(row);

            lbCodes.add(lb.getCodeApi());
        }

        // ==========================================================
        // District & Taluk
        // ==========================================================

        String districtName = districtMasterRepository
                .findById(Long.valueOf(zone.getDistId()))
                .map(DistrictMaster::getDist_name_en)
                .orElse("");

        String talukName = desTalukRepository
                .findById(zone.getDesTalukId())
                .map(DesTaluk::getDesTalukNameEn)
                .orElse("");

        // ==========================================================
        // Zone Label
        // ==========================================================

        String localBodyLabel = "";
        String localbodyType = "";

        Optional<ZoneLocalbodyBlockMapping> zoneMapping =
                zoneLocalbodyBlockMappingRepository.findByZoneAndIsValid(
                        zone.getZoneId(),
                        true
                );

        if (zoneMapping.isPresent()) {

            if (zoneMapping.get().getBlockPanchayatMunicipalArea() == 1) {

                localBodyLabel = masterBlockRepository
                        .findById(zoneMapping.get().getBlockDetails())
                        .map(MasterBlock::getBlockName)
                        .orElse("");

                localbodyType = "Block Panchayath";

            } else {

                Optional<TblLocalBody> lb =
                        localBodyRepository.findById(
                                zoneMapping.get().getBlockDetails());

                if (lb.isPresent()) {

                    localBodyLabel = lb.get().getLocalbodyNameEn();

                    localbodyType = localBodyTypeRepository
                            .findById((long) lb.get().getLocalbodyType())
                            .map(LocalBodyType::getName)
                            .orElse("");
                }
            }
        }

        // ==========================================================
        // Return
        // ==========================================================

        return new KeyPlotResponse<>(
                "success",
                "Data fetched successfully",
                districtName,
                talukName,
                localbodyType,
                localBodyLabel,
                zone.getZoneNameEn(),
                responseData,
                lbCodes,
                0,
                0,
                0,
                0,
                new ArrayList<>()
        );
    }

    public List<TblWorkAllocationDTO> getWorkAllocationsByZoneId(
            Integer zoneId,
            String agriYear) {

        LocalDate agriStart =
                AgriYearUtil.getAgriYearStart(agriYear);

        LocalDate agriEnd =
                AgriYearUtil.getAgriYearEnd(agriYear);

        List<TblWorkAllocation> allocations =
                tblWorkAllocationRepository.findByZoneAndAgriYear(
                        zoneId,
                        agriStart,
                        agriEnd
                );

        return allocations.stream()
                .map(a -> {

                    TblWorkAllocationApproval approval =
                            tblWorkAllocationApprovalRepository
                                    .findById(a.getApprovalId())
                                    .orElse(null);

                    TblWorkAllocationVerification verification =
                            tblWorkAllocationVerificationRepository
                                    .findByApproval_Id(a.getApprovalId())
                                    .orElse(null);

                    String approvalStatus =
                            approval != null
                                    ? approval.getStatus()
                                    : "N/A";

                    String adminRemarks =
                            approval != null
                                    ? approval.getRemark()
                                    : "";

                    String verifiedStatus =
                            verification != null
                                    ? verification.getStatus()
                                    : "PENDING";

                    LocalDate verifiedDate =
                            verification != null &&
                                    verification.getVerifiedAt() != null
                                    ? verification.getVerifiedAt().toLocalDate()
                                    : null;

                    String verifiedBy =
                            verification != null &&
                                    verification.getVerifiedBy() != null
                                    ? verification.getVerifiedBy().toString()
                                    : null;

                    String verifiedRemarks =
                            verification != null &&
                                    verification.getRemarks() != null
                                    ? verification.getRemarks().toString()
                                    : null;


                    return new TblWorkAllocationDTO(

                            // ==============================
                            // BASIC DETAILS
                            // ==============================
                            a.getId(),
                            a.getZone().getZoneId(),
                            a.getLbcode(),


                            // ==============================
                            // VILLAGE AREA
                            // ==============================
                            a.getVillageWetArea(),
                            a.getVillageDryArea(),
                            a.getVillageTotalArea(),


                            // ==============================
                            // FOREST AREA
                            // Dry -> forestAreaA
                            // Wet -> forestAreaB
                            // ==============================
                            a.getForestAreaA(),
                            a.getForestAreaB(),


                            // ==============================
                            // PLANTATION AREA
                            // Wet -> forestAreaC
                            // Dry -> areaUnderPlant
                            // ==============================
                            a.getForestAreaC(),
                            a.getAreaUnderPlant(),


                            // ==============================
                            // OTHER AREAS
                            // Dry -> forestExcludeUnclutivate
                            // Wet -> forestExcludeNotUnclutivate
                            // ==============================
                            a.getForestExcludeUnclutivate(),
                            a.getForestExcludeNotUnclutivate(),


                            // ==============================
                            // WATER BODIES
                            // Dry -> kayalExcludeArea
                            // Wet -> otherExcludeFWet
                            // ==============================
                            a.getKayalExcludeArea(),
                            a.getOtherExcludeFWet(),


                            // ==============================
                            // CLUSTER
                            // ==============================
                            a.getTotalWetCluster(),
                            a.getTotalDryCluster(),


                            // ==============================
                            // TOTAL EARAS
                            // ==============================
                            a.getOtherExcludeFWet(),


                            // ==============================
                            // PLOTS
                            // ==============================
                            a.getNoOfPlotsWet(),
                            a.getNoOfPlotsDry(),
                            a.getNoOfPlotsTotal(),


                            // ==============================
                            // ESTIMATED AREA
                            // ==============================
                            a.getTotalAreaWet(),
                            a.getTotalAreaDry(),
                            a.getTotalAreaForEstimation(),


                            // ==============================
                            // REMARKS
                            // ==============================
                            a.getRemarks(),
                            adminRemarks,


                            // ==============================
                            // USER / DATES
                            // ==============================
                            a.getUserId(),
                            a.getCreated(),
                            a.getUpdated(),
                            a.getIsActive(),


                            // ==============================
                            // AGRI YEAR
                            // ==============================
                            a.getAgriStart(),
                            a.getAgriEnd(),
                            a.getIsEdit(),

                            agriYear,

                            // ==============================
                            // APPROVAL
                            // ==============================
                            a.getApprovalId(),
                            approvalStatus,


                            // ==============================
                            // VERIFICATION
                            // ==============================
                            verifiedStatus,
                            verifiedDate,
                            verifiedBy,
                            verifiedRemarks
                    );
                })
                .collect(Collectors.toList());
    }
    @Transactional
    public List<TblWorkAllocationDTO> saveOrSubmitWorkAllocations(
            List<TblWorkAllocationDTO> dtos,
            boolean isSubmit) {

        if (dtos == null || dtos.isEmpty()) {
            return dtos;
        }

        Integer currentZoneId = dtos.get(0).getZoneId();
        UUID currentUserId = dtos.get(0).getUserId();

        String agriYear = dtos.get(0).getAgriYear();

        LocalDate agriStart = AgriYearUtil.getAgriYearStart(agriYear);
        LocalDate agriEnd = AgriYearUtil.getAgriYearEnd(agriYear);

        TblMasterZone zone = tblMasterZoneRepository.findById(currentZoneId)
                .orElseThrow(() ->
                        new RuntimeException("Zone not found for ID: " + currentZoneId)
                );

        /*
         * ============================================
         * FIND OR CREATE APPROVAL FOR THIS YEAR
         * ============================================
         */
        TblWorkAllocationApproval approval =
                tblWorkAllocationApprovalRepository
                        .findByZoneIdAndAgriStartAndAgriEnd(
                                currentZoneId,
                                agriStart,
                                agriEnd
                        )
                        .orElseGet(() -> {

                            TblWorkAllocationApproval newApproval =
                                    new TblWorkAllocationApproval();

                            newApproval.setZoneId(currentZoneId);
                            newApproval.setCreatedAt(LocalDateTime.now());
                            newApproval.setAgriStart(agriStart);
                            newApproval.setAgriEnd(agriEnd);
                            newApproval.setIsActive(true);

                            return newApproval;
                        });

        approval.setRequestedBy(currentUserId);
        approval.setIsActive(true);

        if (isSubmit) {

            approval.setStatus("SUBMITTED");

        } else {

            if (approval.getStatus() == null ||
                    "RETURNED".equals(approval.getStatus())) {

                approval.setStatus("DRAFT");
            }
        }

        approval.setAgriStart(agriStart);
        approval.setAgriEnd(agriEnd);

        approval = tblWorkAllocationApprovalRepository.save(approval);

        Long approvalId = approval.getId();


        /*
         * ============================================
         * CREATE / RESET VERIFICATION
         * ============================================
         */
        if (isSubmit) {

            TblWorkAllocationApproval finalApproval = approval;

            TblWorkAllocationVerification verification =
                    tblWorkAllocationVerificationRepository
                            .findByApproval_Id(approvalId)
                            .orElseGet(() -> {

                                TblWorkAllocationVerification v =
                                        new TblWorkAllocationVerification();

                                v.setApproval(finalApproval);
                                v.setCreatedAt(LocalDateTime.now());
                                v.setIsActive(true);

                                return v;
                            });

            // If submitted again after investigator returned,
            // reset verification to pending.

            verification.setStatus("PENDING");
            verification.setVerifiedBy(null);
            verification.setVerifiedAt(null);
            verification.setRemarks(null);
            verification.setUpdatedAt(LocalDateTime.now());

            tblWorkAllocationVerificationRepository.save(verification);
        }


        /*
         * ============================================
         * SAVE WORK ALLOCATION ROWS
         * ============================================
         */
        for (TblWorkAllocationDTO dto : dtos) {

            TblWorkAllocation allocation =
                    tblWorkAllocationRepository
                            .findByLbcodeAndZone_ZoneIdAndAgriStartAndAgriEnd(
                                    dto.getLbcode(),
                                    dto.getZoneId(),
                                    agriStart,
                                    agriEnd
                            )
                            .orElse(new TblWorkAllocation());


            if (allocation.getId() == null) {
                allocation.setCreated(LocalDate.now());
            }


            /*
             * ============================================
             * BASIC DETAILS
             * ============================================
             */
            allocation.setApprovalId(approvalId);
            allocation.setZone(zone);
            allocation.setLbcode(dto.getLbcode());


            /*
             * ============================================
             * AREA DETAILS - TAB 1
             * ============================================
             */
            allocation.setVillageWetArea(
                    dto.getVillageWetArea()
            );

            allocation.setVillageDryArea(
                    dto.getVillageDryArea()
            );

            allocation.setVillageTotalArea(
                    dto.getVillageTotalArea()
            );


            /*
             * ============================================
             * EXCLUDED AREAS - TAB 2
             *
             * Forest Area
             * --------------------------------------------
             * Dry -> forestAreaA
             * Wet -> forestAreaB
             *
             * Plantation Area
             * --------------------------------------------
             * Wet -> forestAreaC
             * Dry -> areaUnderPlant
             *
             * Water Bodies
             * --------------------------------------------
             * Wet -> otherExcludeFWet
             * Dry -> kayalExcludeArea
             *
             * Other Areas
             * --------------------------------------------
             * Wet -> forestExcludeNotUnclutivate
             * Dry -> forestExcludeUnclutivate
             * ============================================
             */


            // --------------------------------------------
            // FOREST AREA
            // --------------------------------------------

            // Forest Area - Dry
            allocation.setForestAreaA(
                    dto.getForestAreaA()
            );

            // Forest Area - Wet
            allocation.setForestAreaB(
                    dto.getForestAreaB()
            );


            // --------------------------------------------
            // PLANTATION AREA
            // --------------------------------------------

            // Plantation Area - Wet
            allocation.setForestAreaC(
                    dto.getForestAreaC()
            );

            // Plantation Area - Dry
            allocation.setAreaUnderPlant(
                    dto.getAreaUnderPlant()
            );


            // --------------------------------------------
            // AREA OF WATER BODIES
            // --------------------------------------------

            // Water Bodies - Wet
            allocation.setOtherExcludeFWet(
                    dto.getOtherExcludeFWet()
            );

            // Water Bodies - Dry
            allocation.setKayalExcludeArea(
                    dto.getKayalExcludeArea()
            );


            // --------------------------------------------
            // OTHER AREAS
            // --------------------------------------------

            // Other Areas - Wet
            allocation.setForestExcludeNotUnclutivate(
                    dto.getForestExcludeNotUnclutivate()
            );

            // Other Areas - Dry
            allocation.setForestExcludeUnclutivate(
                    dto.getForestExcludeUnclutivate()
            );


            /*
             * ============================================
             * TOTAL EARAS AREA - TAB 3
             * ============================================
             */
            allocation.setTotalWetCluster(
                    dto.getTotalWetCluster()
            );

            allocation.setTotalDryCluster(
                    dto.getTotalDryCluster()
            );


            /*
             * ============================================
             * ESTIMATED AREA - TAB 3
             * ============================================
             */
            allocation.setTotalAreaWet(
                    dto.getTotalAreaWet()
            );

            allocation.setTotalAreaDry(
                    dto.getTotalAreaDry()
            );

            allocation.setTotalAreaForEstimation(
                    dto.getTotalAreaForEstimation()
            );


            /*
             * ============================================
             * PLOTS
             * ============================================
             */
            allocation.setNoOfPlotsWet(
                    dto.getNoOfPlotsWet()
            );

            allocation.setNoOfPlotsDry(
                    dto.getNoOfPlotsDry()
            );

            allocation.setNoOfPlotsTotal(
                    dto.getNoOfPlotsTotal()
            );


            /*
             * ============================================
             * REMARKS
             * ============================================
             */
            allocation.setRemarks(
                    dto.getRemarks()
            );


            /*
             * ============================================
             * USER / STATUS / AGRI YEAR
             * ============================================
             */
            allocation.setUserId(
                    dto.getUserId()
            );

            allocation.setUpdated(
                    LocalDate.now()
            );

            allocation.setAgriStart(
                    agriStart
            );

            allocation.setAgriEnd(
                    agriEnd
            );

            allocation.setIsActive(true);

            allocation.setIsEdit(
                    !isSubmit
            );


            /*
             * ============================================
             * SAVE
             * ============================================
             */
            tblWorkAllocationRepository.save(allocation);
        }


        /*
         * ============================================
         * RETURN
         * ============================================
         */
        return dtos;
    }


    @Transactional
    public void verify(WorkAllocationVerificationRequest request) {

        TblWorkAllocationVerification verification =
                tblWorkAllocationVerificationRepository
                        .findByApproval_Id(request.getApprovalId())
                        .orElseThrow(() ->
                                new RuntimeException("Verification not found"));

        verification.setStatus(request.getStatus());

        verification.setRemarks(request.getRemarks());

        verification.setVerifiedBy(request.getVerifiedBy());

        verification.setVerifiedAt(LocalDateTime.now());

        verification.setUpdatedAt(LocalDateTime.now());

        tblWorkAllocationVerificationRepository.save(verification);
    }

    @Transactional
    public void revokeWorkAllocation(Long approvalId, String remarks, UUID revokedBy) {
        // Find the approval

        TblWorkAllocationApproval approval = tblWorkAllocationApprovalRepository
                .findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval not found"));

        // Update status to RETURNED
        approval.setStatus("RETURNED");
        approval.setRemark(remarks);
        approval.setRevokedAt(LocalDateTime.now());
        approval.setRevokedBy(revokedBy);
        approval.setUpdatedAt(LocalDateTime.now());
        tblWorkAllocationApprovalRepository.save(approval);

        // Update all work allocations to editable
        List<TblWorkAllocation> allocations = tblWorkAllocationRepository
                .findByApprovalId(approvalId);

        for (TblWorkAllocation allocation : allocations) {
            allocation.setIsEdit(true);
            allocation.setUpdated(LocalDate.now());
            tblWorkAllocationRepository.save(allocation);
        }

        // Update verification status
        TblWorkAllocationVerification verification = tblWorkAllocationVerificationRepository
                .findByApproval_Id(approvalId)
                .orElse(null);

        if (verification != null) {
            verification.setStatus("PENDING");
            verification.setUpdatedAt(LocalDateTime.now());
            tblWorkAllocationVerificationRepository.save(verification);
        }
    }
//    public List<TblWorkAllocationDTO> saveOrSubmitWorkAllocations(
//            List<TblWorkAllocationDTO> dtos,
//            boolean isSubmit) {
//
//        if (dtos == null || dtos.isEmpty()) {
//            return dtos;
//        }
//
//        Integer currentZoneId = dtos.get(0).getZoneId();
//        UUID currentUserId = dtos.get(0).getUserId();
//
//        String agriYear = dtos.get(0).getAgriYear();
//
//        LocalDate agriStart =
//                AgriYearUtil.getAgriYearStart(agriYear);
//
//        LocalDate agriEnd =
//                AgriYearUtil.getAgriYearEnd(agriYear);
//
//        TblMasterZone zone =
//                tblMasterZoneRepository.findById(currentZoneId)
//                        .orElseThrow(() ->
//                                new RuntimeException(
//                                        "Zone not found for ID: "
//                                                + currentZoneId));
//
//        /*
//         * ============================================
//         * FIND OR CREATE APPROVAL FOR THIS YEAR
//         * ============================================
//         */
//
//        TblWorkAllocationApproval approval =
//                tblWorkAllocationApprovalRepository
//                        .findByZoneIdAndAgriStartAndAgriEnd(
//                                currentZoneId,
//                                agriStart,
//                                agriEnd
//                        )
//                        .orElseGet(() -> {
//
//                            TblWorkAllocationApproval newApproval =
//                                    new TblWorkAllocationApproval();
//
//                            newApproval.setZoneId(currentZoneId);
//                            newApproval.setCreatedAt(LocalDateTime.now());
//                            newApproval.setAgriStart(agriStart);
//                            newApproval.setAgriEnd(agriEnd);
//                            newApproval.setIsActive(true);
//
//                            return newApproval;
//                        });
//
//        approval.setRequestedBy(currentUserId);
//        approval.setIsActive(true);
//
//        if (isSubmit) {
//
//            approval.setStatus("SUBMITTED");
//
//        } else {
//
//            if (approval.getStatus() == null
//                    || "RETURNED".equals(approval.getStatus())) {
//
//                approval.setStatus("DRAFT");
//            }
//        }
//
//        approval.setAgriStart(agriStart);
//        approval.setAgriEnd(agriEnd);
//
//        approval =
//                tblWorkAllocationApprovalRepository.save(approval);
//
//        Long approvalId = approval.getId();
//
//        /*
//         * ============================================
//         * SAVE WORK ALLOCATION ROWS
//         * ============================================
//         */
//
//        for (TblWorkAllocationDTO dto : dtos) {
//
//            TblWorkAllocation allocation =
//                    tblWorkAllocationRepository
//                            .findByLbcodeAndZone_ZoneIdAndAgriStartAndAgriEnd(
//                                    dto.getLbcode(),
//                                    dto.getZoneId(),
//                                    agriStart,
//                                    agriEnd
//                            )
//                            .orElse(new TblWorkAllocation());
//
//            if (allocation.getId() == null) {
//
//                allocation.setCreated(LocalDate.now());
//            }
//
//            allocation.setApprovalId(approvalId);
//
//            allocation.setZone(zone);
//
//            allocation.setLbcode(dto.getLbcode());
//
//            allocation.setVillageWetArea(dto.getVillageWetArea());
//            allocation.setVillageDryArea(dto.getVillageDryArea());
//            allocation.setVillageTotalArea(dto.getVillageTotalArea());
//
//            allocation.setForestAreaA(dto.getForestAreaA());
//            allocation.setForestAreaB(dto.getForestAreaB());
//            allocation.setForestAreaC(dto.getForestAreaC());
//
//            allocation.setAreaUnderPlant(dto.getAreaUnderPlant());
//
//            allocation.setForestExcludeUnclutivate(
//                    dto.getForestExcludeUnclutivate());
//
//            allocation.setForestExcludeNotUnclutivate(
//                    dto.getForestExcludeNotUnclutivate());
//
//            allocation.setKayalExcludeArea(
//                    dto.getKayalExcludeArea());
//
//            allocation.setOtherExcludeFWet(
//                    dto.getOtherExcludeFWet());
//
//            allocation.setOtherExcludedFDry(
//                    dto.getOtherExcludedFDry());
//
//            allocation.setOtherExcludeFTotal(
//                    dto.getOtherExcludeFTotal());
//
//            allocation.setNoOfPlotsWet(
//                    dto.getNoOfPlotsWet());
//
//            allocation.setNoOfPlotsDry(
//                    dto.getNoOfPlotsDry());
//
//            allocation.setNoOfPlotsTotal(
//                    dto.getNoOfPlotsTotal());
//
//            allocation.setTotalAreaWet(
//                    dto.getTotalAreaWet());
//
//            allocation.setTotalAreaDry(
//                    dto.getTotalAreaDry());
//
//            allocation.setTotalAreaForEstimation(
//                    dto.getTotalAreaForEstimation());
//
//            allocation.setRemarks(dto.getRemarks());
//
//            allocation.setUserId(dto.getUserId());
//
//            allocation.setUpdated(LocalDate.now());
//
//            allocation.setAgriStart(agriStart);
//            allocation.setAgriEnd(agriEnd);
//
//            allocation.setIsActive(true);
//
//            // Draft = editable
//            // Submit = not editable
//            allocation.setIsEdit(!isSubmit);
//
//            tblWorkAllocationRepository.save(allocation);
//        }
//
//        return dtos;
//    }
}
