package cdti.aidea.earas.service;

import cdti.aidea.earas.config.FormEntryClient;
import cdti.aidea.earas.contract.FormEntryDto.CceAssignmentRequest;
import cdti.aidea.earas.contract.RequestsDTOs.CropAssignmentTrailSaveDto;
import cdti.aidea.earas.model.Btr_models.ClusterMaster;
import cdti.aidea.earas.model.Btr_models.CropAssignmentTrail;
import cdti.aidea.earas.model.Btr_models.KeyPlots;
import cdti.aidea.earas.repository.Btr_repo.ClusterMasterRepository;
import cdti.aidea.earas.repository.Btr_repo.CropAssignmentTrailRepository;
import cdti.aidea.earas.repository.Btr_repo.KeyPlotsRepository;
import feign.FeignException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CropAssignmentTrailService {

  private final CropAssignmentTrailRepository cropAssignmentTrailRepository;
  private final ClusterMasterRepository clusterMasterRepository;
  private final KeyPlotsRepository keyPlotsRepository;
  private final FormEntryClient formEntryClient;

  @Transactional
  public List<Long> saveCropAssignmentTrail(List<CropAssignmentTrailSaveDto> saveDtoList) {

    int currentYear = LocalDateTime.now().getYear();
    int nextYear = currentYear + 1;
    String agriYear = currentYear + "-" + String.valueOf(nextYear).substring(2);

    for (CropAssignmentTrailSaveDto saveDto : saveDtoList) {
      try {
        log.info("Processing crop assignment trail for crop ID: {}", saveDto.getCropId());

        // Validate cluster exists if clusterId is provided
        ClusterMaster cluster = null;
        if (saveDto.getClusterId() != null) {
          cluster =
              clusterMasterRepository
                  .findById(saveDto.getClusterId())
                  .orElseThrow(
                      () ->
                          new RuntimeException(
                              "Cluster not found with ID: " + saveDto.getClusterId()));
        }

        // Validate keyplot exists if keyplotId is provided
        KeyPlots keyPlot = null;
        if (saveDto.getKeyplotId() != null) {
          keyPlot =
              keyPlotsRepository
                  .findById(saveDto.getKeyplotId())
                  .orElseThrow(
                      () ->
                          new RuntimeException(
                              "Keyplot not found with ID: " + saveDto.getKeyplotId()));
        }

        // Check for existing rejection
        if (saveDto.getRejectedBy() != null
            && saveDto.getClusterId() != null
            && Boolean.TRUE.equals(saveDto.getIsRejected())) {
          boolean alreadyRejected =
              cropAssignmentTrailRepository
                  .existsByCropIdAndCluster_CluMasterIdAndIsRejectedTrueAndRejectedBy(
                      saveDto.getCropId(), saveDto.getClusterId(), saveDto.getRejectedBy());
          if (alreadyRejected) {
            log.warn(
                "Crop already rejected by user {} for cluster {}",
                saveDto.getRejectedBy(),
                saveDto.getClusterId());
            continue; // skip this record
          }
        }

        // Build and save entity
        CropAssignmentTrail trail =
            CropAssignmentTrail.builder()
                .cropId(saveDto.getCropId())
                .cluster(cluster)
                .keyPlot(keyPlot)
                .zoneId(saveDto.getZoneId())
                .landType(saveDto.getLandType())
                .isRejected(saveDto.getIsRejected() != null ? saveDto.getIsRejected() : false)
                .rejectionReason(saveDto.getRejectionReason())
                .isLimitExceeded(
                    saveDto.getIsLimitExceeded() != null ? saveDto.getIsLimitExceeded() : false)
                .isCurrentAssignment(
                    saveDto.getIsCurrentAssignment() != null
                        ? saveDto.getIsCurrentAssignment()
                        : true)
                .rejectedBy(saveDto.getRejectedBy())
                .rejectedAt(saveDto.getRejectedAt())
                .assignedOn(saveDto.getAssignedOn())
                .createdAt(LocalDateTime.now())
                .build();

        CropAssignmentTrail savedTrail = cropAssignmentTrailRepository.save(trail);
        log.info("Saved crop assignment trail locally with ID: {}", savedTrail.getId());

        // Map and send to external service
        CceAssignmentRequest cceRequest = new CceAssignmentRequest();

        if (keyPlot != null && keyPlot.getId() != null) {
          cceRequest.setPlotId(keyPlot.getId());
        } else {
          cceRequest.setPlotId(UUID.randomUUID());
        }

        if (saveDto.getClusterId() == null)
          throw new IllegalArgumentException("ClusterId cannot be null");
        if (saveDto.getZoneId() == null)
          throw new IllegalArgumentException("ZoneId cannot be null");
        if (saveDto.getCropId() == null)
          throw new IllegalArgumentException("CropId cannot be null");

        cceRequest.setClusterId(saveDto.getClusterId());
        cceRequest.setZoneId(Math.toIntExact(saveDto.getZoneId()));
        cceRequest.setCropId(saveDto.getCropId());
        cceRequest.setCceSourceType("RANDOM");
        cceRequest.setAddedBy(
            saveDto.getRejectedBy() != null ? saveDto.getRejectedBy() : UUID.randomUUID());
        cceRequest.setAgriStartYear(agriYear);
        cceRequest.setAgriEndYear(agriYear);
        cceRequest.setIsActive(!Boolean.TRUE.equals(saveDto.getIsRejected()));
        cceRequest.setIsSelected(Boolean.TRUE.equals(saveDto.getIsCurrentAssignment()));

        try {
          formEntryClient.saveCceAssignment(cceRequest);
          log.info(
              "Successfully synced with external service for crop ID: {}", saveDto.getCropId());
        } catch (FeignException e) {
          log.error(
              "Feign error for crop ID {}: Status {}, Body: {}",
              saveDto.getCropId(),
              e.status(),
              e.contentUTF8());
        } catch (Exception e) {
          log.error(
              "Unexpected error during external sync for crop ID {}: {}",
              saveDto.getCropId(),
              e.getMessage());
        }

      } catch (Exception e) {
        log.error("Failed to process crop ID {}: {}", saveDto.getCropId(), e.getMessage());
        // continue with next DTO
      }
    }
    return null;
  }
}
