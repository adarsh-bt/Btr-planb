package cdti.aidea.earas.service;


import cdti.aidea.earas.contract.RequestsDTOs.ZoneIdFrameIdRequest;
import cdti.aidea.earas.contract.Response.TblZoneSeasonScheduleDTO;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import cdti.aidea.earas.model.Btr_models.TblMasterFrame;
import cdti.aidea.earas.model.Btr_models.TblSeasonMaster;
import cdti.aidea.earas.model.Btr_models.TblZoneSeasonSchedule;
import cdti.aidea.earas.repository.Btr_repo.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TblZoneSeasonScheduleService {
    private final TblZoneSeasonScheduleRepository scheduleRepo;
    private final TblMasterZoneRepository zoneRepo;
    private final TblSeasonMasterRepository seasonRepo;
    private final TblMasterFrameRepository masterFrameRepo;

    // ================= CREATE / UPDATE =================
    public TblZoneSeasonScheduleDTO createSchedule(TblZoneSeasonScheduleDTO dto) {
        TblZoneSeasonSchedule entity;

        // ================= UPDATE (VERSIONING) =================
//
//        if (dto.getFrameId() == null) {
//            throw new RuntimeException("Frame Id is mandatory");
//        }
        if (dto.getZoneId() != null && dto.getSeasonId() != null
                && scheduleRepo.existsByZoneZoneIdAndSeasonIdAndIsActiveTrue(
                dto.getZoneId(), dto.getSeasonId())) {

            TblZoneSeasonSchedule oldEntity =
                    scheduleRepo.findByZoneZoneIdAndSeasonIdAndIsActiveTrue(
                                    dto.getZoneId(),
                                    dto.getSeasonId()
                            ).stream().findFirst()
                            .orElseThrow(() ->
                                    new RuntimeException("No active schedule found for this Zone and Season")
                            );
// deactivate old
            oldEntity.setIsActive(false);
            oldEntity.setUpdatedAt(LocalDateTime.now());
            scheduleRepo.save(oldEntity);

            entity = new TblZoneSeasonSchedule();
            entity.setZone(oldEntity.getZone());
            entity.setSeason(oldEntity.getSeason());

            TblMasterFrame frame = masterFrameRepo.findById(3L)
                    .orElseThrow(() -> new RuntimeException("Frame not found"));
            entity.setFrame(frame);

            TblSeasonMaster season = oldEntity.getSeason();
            LocalDate defaultStart = season.getDefaultStart();
            LocalDate defaultEnd = season.getDefaultEnd();

            if (dto.getStartDate() != null && dto.getExtendedDate() == null) {
                entity.setStartDate(dto.getStartDate());
                entity.setEndDate(defaultEnd);
                entity.setExtendedDate(null);
            } else if (dto.getStartDate() == null && dto.getExtendedDate() != null) {
                entity.setStartDate(defaultStart);
                entity.setEndDate(defaultEnd);
                entity.setExtendedDate(dto.getExtendedDate());
            } else if (dto.getStartDate() != null && dto.getExtendedDate() != null) {
                entity.setStartDate(dto.getStartDate());
                entity.setEndDate(defaultEnd);
                entity.setExtendedDate(dto.getExtendedDate());
            } else {
                throw new RuntimeException("Either Start Date or Extended Date must be provided");
            }

            entity.setYear(dto.getYear());
            entity.setRemark(dto.getRemark());
            entity.setUserId(UUID.randomUUID());
            entity.setIsActive(true);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());

        }
        // ================= CREATE =================
        else {

            entity = new TblZoneSeasonSchedule();

            TblMasterZone zone = zoneRepo.findById(dto.getZoneId())
                    .orElseThrow(() -> new RuntimeException("Zone not found"));

            TblSeasonMaster season = seasonRepo.findById(dto.getSeasonId())
                    .orElseThrow(() -> new RuntimeException("Season not found"));

            entity.setZone(zone);
            entity.setSeason(season);

            //if (dto.getFrameId() != null) {
                TblMasterFrame frame = masterFrameRepo.findById(3L)
                        .orElseThrow(() -> new RuntimeException("Frame not found"));
                entity.setFrame(frame);
           // }

            LocalDate defaultStart = season.getDefaultStart();
            LocalDate defaultEnd = season.getDefaultEnd();

            if (dto.getStartDate() != null && dto.getExtendedDate() == null) {
                entity.setStartDate(dto.getStartDate());
                entity.setEndDate(defaultEnd);
                entity.setExtendedDate(null);
            } else if (dto.getStartDate() == null && dto.getExtendedDate() != null) {
                entity.setStartDate(defaultStart);
                entity.setEndDate(defaultEnd);
                entity.setExtendedDate(dto.getExtendedDate());
            } else if (dto.getStartDate() != null && dto.getExtendedDate() != null) {
                entity.setStartDate(dto.getStartDate());
                entity.setEndDate(defaultEnd);
                entity.setExtendedDate(dto.getExtendedDate());
            } else {
                throw new RuntimeException("Either Start Date or Extended Date must be provided");
            }

            entity.setYear(dto.getYear());
            entity.setRemark(dto.getRemark());
            entity.setUserId(UUID.randomUUID());
            entity.setIsActive(true);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
        }

        // ================= DEACTIVATE OLD BY ZONE + SEASON =================
        List<TblZoneSeasonSchedule> activeSchedules =
                scheduleRepo.findByZoneZoneIdAndSeasonIdAndIsActiveTrue(
                        entity.getZone().getZoneId(),
                        entity.getSeason().getId()
                );

////        for (TblZoneSeasonSchedule old : activeSchedules) {
////            old.setIsActive(false);
////            old.setUpdatedAt(LocalDateTime.now());
////            scheduleRepo.save(old);
////        }
//        for (TblZoneSeasonSchedule old : activeSchedules) {
//            if (!old.getScheduleId().equals(entity.getScheduleId())) {
//                old.setIsActive(false);
//                old.setUpdatedAt(LocalDateTime.now());
//                scheduleRepo.save(old);
//            }
//        }


        TblZoneSeasonSchedule saved = scheduleRepo.save(entity);
        dto.setScheduleId(saved.getScheduleId());
        dto.setUuid(saved.getUserId());

        return dto;
    }

    // ================= GET ALL =================
    public List<TblZoneSeasonScheduleDTO> getAllSchedules() {
        return scheduleRepo.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // ================= PAGINATION =================
    public List<TblZoneSeasonScheduleDTO> getPaginatedSchedules(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return scheduleRepo.findAll(pageable).getContent()
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // ================= ZONE + FRAME =================
    public List<TblZoneSeasonScheduleDTO> getSchedulesByZoneAndFrame(ZoneIdFrameIdRequest request) {
        return scheduleRepo.findByZoneZoneIdAndFrameFrameId(
                request.getZoneId(), request.getFrameId()
        ).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // ================= MAPPER =================
    private TblZoneSeasonScheduleDTO mapToDto(TblZoneSeasonSchedule entity) {
        TblZoneSeasonScheduleDTO dto = new TblZoneSeasonScheduleDTO();
        dto.setScheduleId(entity.getScheduleId());
        dto.setZoneId(entity.getZone().getZoneId());
        dto.setSeasonId(entity.getSeason().getId());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setExtendedDate(entity.getExtendedDate());
        dto.setYear(entity.getYear());
        dto.setUuid(entity.getUserId());
        dto.setIsActive(entity.getIsActive());
        dto.setRemark(entity.getRemark());
        dto.setZoneNameEn(entity.getZone().getZoneNameEn());
        dto.setSeasonName(entity.getSeason().getSeasonName());
        return dto;
    }
}
