package cdti.aidea.earas.service;

import cdti.aidea.earas.config.FormEntryClient;
import cdti.aidea.earas.contract.RequestsDTOs.CropAssignmentTrailSaveDto;
import cdti.aidea.earas.contract.FormEntryDto.CceAssignmentRequest;
import cdti.aidea.earas.model.Btr_models.CropAssignmentTrail;
import cdti.aidea.earas.model.Btr_models.ClusterMaster;
import cdti.aidea.earas.model.Btr_models.KeyPlots;
import cdti.aidea.earas.repository.Btr_repo.CropAssignmentTrailRepository;
import cdti.aidea.earas.repository.Btr_repo.ClusterMasterRepository;
import cdti.aidea.earas.repository.Btr_repo.KeyPlotsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import feign.FeignException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CropAssignmentTrailService {

    private final CropAssignmentTrailRepository cropAssignmentTrailRepository;
    private final ClusterMasterRepository clusterMasterRepository;
    private final KeyPlotsRepository keyPlotsRepository;
    private final FormEntryClient formEntryClient;

    /**
     * Save crop assignment trail locally and sync with external service
     */
    public Long saveCropAssignmentTrail(CropAssignmentTrailSaveDto saveDto) {
        log.info("Saving crop assignment trail for crop ID: {}", saveDto.getCropId());

        try {
            // Validate cluster exists if clusterId is provided
            ClusterMaster cluster = null;
            if (saveDto.getClusterId() != null) {
                cluster = clusterMasterRepository.findById(saveDto.getClusterId())
                        .orElseThrow(() -> new RuntimeException("Cluster not found with ID: " + saveDto.getClusterId()));
            }

            // Validate keyplot exists if keyplotId is provided
            KeyPlots keyPlot = null;
            if (saveDto.getKeyplotId() != null) {
                keyPlot = keyPlotsRepository.findById(saveDto.getKeyplotId())
                        .orElseThrow(() -> new RuntimeException("Keyplot not found with ID: " + saveDto.getKeyplotId()));
            }

            // Check for existing rejection by the same user for same crop and cluster
            if (saveDto.getRejectedBy() != null && saveDto.getClusterId() != null && Boolean.TRUE.equals(saveDto.getIsRejected())) {
                boolean alreadyRejected = cropAssignmentTrailRepository.existsByCropIdAndCluster_CluMasterIdAndIsRejectedTrueAndRejectedBy(
                        saveDto.getCropId(),
                        saveDto.getClusterId(),
                        saveDto.getRejectedBy()
                );

                if (alreadyRejected) {
                    throw new IllegalStateException("Crop has already been rejected by this user for this cluster");
                }
            }

            // Build the entity
            CropAssignmentTrail trail = CropAssignmentTrail.builder()
                    .cropId(saveDto.getCropId())
                    .cluster(cluster)
                    .keyPlot(keyPlot)
                    .zoneId(saveDto.getZoneId())
                    .landType(saveDto.getLandType())
                    .isRejected(saveDto.getIsRejected() != null ? saveDto.getIsRejected() : false)
                    .rejectionReason(saveDto.getRejectionReason())
                    .isLimitExceeded(saveDto.getIsLimitExceeded() != null ? saveDto.getIsLimitExceeded() : false)
                    .isCurrentAssignment(saveDto.getIsCurrentAssignment() != null ? saveDto.getIsCurrentAssignment() : true)
                    .rejectedBy(saveDto.getRejectedBy())
                    .rejectedAt(saveDto.getRejectedAt())
                    .assignedOn(saveDto.getAssignedOn())
                    .createdAt(LocalDateTime.now())
                    .build();

            // Save the entity locally first
            CropAssignmentTrail savedTrail = cropAssignmentTrailRepository.save(trail);
            log.info("Successfully saved crop assignment trail locally with ID: {}", savedTrail.getId());

            // Sync with external service via Feign Client
            try {
                CceAssignmentRequest cceRequest = mapToCceAssignmentRequest(saveDto, keyPlot);
                log.info("Sending data to external service: {}", cceRequest);

                formEntryClient.saveCceAssignment(cceRequest);
                log.info("Successfully synced with external service for crop ID: {}", saveDto.getCropId());

            } catch (FeignException.BadRequest e) {
                log.error("Bad request to external service for crop ID {}: Status: {}, Body: {}",
                        saveDto.getCropId(), e.status(), e.contentUTF8());
                throw new RuntimeException("External service validation failed: " + e.contentUTF8());

            } catch (FeignException.InternalServerError e) {
                log.error("Internal server error in external service for crop ID {}: Status: {}, Body: {}",
                        saveDto.getCropId(), e.status(), e.contentUTF8());
                throw new RuntimeException("External service internal error occurred. Details: " + e.contentUTF8());

            } catch (FeignException e) {
                log.error("Feign client error for crop ID {}: Status: {}, Message: {}, Body: {}",
                        saveDto.getCropId(), e.status(), e.getMessage(), e.contentUTF8());
                throw new RuntimeException("Failed to sync with external service: " + e.contentUTF8());

            } catch (Exception e) {
                log.error("Unexpected error during external service sync for crop ID {}: {}",
                        saveDto.getCropId(), e.getMessage());
                throw new RuntimeException("Unexpected error during external service synchronization: " + e.getMessage());
            }

            return savedTrail.getId();

        } catch (IllegalStateException e) {
            log.warn("Invalid crop assignment trail save request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error saving crop assignment trail for crop ID {}: {}", saveDto.getCropId(), e.getMessage());
            throw new RuntimeException("Failed to save crop assignment trail: " + e.getMessage(), e);
        }
    }

    /**
     * Map CropAssignmentTrailSaveDto to CceAssignmentRequest for Feign Client
     */
    private CceAssignmentRequest mapToCceAssignmentRequest(CropAssignmentTrailSaveDto saveDto, KeyPlots keyPlot) {
        log.info("Starting mapping for saveDto: {}", saveDto);

        CceAssignmentRequest request = new CceAssignmentRequest();

        // Handle plotId - MUST NOT BE NULL
        if (keyPlot != null && keyPlot.getId() != null) {
            request.setPlotId(keyPlot.getId());
            log.info("Set plotId from keyPlot: {}", keyPlot.getId());
        } else {
            // Generate a UUID if plotId is not available
            UUID generatedPlotId = UUID.randomUUID();
            request.setPlotId(generatedPlotId);
            log.warn("KeyPlot is null, generated plotId: {}", generatedPlotId);
        }

        // Handle clusterId - validate not null
        if (saveDto.getClusterId() == null) {
            log.error("ClusterId is null in saveDto: {}", saveDto);
            throw new IllegalArgumentException("ClusterId cannot be null for external service");
        }
        request.setClusterId(saveDto.getClusterId());

        // Handle zoneId conversion safely
        if (saveDto.getZoneId() == null) {
            log.error("ZoneId is null in saveDto: {}", saveDto);
            throw new IllegalArgumentException("ZoneId cannot be null for external service");
        }

        try {
            Integer zoneIdInt = Math.toIntExact(saveDto.getZoneId());
            request.setZoneId(zoneIdInt);
        } catch (ArithmeticException e) {
            log.error("ZoneId value too large for Integer: {}", saveDto.getZoneId());
            throw new IllegalArgumentException("ZoneId value is too large: " + saveDto.getZoneId());
        }

        // Handle cropId - validate not null
        if (saveDto.getCropId() == null) {
            log.error("CropId is null in saveDto: {}", saveDto);
            throw new IllegalArgumentException("CropId cannot be null for external service");
        }
        request.setCropId(saveDto.getCropId());

        // Set required non-null fields
        request.setCceSourceType("SYSTEM");

        // Handle addedBy - MUST NOT BE NULL
        if (saveDto.getRejectedBy() != null) {
            request.setAddedBy(saveDto.getRejectedBy());
            log.info("Set addedBy from rejectedBy: {}", saveDto.getRejectedBy());
        } else {
            // Generate a system UUID if no user is available
            UUID systemUserId = UUID.randomUUID(); // In real scenario, get from SecurityContext
            request.setAddedBy(systemUserId);
            log.warn("RejectedBy is null, using system generated addedBy: {}", systemUserId);
        }

        // Set agricultural year fields - ensure not null
        request.setAgriStartYear("2024-25");
        request.setAgriEndYear("2024-25");

        // Set boolean flags - ensure not null
        request.setIsActive(!Boolean.TRUE.equals(saveDto.getIsRejected()));
        request.setIsSelected(Boolean.TRUE.equals(saveDto.getIsCurrentAssignment()));

        log.info("Final mapped CceAssignmentRequest: {}", request);
        return request;
    }
}
