package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.RequestsDTOs.KeyplotsLimitLogRequest;
import cdti.aidea.earas.contract.Response.KeyplotsLimitLogResponse;
import cdti.aidea.earas.model.Btr_models.KeyplotsLimitLog;
import cdti.aidea.earas.repository.Btr_repo.KeyplotsLimitLogRepository;
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
}
