package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.RequestsDTOs.ClusterLimitRequest;
import cdti.aidea.earas.contract.RequestsDTOs.KeyplotsLimitLogRequest;
import cdti.aidea.earas.contract.Response.KeyplotsLimitLogResponse;
import cdti.aidea.earas.model.Btr_models.ClusterLimitLog;
import cdti.aidea.earas.model.Btr_models.KeyplotsLimitLog;
import cdti.aidea.earas.repository.Btr_repo.ClusterLimitLogRepository;
import cdti.aidea.earas.repository.Btr_repo.KeyplotsLimitLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class AdminManage {

    private final KeyplotsLimitLogRepository repository;
    private final ClusterLimitLogRepository clusterLimitLogRepository;


    public List<KeyplotsLimitLogResponse> getAllKeyplots() {
        List<KeyplotsLimitLog> entities = repository.findAll();

        return entities.stream()
                .map(entity -> new KeyplotsLimitLogResponse(
                        entity.getId(),
                        entity.getKeyplotsLimit(),
                        entity.getIsEdited(),
                        entity.getIsActive(),
                        entity.getAddedBy(),
                        entity.getEditPermitter(),
                        entity.getRemarks(),
                        entity.getAgriStartYear(),
                        entity.getAgriEndYear()
                ))
                .collect(Collectors.toList());
    }


    public KeyplotsLimitLog saveOrUpdateKeyplotsLimit(KeyplotsLimitLogRequest request) {
        KeyplotsLimitLog log;

        // === UPDATE PATH ===
        if (request.getId() != null) {
            log = repository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Record not found with ID: " + request.getId()));

            // ❗ Only allow edit if marked editable
            if (!Boolean.TRUE.equals(log.getIsEdited())) {
                throw new IllegalStateException("Edit not allowed. This record is not editable.");
            }

            // ❗ Editor (addedBy) must not be same as editPermitter (admin who allowed edit)
            if (request.getAddedBy() == null || request.getAddedBy().equals(log.getEditPermitter())) {
                throw new IllegalStateException("Edit not allowed by admin. Another user must perform the edit.");
            }

            // ✅ Update allowed
            log.setKeyplotsLimit(request.getKeyplotsLimit());
            log.setRemarks(request.getRemarks());
            log.setUpdatedAt(LocalDateTime.now());

        } else {
            // === CREATE PATH ===
            LocalDate agriStart = LocalDate.now();
            LocalDate agriEnd = agriStart.plusYears(1).minusDays(1);

            // ❗ Prevent duplicate active agri year
            boolean exists = repository.existsByAgriStartYearAndIsActive(agriStart, true);
            if (exists) {
                throw new IllegalStateException("A record already exists for the current agri year: " + agriStart);
            }

            log = new KeyplotsLimitLog();
            log.setKeyplotsLimit(request.getKeyplotsLimit());
            log.setIsEdited(request.getIsEdited() != null ? request.getIsEdited() : false);
//            log.setEditPermitter(request.getEditPermitter());
            log.setAddedBy(request.getAddedBy());
            log.setRemarks(request.getRemarks());
            log.setAgriStartYear(agriStart);
            log.setAgriEndYear(agriEnd);
            log.setIsActive(true);

            log.setCreatedAt(LocalDateTime.now());
            log.setUpdatedAt(LocalDateTime.now());
        }

        return repository.save(log);
    }

    @Transactional
    public ClusterLimitLog saveOrUpdateClusterLimit(ClusterLimitRequest request) {
        ClusterLimitLog log;

        if (request.getId() != null) {
            // update path unchanged except do not flip inActive unless explicitly intended
            log = clusterLimitLogRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Record not found with ID: " + request.getId()));

            if (!Boolean.TRUE.equals(log.getIsEdited())) {
                throw new IllegalStateException("Edit not allowed. This record is not editable.");
            }
            if (request.getAddedBy() == null || request.getAddedBy().equals(log.getEditPermitter())) {
                throw new IllegalStateException("Edit not allowed by admin. Another user must perform the edit.");
            }

            log.setClusterMin(request.getClusterMin());
            log.setClusterMax(request.getClusterMax());
            log.setRemarks(request.getRemarks());

            // If you do NOT want updates to change active status, remove this block:
            // if (request.getInActive() != null) {
            //     log.setInActive(request.getInActive());
            // }

            log.setUpdatedAt(LocalDateTime.now());
            return clusterLimitLogRepository.save(log);
        } else {
            LocalDate agriStart = LocalDate.now();
            LocalDate agriEnd = agriStart.plusYears(1).minusDays(1);

            int startYear = agriStart.getYear();
            int endYear = agriEnd.getYear();

            // ✅ Prevent duplicate agri year by year only
            if (clusterLimitLogRepository.existsByAgriStartAndEndYear(startYear, endYear)) {
                throw new IllegalStateException("A record already exists for agri year " + startYear + " - " + endYear);
            }

            // Deactivate all previous active records
            clusterLimitLogRepository.deactivateAll();

            // Create new active record (inActive=true means current active per your requirement)
            log = new ClusterLimitLog();
            log.setClusterMin(request.getClusterMin());
            log.setClusterMax(request.getClusterMax());
            log.setAddedBy(request.getAddedBy());
            log.setRemarks(request.getRemarks());
            log.setAgriStartYear(agriStart);
            log.setAgriEndYear(agriEnd);
            log.setTsoApprovalLimit(request.getTsoLimit());
            log.setIsEdited(false);

            // Single source of truth: inActive=true for the new active config
            log.setInActive(true);

            // If isActive exists but is redundant, keep it aligned or remove it from the entity
            // log.setIsActive(true);

            log.setCreatedAt(LocalDateTime.now());
            log.setUpdatedAt(LocalDateTime.now());
            return clusterLimitLogRepository.save(log);
        }
    }
}
