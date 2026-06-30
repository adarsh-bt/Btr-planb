package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.RequestsDTOs.TblWorkAllocationDTO;
import cdti.aidea.earas.contract.RequestsDTOs.WorkAllocationVerificationRequest;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import cdti.aidea.earas.model.Btr_models.TblWorkAllocation;
import cdti.aidea.earas.model.Btr_models.TblWorkAllocationApproval;
import cdti.aidea.earas.model.Btr_models.TblWorkAllocationVerification;
import cdti.aidea.earas.repository.Btr_repo.TblMasterZoneRepository;
import cdti.aidea.earas.repository.Btr_repo.TblWorkAllocationApprovalRepository;
import cdti.aidea.earas.repository.Btr_repo.TblWorkAllocationRepository;
import cdti.aidea.earas.repository.Btr_repo.TblWorkAllocationVerificationRepository;
import cdti.aidea.earas.utils.AgriYearUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    public List<TblWorkAllocationDTO> getWorkAllocationsByZoneId(Integer zoneId,String agriYear) {

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
                            approval != null ? approval.getStatus() : "N/A";

                    String adminRemarks =
                            approval != null ? approval.getRemark() : "";

                    String verifiedStatus =
                            verification != null ? verification.getStatus() : "PENDING";

                    LocalDate verifiedDate =
                            verification != null && verification.getVerifiedAt() != null
                                    ? verification.getVerifiedAt().toLocalDate()
                                    : null;

                    String verifiedBy =
                            verification != null && verification.getVerifiedBy() != null
                                    ? verification.getVerifiedBy().toString()
                                    : null;

                    return new TblWorkAllocationDTO(

                            a.getId(),
                            a.getZone().getZoneId(),
                            a.getLbcode(),

                            a.getVillageWetArea(),
                            a.getVillageDryArea(),
                            a.getVillageTotalArea(),

                            a.getForestAreaA(),
                            a.getForestAreaB(),
                            a.getForestAreaC(),

                            a.getAreaUnderPlant(),
                            a.getForestExcludeUnclutivate(),
                            a.getForestExcludeNotUnclutivate(),
                            a.getKayalExcludeArea(),

                            a.getOtherExcludeFWet(),
                            a.getOtherExcludedFDry(),
                            a.getOtherExcludeFTotal(),

                            a.getNoOfPlotsWet(),
                            a.getNoOfPlotsDry(),
                            a.getNoOfPlotsTotal(),

                            a.getTotalAreaWet(),
                            a.getTotalAreaDry(),
                            a.getTotalAreaForEstimation(),

                            a.getRemarks(),
                            adminRemarks,

                            a.getUserId(),
                            a.getCreated(),
                            a.getUpdated(),
                            a.getIsActive(),

                            a.getAgriStart(),
                            a.getAgriEnd(),

                            a.getIsEdit(),

                            null, // agriYear

                            a.getApprovalId(),

                            approvalStatus,

                            verifiedStatus,
                            verifiedDate,
                            verifiedBy
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
                .orElseThrow(() -> new RuntimeException("Zone not found for ID: " + currentZoneId));

        /*
         * ============================================
         * FIND OR CREATE APPROVAL FOR THIS YEAR
         * ============================================
         */
        TblWorkAllocationApproval approval = tblWorkAllocationApprovalRepository
                .findByZoneIdAndAgriStartAndAgriEnd(currentZoneId, agriStart, agriEnd)
                .orElseGet(() -> {
                    TblWorkAllocationApproval newApproval = new TblWorkAllocationApproval();
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
            if (approval.getStatus() == null || "RETURNED".equals(approval.getStatus())) {
                approval.setStatus("DRAFT");
            }
        }

        approval.setAgriStart(agriStart);
        approval.setAgriEnd(agriEnd);
        approval = tblWorkAllocationApprovalRepository.save(approval);

        Long approvalId = approval.getId();

        /*
         * ============================================
         * SAVE WORK ALLOCATION ROWS
         * ============================================
         */
        /*
         * ============================================
         * CREATE / UPDATE VERIFICATION
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
        for (TblWorkAllocationDTO dto : dtos) {
            TblWorkAllocation allocation = tblWorkAllocationRepository
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

            allocation.setApprovalId(approvalId);
            allocation.setZone(zone);
            allocation.setLbcode(dto.getLbcode());

            // ===== AREA DETAILS (Tab 1) =====
            allocation.setVillageWetArea(dto.getVillageWetArea());
            allocation.setVillageDryArea(dto.getVillageDryArea());
            allocation.setVillageTotalArea(dto.getVillageTotalArea());

            // ===== EXCLUDED AREAS (Tab 2 - Forest Areas, Plantation Area, Water Bodies, Other Areas) =====
            // Forest Areas - using forestAreaA as the single forest area field
            allocation.setForestAreaA(dto.getForestAreaA());
            // Set forestAreaB and forestAreaC to null or 0 since they're not in UI
            allocation.setForestAreaB(BigDecimal.ZERO);
            allocation.setForestAreaC(BigDecimal.ZERO);

            // Plantation Area - using areaUnderPlant
            allocation.setAreaUnderPlant(dto.getAreaUnderPlant());

            // Area of Water Bodies - using kayalExcludeArea
            allocation.setKayalExcludeArea(dto.getKayalExcludeArea());

            // Other Areas (Military Barracks, SEZ, Airport etc.) - using plantation_under
            allocation.setForestExcludeUnclutivate(dto.getForestExcludeUnclutivate());
            // Set plantation_not_under to null or 0 since not in UI
            allocation.setForestExcludeNotUnclutivate(BigDecimal.ZERO);

            // ===== TOTAL EARAS AREA AND ESTIMATED AREA (Tab 3) =====
            // EARAS Area
            allocation.setOtherExcludeFWet(dto.getOtherExcludeFWet());      // Wet EARAS
            allocation.setOtherExcludedFDry(dto.getOtherExcludedFDry());    // Dry EARAS
            allocation.setOtherExcludeFTotal(dto.getOtherExcludeFTotal());  // Total EARAS

            // Estimated Area
            allocation.setTotalAreaWet(dto.getTotalAreaWet());              // Wet Estimated
            allocation.setTotalAreaDry(dto.getTotalAreaDry());              // Dry Estimated
            allocation.setTotalAreaForEstimation(dto.getTotalAreaForEstimation()); // Total Estimated

            // Plots fields - not in UI, set to 0 or null
            allocation.setNoOfPlotsWet(dto.getNoOfPlotsWet());
            allocation.setNoOfPlotsDry(dto.getNoOfPlotsDry());
            allocation.setNoOfPlotsTotal(dto.getNoOfPlotsTotal());

            // Remarks
            allocation.setRemarks(dto.getRemarks());

            allocation.setUserId(dto.getUserId());
            allocation.setUpdated(LocalDate.now());
            allocation.setAgriStart(agriStart);
            allocation.setAgriEnd(agriEnd);
            allocation.setIsActive(true);
            allocation.setIsEdit(!isSubmit);

            tblWorkAllocationRepository.save(allocation);
        }

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
