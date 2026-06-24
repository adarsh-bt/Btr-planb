package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.RequestsDTOs.TblWorkAllocationDTO;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import cdti.aidea.earas.model.Btr_models.TblWorkAllocation;
import cdti.aidea.earas.model.Btr_models.TblWorkAllocationApproval;
import cdti.aidea.earas.repository.Btr_repo.TblMasterZoneRepository;
import cdti.aidea.earas.repository.Btr_repo.TblWorkAllocationApprovalRepository;
import cdti.aidea.earas.repository.Btr_repo.TblWorkAllocationRepository;
import cdti.aidea.earas.utils.AgriYearUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

                    String status = tblWorkAllocationApprovalRepository
                            .findById(a.getApprovalId())
                            .map(TblWorkAllocationApproval::getStatus)
                            .orElse("N/A");
                    String remarks = tblWorkAllocationApprovalRepository.findById(a.getApprovalId())
                            .map(TblWorkAllocationApproval::getRemark)
                            .orElse("");

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
                            remarks,
                            a.getUserId(),
                            a.getCreated(),
                            a.getUpdated(),
                            a.getIsActive(),
                            a.getAgriStart(),
                            a.getAgriEnd(),
                            a.getIsEdit(),
                            null,
                            a.getApprovalId(),
                            status
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

        LocalDate agriStart =
                AgriYearUtil.getAgriYearStart(agriYear);

        LocalDate agriEnd =
                AgriYearUtil.getAgriYearEnd(agriYear);

        TblMasterZone zone =
                tblMasterZoneRepository.findById(currentZoneId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Zone not found for ID: "
                                                + currentZoneId));

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

            if (approval.getStatus() == null
                    || "RETURNED".equals(approval.getStatus())) {

                approval.setStatus("DRAFT");
            }
        }

        approval.setAgriStart(agriStart);
        approval.setAgriEnd(agriEnd);

        approval =
                tblWorkAllocationApprovalRepository.save(approval);

        Long approvalId = approval.getId();

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

            allocation.setApprovalId(approvalId);

            allocation.setZone(zone);

            allocation.setLbcode(dto.getLbcode());

            allocation.setVillageWetArea(dto.getVillageWetArea());
            allocation.setVillageDryArea(dto.getVillageDryArea());
            allocation.setVillageTotalArea(dto.getVillageTotalArea());

            allocation.setForestAreaA(dto.getForestAreaA());
            allocation.setForestAreaB(dto.getForestAreaB());
            allocation.setForestAreaC(dto.getForestAreaC());

            allocation.setAreaUnderPlant(dto.getAreaUnderPlant());

            allocation.setForestExcludeUnclutivate(
                    dto.getForestExcludeUnclutivate());

            allocation.setForestExcludeNotUnclutivate(
                    dto.getForestExcludeNotUnclutivate());

            allocation.setKayalExcludeArea(
                    dto.getKayalExcludeArea());

            allocation.setOtherExcludeFWet(
                    dto.getOtherExcludeFWet());

            allocation.setOtherExcludedFDry(
                    dto.getOtherExcludedFDry());

            allocation.setOtherExcludeFTotal(
                    dto.getOtherExcludeFTotal());

            allocation.setNoOfPlotsWet(
                    dto.getNoOfPlotsWet());

            allocation.setNoOfPlotsDry(
                    dto.getNoOfPlotsDry());

            allocation.setNoOfPlotsTotal(
                    dto.getNoOfPlotsTotal());

            allocation.setTotalAreaWet(
                    dto.getTotalAreaWet());

            allocation.setTotalAreaDry(
                    dto.getTotalAreaDry());

            allocation.setTotalAreaForEstimation(
                    dto.getTotalAreaForEstimation());

            allocation.setRemarks(dto.getRemarks());

            allocation.setUserId(dto.getUserId());

            allocation.setUpdated(LocalDate.now());

            allocation.setAgriStart(agriStart);
            allocation.setAgriEnd(agriEnd);

            allocation.setIsActive(true);

            // Draft = editable
            // Submit = not editable
            allocation.setIsEdit(!isSubmit);

            tblWorkAllocationRepository.save(allocation);
        }

        return dtos;
    }
}
