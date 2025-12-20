package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.Response.TblZoneSeasonBulkScheduleADTO;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import cdti.aidea.earas.model.Btr_models.TblMasterFrame;
import cdti.aidea.earas.model.Btr_models.TblSeasonMaster;
import cdti.aidea.earas.model.Btr_models.TblZoneSeasonSchedule;
import cdti.aidea.earas.repository.Btr_repo.TblMasterFrameRepository;
import cdti.aidea.earas.repository.Btr_repo.TblMasterZoneRepository;
import cdti.aidea.earas.repository.Btr_repo.TblSeasonMasterRepository;
import cdti.aidea.earas.repository.Btr_repo.TblZoneSeasonScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TblZoneSeasonBulkScheduleService {
    private final TblZoneSeasonScheduleRepository scheduleRepo;
    private final TblMasterZoneRepository zoneRepo;
    private final TblSeasonMasterRepository seasonRepo;
    private final TblMasterFrameRepository frameRepo;

    public void applyScheduleByOffice(TblZoneSeasonBulkScheduleADTO dto) {

        TblSeasonMaster season = seasonRepo.findById(dto.getSeasonId())
                .orElseThrow(() -> new RuntimeException("Season not found"));

        TblMasterFrame frame = frameRepo.findById(3L)
                .orElseThrow(() -> new RuntimeException("Frame not found"));

        List<TblMasterZone> zones = resolveZones(dto.getOfficeType(), dto.getOfficeId());

        for (TblMasterZone zone : zones) {

            // 🔴 deactivate existing active schedules (zone + season + frame)
            List<TblZoneSeasonSchedule> activeSchedules =
                    scheduleRepo.findByZoneZoneIdAndSeasonIdAndFrameFrameIdAndIsActiveTrue(
                            zone.getZoneId(),
                            season.getId(),
                            frame.getFrameId()
                    );

            for (TblZoneSeasonSchedule old : activeSchedules) {
                old.setIsActive(false);
                old.setUpdatedAt(LocalDateTime.now());
                scheduleRepo.save(old);
            }

            // 🟢 create new schedule
            TblZoneSeasonSchedule entity = new TblZoneSeasonSchedule();
            entity.setZone(zone);
            entity.setSeason(season);
            entity.setFrame(frame);

            applyDateLogic(entity, dto, season);

            entity.setYear(dto.getYear());
            entity.setRemark(dto.getRemark());
            entity.setUserId(UUID.randomUUID());
            entity.setIsActive(true);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());

            scheduleRepo.save(entity);
        }
    }

    // ================= DATE LOGIC =================
    private void applyDateLogic(
            TblZoneSeasonSchedule entity,
            TblZoneSeasonBulkScheduleADTO dto,
            TblSeasonMaster season
    ) {

        LocalDate defaultStart = season.getDefaultStart();
        LocalDate defaultEnd = season.getDefaultEnd();

        if (dto.getStartDate() != null && dto.getExtendedDate() == null) {
            entity.setStartDate(dto.getStartDate());
            entity.setEndDate(defaultEnd);
            entity.setExtendedDate(defaultEnd);

        } else if (dto.getStartDate() == null && dto.getExtendedDate() != null) {
            entity.setStartDate(defaultStart);
            entity.setEndDate(defaultEnd);
            entity.setExtendedDate(dto.getExtendedDate());

        } else if (dto.getStartDate() != null && dto.getExtendedDate() != null) {
            entity.setStartDate(dto.getStartDate());
            entity.setEndDate(defaultEnd);
            entity.setExtendedDate(dto.getExtendedDate());

        } else {
            throw new RuntimeException("Either startDate or extendedDate must be provided");
        }
    }

    // ================= ZONE RESOLUTION =================
    private List<TblMasterZone> resolveZones(Integer officeType, Integer officeId) {

        if (officeType == null) {
            throw new RuntimeException("Office type is required");
        }

        switch (officeType) {
            case 1: // STATE
                return zoneRepo.findAll();

            case 2: // DISTRICT
                Integer distId = officeId;
                return zoneRepo.findByDistId(distId);

            case 3: // TALUK
                return zoneRepo.findByDesTalukId(officeId);

            default:
                throw new RuntimeException("Invalid office type");
        }
    }
}


