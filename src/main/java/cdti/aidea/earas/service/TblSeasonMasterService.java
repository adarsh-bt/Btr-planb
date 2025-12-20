package cdti.aidea.earas.service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import cdti.aidea.earas.contract.Response.TblSeasonMasterDTO;
import cdti.aidea.earas.model.Btr_models.TblSeasonMaster;
import cdti.aidea.earas.repository.Btr_repo.TblSeasonMasterRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class TblSeasonMasterService {
    private final TblSeasonMasterRepository repository;

    public List<TblSeasonMasterDTO> getAllSeasons() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    // ✅ Add or Update season (merged logic)
    public TblSeasonMasterDTO addSeason(TblSeasonMasterDTO dto) {
        // 🧩 Validation 1: UUID should not be null
        if (dto.getUuid() == null) {
            throw new IllegalArgumentException("UUID must not be null");
        }

        // 🧩 Validation 2: Season name must be unique (case-insensitive)
        boolean seasonExists = repository.findAll().stream()
                .anyMatch(s -> s.getSeasonName().equalsIgnoreCase(dto.getSeasonName())
                        && (dto.getId() == null || !s.getId().equals(dto.getId())));

        if (seasonExists) {
            throw new IllegalArgumentException("Season name already exists: " + dto.getSeasonName());
        }
        TblSeasonMaster entity;
    // 🟢 If ID or UUID provided → update existing
        if (dto.getId() != null) {
        Optional<TblSeasonMaster> existing = repository.findById(dto.getId());
        if (existing.isPresent()) {
            entity = existing.get();
            entity.setSeasonName(dto.getSeasonName());
            entity.setDefaultStart(dto.getDefaultStart());
            entity.setDefaultEnd(dto.getDefaultEnd());
            entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : entity.getIsActive());
        } else {
            // If ID doesn’t exist, treat as new
            entity = new TblSeasonMaster();
            entity.setSeasonName(dto.getSeasonName());
            entity.setDefaultStart(dto.getDefaultStart());
            entity.setDefaultEnd(dto.getDefaultEnd());
            entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        }
    } else {
        // 🆕 If no ID, create new
        entity = new TblSeasonMaster();
        entity.setSeasonName(dto.getSeasonName());
        entity.setDefaultStart(dto.getDefaultStart());
        entity.setDefaultEnd(dto.getDefaultEnd());
        entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
    }

    TblSeasonMaster saved = repository.save(entity);
        return convertToDTO(saved);
}
    private TblSeasonMasterDTO convertToDTO(TblSeasonMaster entity) {
        TblSeasonMasterDTO dto = new TblSeasonMasterDTO();
        dto.setId(entity.getId());
        dto.setSeasonName(entity.getSeasonName());
        dto.setDefaultStart(entity.getDefaultStart());
        dto.setDefaultEnd(entity.getDefaultEnd());
        dto.setUuid(entity.getUserId());
        dto.setIsActive(entity.getIsActive());
        return dto;
    }
}
