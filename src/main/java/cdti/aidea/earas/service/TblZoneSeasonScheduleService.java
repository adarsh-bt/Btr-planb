package cdti.aidea.earas.service;


import cdti.aidea.earas.contract.RequestsDTOs.ZoneIdFrameIdRequest;
import cdti.aidea.earas.contract.Response.TblZoneSeasonScheduleDTO;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import cdti.aidea.earas.model.Btr_models.TblMasterFrame;
import cdti.aidea.earas.model.Btr_models.TblSeasonMaster;
import cdti.aidea.earas.model.Btr_models.TblZoneSeasonSchedule;
import cdti.aidea.earas.repository.Btr_repo.TblMasterFrameRepository;
import cdti.aidea.earas.repository.Btr_repo.TblMasterZoneRepository;
import cdti.aidea.earas.repository.Btr_repo.TblSeasonMasterRepository;
import cdti.aidea.earas.repository.Btr_repo.TblZoneSeasonScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TblZoneSeasonScheduleService {
    private final TblZoneSeasonScheduleRepository scheduleRepo;
    private final TblMasterZoneRepository zoneRepo;
    private final TblSeasonMasterRepository  seasonRepo;
    private final TblMasterFrameRepository masterFrameRepo;

    public TblZoneSeasonScheduleDTO createSchedule(TblZoneSeasonScheduleDTO dto) {
        TblZoneSeasonSchedule entity;

        // 🔹 If scheduleId exists → update; otherwise create
        if (dto.getScheduleId() != null) {
            // Update logic
            entity = scheduleRepo.findById(dto.getScheduleId())
                    .orElseThrow(() -> new RuntimeException("Schedule not found for update"));

            // Update related zone if provided
            if (dto.getZoneId() != null) {
                TblMasterZone zone = zoneRepo.findById(dto.getZoneId())
                        .orElseThrow(() -> new RuntimeException("Zone not found"));
                entity.setZone(zone);
            }

            // Update related season if provided
            if (dto.getSeasonId() != null) {
                TblSeasonMaster season = seasonRepo.findById(dto.getSeasonId())
                        .orElseThrow(() -> new RuntimeException("Season not found"));
                entity.setSeason(season);
            }
            // Assign frame if provided
            if (dto.getFrameId() != null) {
                TblMasterFrame frame = masterFrameRepo.findById(dto.getFrameId())
                        .orElseThrow(() -> new RuntimeException("Frame not found"));
                entity.setFrame(frame);
            }else {
                throw new RuntimeException("Frame must not be null");
            }


            // Update remaining fields
            //  entity.setClusterType(dto.getClusterType() != null ? dto.getClusterType() : entity.getClusterType());
            entity.setStartDate(dto.getStartDate() != null ? dto.getStartDate() : entity.getStartDate());
            entity.setEndDate(dto.getEndDate() != null ? dto.getEndDate() : entity.getEndDate());
            entity.setExtendedDate(dto.getExtendedDate() != null ? dto.getExtendedDate() : entity.getExtendedDate());
            entity.setYear(dto.getYear() != null ? dto.getYear() : entity.getYear());
            entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : entity.getIsActive());
            entity.setRemark(dto.getRemark() != null ? dto.getRemark() : entity.getRemark());

            // Keep existing UUID (don’t overwrite)
            if (entity.getUuid() == null) {
                entity.setUuid(  UUID.randomUUID());
            }

        } else {
            // Create new schedule
            entity = new TblZoneSeasonSchedule();

            TblMasterZone zone = zoneRepo.findById(dto.getZoneId())
                    .orElseThrow(() -> new RuntimeException("Zone not found"));
            TblSeasonMaster season = seasonRepo.findById(dto.getSeasonId())
                    .orElseThrow(() -> new RuntimeException("Season not found"));
            TblMasterFrame frame = masterFrameRepo.findById(dto.getFrameId())
                    .orElseThrow(() -> new RuntimeException("Frame not found")); // ✅ Added here


            entity.setZone(zone);
            entity.setSeason(season);
            entity.setFrame(frame); // ✅ Added line
            //  entity.setClusterType(dto.getClusterType());
            entity.setStartDate(dto.getStartDate());
            entity.setEndDate(dto.getEndDate());
            entity.setExtendedDate(dto.getExtendedDate());
            entity.setYear(dto.getYear());
            entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
            entity.setRemark(dto.getRemark());
            entity.setUuid(UUID.randomUUID()); // ensure uuid never null
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
        }


        TblZoneSeasonSchedule saved = scheduleRepo.save(entity);
        dto.setScheduleId(saved.getScheduleId());
        dto.setUuid(saved.getUuid());
        return dto;
    }

    public List<TblZoneSeasonScheduleDTO> getAllSchedules() {
        return scheduleRepo.findAll().stream().map(entity -> {
            TblZoneSeasonScheduleDTO dto = new TblZoneSeasonScheduleDTO();
            dto.setScheduleId(entity.getScheduleId());
            dto.setZoneId(entity.getZone().getZoneId());
            dto.setSeasonId(entity.getSeason().getId());
            //  dto.setClusterType(entity.getClusterType());
            dto.setStartDate(entity.getStartDate());
            dto.setEndDate(entity.getEndDate());
            dto.setExtendedDate(entity.getExtendedDate());
            dto.setYear(entity.getYear());
            dto.setUuid(entity.getUuid());
            dto.setIsActive(entity.getIsActive());
            dto.setRemark(entity.getRemark());
            dto.setZoneNameEn(entity.getZone() != null ? entity.getZone().getZoneNameEn() : null);
            dto.setSeasonName(entity.getSeason() != null ? entity.getSeason().getSeasonName() : null);
            dto.setFrameId(entity.getFrame()!= null ? entity.getFrame().getFrameId() : null);
            dto.setFrameName(entity.getFrame() != null ? entity.getFrame().getFrame() : null);
            dto.setCreatedAt(entity.getCreatedAt());
            dto.setUpdatedAt(entity.getUpdatedAt());

            return dto;
        }).collect(Collectors.toList());
    }
    // ✅ ✨ NEW PAGINATION LOGIC ADDED HERE ✨
    public List<TblZoneSeasonScheduleDTO> getPaginatedSchedules(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<TblZoneSeasonSchedule> pagedResult = scheduleRepo.findAll(pageable);

        return pagedResult.getContent().stream().map(entity -> {
            TblZoneSeasonScheduleDTO dto = new TblZoneSeasonScheduleDTO();
            dto.setScheduleId(entity.getScheduleId());
            dto.setZoneId(entity.getZone().getZoneId());
            dto.setSeasonId(entity.getSeason().getId());
            dto.setStartDate(entity.getStartDate());
            dto.setEndDate(entity.getEndDate());
            dto.setExtendedDate(entity.getExtendedDate());
            dto.setYear(entity.getYear());
            dto.setUuid(entity.getUuid());
            dto.setIsActive(entity.getIsActive());
            dto.setRemark(entity.getRemark());
            dto.setZoneNameEn(entity.getZone() != null ? entity.getZone().getZoneNameEn() : null);
            dto.setSeasonName(entity.getSeason() != null ? entity.getSeason().getSeasonName() : null);
            dto.setFrameId(entity.getFrame() != null ? entity.getFrame().getFrameId() : null);
            dto.setFrameName(entity.getFrame() != null ? entity.getFrame().getFrame() : null);
            dto.setCreatedAt(entity.getCreatedAt());
            dto.setUpdatedAt(entity.getUpdatedAt());
            return dto;
        }).collect(Collectors.toList());
    }
    // ✅ New method: Get schedules by Zone ID and Frame ID using request DTO
    public List<TblZoneSeasonScheduleDTO> getSchedulesByZoneAndFrame(ZoneIdFrameIdRequest request) {
        Integer zoneId = request.getZoneId();
        Long frameId = request.getFrameId();

        List<TblZoneSeasonSchedule> schedules = scheduleRepo.findByZoneZoneIdAndFrameFrameId(zoneId, frameId);

        return schedules.stream().map(entity -> {
            TblZoneSeasonScheduleDTO dto = new TblZoneSeasonScheduleDTO();
            dto.setScheduleId(entity.getScheduleId());
            dto.setZoneId(entity.getZone().getZoneId());
            dto.setSeasonId(entity.getSeason().getId());
            dto.setStartDate(entity.getStartDate());
            dto.setEndDate(entity.getEndDate());
            dto.setExtendedDate(entity.getExtendedDate());
            dto.setYear(entity.getYear());
            dto.setUuid(entity.getUuid());
            dto.setIsActive(entity.getIsActive());
            dto.setRemark(entity.getRemark());
            dto.setZoneNameEn(entity.getZone() != null ? entity.getZone().getZoneNameEn() : null);
            dto.setSeasonName(entity.getSeason() != null ? entity.getSeason().getSeasonName() : null);
            dto.setFrameId(entity.getFrame() != null ? entity.getFrame().getFrameId() : null);
            dto.setFrameName(entity.getFrame() != null ? entity.getFrame().getFrame() : null);
            dto.setCreatedAt(entity.getCreatedAt());
            dto.setUpdatedAt(entity.getUpdatedAt());
            return dto;
        }).collect(Collectors.toList());
    }
}
