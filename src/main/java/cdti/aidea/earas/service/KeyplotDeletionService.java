package cdti.aidea.earas.service;


import cdti.aidea.earas.config.TourClient;
import cdti.aidea.earas.contract.RequestsDTOs.KeyplotDeletionRequestResponseDTO;
import cdti.aidea.earas.contract.RequestsDTOs.KeyplotDeletionUserRequestDTO;
import cdti.aidea.earas.contract.Response.KeyplotDeletionTableDTO;
import cdti.aidea.earas.contract.Response.TourDiaryPurgeResponseDTO;
import cdti.aidea.earas.model.Btr_models.ClusterFormData;
import cdti.aidea.earas.model.Btr_models.ClusterMaster;
import cdti.aidea.earas.model.Btr_models.KeyPlots;
import cdti.aidea.earas.model.Btr_models.KeyplotDeletionRequest;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import cdti.aidea.earas.repository.Btr_repo.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class KeyplotDeletionService {

    private final KeyPlotsRepository keyPlotsRepository;
    private final ClusterMasterRepository clusterMasterRepo;
    private final KeyplotDeletionRequestRepository requestRepo;
    private final ClusterFormDataRepository clusterDetailsRepo;
    private final TblMasterZoneRepository zoneRepository;
    private final DesTalukRepository desTalukRepository;
    private final DistrictMasterRepository districtMasterRepository;
    private final TourClient tourClient;
    private static final List<String> ACTIVE_STATUSES = List.of(
            "PENDING",
            "APPROVED",
            "IN_PROGRESS",
            "RECONCILIATION_REQUIRED"
    );

    @Transactional
    public KeyplotDeletionRequestResponseDTO submitDeletionRequest(KeyplotDeletionUserRequestDTO dto) {
        UUID keyplotId = dto.getKeyplotId();
        String reason = dto.getReason();
        UUID requestedByUserId = dto.getRequestedBy();

        // 1. Fetch Target Keyplot
        KeyPlots keyPlot = keyPlotsRepository.findById(keyplotId)
                .orElseThrow(() -> new EntityNotFoundException("Keyplot not found with ID: " + keyplotId));

        // 2. Prevent duplicate active requests
        if (requestRepo.existsByKeyplotIdAndStatusIn(keyplotId, ACTIVE_STATUSES)) {
            throw new IllegalStateException("A deletion request is already pending or being processed for Keyplot ID: " + keyplotId);
        }

        // 3. Extract required foreign keys
        if (keyPlot.getBtrData() == null || keyPlot.getBtrData().getId() == null) {
            throw new IllegalStateException("Keyplot is missing an associated BTR record.");
        }
        Long btrId = keyPlot.getBtrData().getId();

        if (keyPlot.getZone() == null || keyPlot.getZone().getZoneId() == null) {
            throw new IllegalStateException("Keyplot is missing an associated Zone record.");
        }
        Long zoneId = Long.valueOf(keyPlot.getZone().getZoneId());

        // 4. Derive Agri Year
        String agriYear = "UNKNOWN";
        if (keyPlot.getAgriStartYear() != null && keyPlot.getAgriEndYear() != null) {
            agriYear = keyPlot.getAgriStartYear().getYear() + "-" + keyPlot.getAgriEndYear().getYear();
        }

        // 5. Fetch downstream ClusterMaster & extract clusterNumber
        Optional<ClusterMaster> clusterOpt = clusterMasterRepo.findByKeyPlotId(keyplotId);

        Long clusterId = null;
        Integer clusterNo = null; // <--- EXTRACT CLUSTER NUMBER
        List<ClusterFormData> details = Collections.emptyList();

        if (clusterOpt.isPresent()) {
            ClusterMaster cluster = clusterOpt.get();
            clusterId = cluster.getCluMasterId();
            clusterNo = cluster.getClusterNumber(); // <--- SET VALUE

            // Lock cluster from further modifications
            cluster.setIs_editable(false);
            cluster.setStatus("Deletion Requested");
            clusterMasterRepo.save(cluster);

            details = clusterDetailsRepo.findByClusterMasterCluMasterIdIn(List.of(clusterId));
        }

        // 6. Build impact snapshot
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("keyplotId", keyPlot.getId());
        snapshot.put("landType", keyPlot.getLandType());
        snapshot.put("ownerName", keyPlot.getOwner_name());
        snapshot.put("isCustomNonBtrPlot", keyPlot.getBtrData().getNonBtrTypeId() != null);
        snapshot.put("hasCluster", clusterOpt.isPresent());
        snapshot.put("clusterMasterId", clusterId);
        snapshot.put("clusterNumber", clusterNo); // <--- INCLUDED IN SNAPSHOT
        snapshot.put("totalClusterDetailsImpacted", details.size());

        // 7. Persist entity
        KeyplotDeletionRequest entity = new KeyplotDeletionRequest();
        entity.setRequestNumber(generateUniqueRequestNumber());
        entity.setKeyplotId(keyplotId);
        entity.setClusterNumber(clusterNo); // <--- PERSISTED TO TABLE
        entity.setBtrId(btrId);
        entity.setZoneId(zoneId);
        entity.setAgriYear(agriYear);
        entity.setReason(reason != null ? reason.trim() : "Rectification requested");
        entity.setStatus("PENDING");
        entity.setRequestedBy(requestedByUserId);
        entity.setRequestedAt(OffsetDateTime.now());
        entity.setCorrelationId(UUID.randomUUID());
        entity.setImpactSnapshot(snapshot);

        KeyplotDeletionRequest saved = requestRepo.save(entity);


        return KeyplotDeletionRequestResponseDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public KeyplotDeletionRequestResponseDTO getRequestStatus(UUID requestId) {
        return requestRepo.findById(requestId)
                .map(KeyplotDeletionRequestResponseDTO::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("Request not found for ID: " + requestId));
    }

    @Transactional(readOnly = true)
    public KeyplotDeletionRequestResponseDTO getRequestStatusByNumber(String requestNumber) {
        return requestRepo.findByRequestNumber(requestNumber)
                .map(KeyplotDeletionRequestResponseDTO::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("Request not found for number: " + requestNumber));
    }

    private String generateUniqueRequestNumber() {
        String datePrefix = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String requestNumber;
        do {
            int randomVal = ThreadLocalRandom.current().nextInt(0x100000, 0xFFFFFF);
            requestNumber = String.format("KDR-%s-%06X", datePrefix, randomVal);
        } while (requestRepo.existsByRequestNumber(requestNumber));

        return requestNumber;
    }


    @Transactional(readOnly = true)
    public Page<KeyplotDeletionTableDTO> listDeletionRequests(
            String type,
            Integer idValue,
            int page,
            int size,
            String agriYear
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "requestedAt"));
        Page<KeyplotDeletionRequest> requests;

        if ("Taluk".equalsIgnoreCase(type)) {
            requests = requestRepo.findByTalukAndAgriYear(idValue, agriYear, pageable);
        } else if ("District".equalsIgnoreCase(type)) {
            requests = requestRepo.findByDistrictAndAgriYear(idValue, agriYear, pageable);
        } else if ("Directorate".equalsIgnoreCase(type)) {
            requests = requestRepo.findByAgriYear(agriYear, pageable);
        } else {
            throw new IllegalArgumentException("Invalid type. Use 'Taluk', 'District', or 'Directorate'");
        }

        return requests.map(this::mapToDTO);
    }

    private KeyplotDeletionTableDTO mapToDTO(KeyplotDeletionRequest req) {
        // Fetch Zone and geographic details
        TblMasterZone zone = zoneRepository.findById(req.getZoneId().intValue()).orElse(null);

        Integer talukId = null;
        String talukName = null;
        Integer districtId = null;
        AtomicReference<String> districtName = new AtomicReference<>();
        String zoneName = null;

        if (zone != null) {
            zoneName = zone.getZoneNameEn();

            if (zone.getDesTalukMaster() != null) {
                talukId = Math.toIntExact(zone.getDesTalukMaster().getDesTalukId());
                talukName = desTalukRepository.findById(talukId)
                        .map(t -> t.getDesTalukNameEn())
                        .orElse(null);
            }

            if (zone.getDistId() != null) {
                districtId = zone.getDistId();
                districtMasterRepository.findById(Long.valueOf(districtId))
                        .ifPresent(d -> districtName.set(d.getDist_name_en()));
            }
        }

        // Fetch landType from KeyPlots if available
        String landType = null;
        if (req.getKeyplotId() != null) {
            landType = keyPlotsRepository.findById(req.getKeyplotId())
                    .map(KeyPlots::getLandType)
                    .orElse(null);
        }

        return KeyplotDeletionTableDTO.builder()
                .requestId(req.getRequestId())
                .requestNumber(req.getRequestNumber())
                .keyplotId(req.getKeyplotId())
                .clusterNumber(req.getClusterNumber())
                .btrId(req.getBtrId())
                .landType(landType)
                .agriYear(req.getAgriYear())
                .zoneId(zone != null ? zone.getZoneId() : req.getZoneId().intValue())
                .zoneName(zoneName)
                .talukId(talukId)
                .talukName(talukName)
                .districtId(districtId)
                .districtName(String.valueOf(districtName))
                .status(req.getStatus())
                .reason(req.getReason())
                .adminRemark(req.getAdminRemark())
                .requestedBy(req.getRequestedBy())
                .requestedAt(req.getRequestedAt())
                .reviewedBy(req.getReviewedBy())
                .reviewedAt(req.getReviewedAt())
                .impactSnapshot(req.getImpactSnapshot())
                .build();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> previewDeletionImpact(UUID requestId) {
        KeyplotDeletionRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request not found with ID: " + requestId));

        Long clusterId = null;
        Optional<ClusterMaster> clusterOpt = clusterMasterRepo.findByKeyPlotId(request.getKeyplotId());

        int tourDiaryCount = 0;
        int cropCount = 0;
        int nucCount = 0;
        int clusterDetailsCount = 0;
        List<Map<String, Object>> visitSummary = Collections.emptyList();

        if (clusterOpt.isPresent()) {
            clusterId = clusterOpt.get().getCluMasterId();

            // 1. Fetch tour diary impact without token
            TourDiaryPurgeResponseDTO tourImpact = null;
            try {
                tourImpact = tourClient.getImpactByClusterId(clusterId);
                if (tourImpact != null) {
                    tourDiaryCount = tourImpact.getDeletedCount();

                    if (tourImpact.getArchivedTourDetails() != null) {
                        visitSummary = tourImpact.getArchivedTourDetails().stream()
                                .map(item -> {
                                    Map<String, Object> map = new LinkedHashMap<>();
                                    map.put("actionDate", item.getActionDate());
                                    map.put("status", item.getStatus());
                                    map.put("reportEntryType", item.getReportEntryType());
                                    map.put("distance", item.getDistance());
                                    map.put("remark", item.getRemark());
                                    return map;
                                })
                                .toList();
                    }
                }
            } catch (Exception e) {
                System.out.println("Could not query Tour Diary Service for clusterId: "+ clusterId+" > "+ e);
            }

            // 2. Fetch form service impact (crops, nuc) when ready
            // try {
            //     if (formServiceClient != null) {
            //         cropCount = formServiceClient.getCropCountByClusterId(clusterId);
            //         nucCount = formServiceClient.getNucCountByClusterId(clusterId);
            //     }
            // } catch (Exception e) {
            //     log.warn("Could not query Form Service for clusterId: {}", clusterId, e);
            // }

            // 3. Local cluster details count
            clusterDetailsCount = clusterDetailsRepo.findByClusterMasterCluMasterIdIn(List.of(clusterId)).size();
        }

        // Single unified impact response
        Map<String, Object> impact = new LinkedHashMap<>();
        impact.put("requestId", request.getRequestId());
        impact.put("requestNumber", request.getRequestNumber());
        impact.put("clusterId", clusterId);
        impact.put("clusterNumber", request.getClusterNumber());
        impact.put("hasCluster", clusterOpt.isPresent());
        impact.put("tourDiaryRecordsFound", tourDiaryCount);
        impact.put("tourDiaryVisits", visitSummary);
        impact.put("cropRecordsFound", cropCount);
        impact.put("nucRecordsFound", nucCount);
        impact.put("clusterDetailsFound", clusterDetailsCount);

        return impact;
    }
}