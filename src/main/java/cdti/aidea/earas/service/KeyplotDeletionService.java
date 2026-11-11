package cdti.aidea.earas.service;


import cdti.aidea.earas.config.FormEntryClient;
import cdti.aidea.earas.config.TourClient;
import cdti.aidea.earas.contract.RequestsDTOs.KeyplotDeletionRequestResponseDTO;
import cdti.aidea.earas.contract.RequestsDTOs.KeyplotDeletionUserRequestDTO;
import cdti.aidea.earas.contract.Response.KeyplotDeletionTableDTO;
import cdti.aidea.earas.contract.Response.TourDiaryPurgeResponseDTO;
import cdti.aidea.earas.model.Btr_models.*;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import cdti.aidea.earas.repository.Btr_repo.*;
import com.fasterxml.jackson.databind.JsonNode;
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
    private final FormEntryClient formEntryClient;
    private final DistrictMasterRepository districtMasterRepository;
    private final KeyplotDeletionAuditRepository auditRepository;
    private final KeyplotDeletionRequestRepository keyplotDeletionRequestRepository;
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
        // 6. Build impact snapshot
        Map<String, Object> snapshot = new LinkedHashMap<>();

        snapshot.put("keyplotId", keyPlot.getId());
        snapshot.put("landType", keyPlot.getLandType());
        snapshot.put("ownerName", keyPlot.getOwner_name());

        snapshot.put(
                "isCustomNonBtrPlot",
                keyPlot.getBtrData().getNonBtrTypeId() != null
        );

        snapshot.put("hasCluster", clusterOpt.isPresent());
        snapshot.put("clusterMasterId", clusterId);
        snapshot.put("clusterNumber", clusterNo);

// Count existing BTR cluster details
        snapshot.put("clusterDetailsFound", details.size());

// Default Form-service counts
        int nucCount = 0;
        int cropCount = 0;
        int landUtilizationCount = 0;
        int irrigationCount = 0;
        int availablePlotCount = 0;

// Fetch actual Form-service records
        if (clusterId != null) {

            JsonNode clusterLog =
                    formEntryClient .getClusterLog(clusterId);

            if (clusterLog == null) {
                throw new IllegalStateException(
                        "Unable to retrieve cluster data for deletion preview"
                );
            }

            nucCount = getArrayCount(clusterLog, "nucDetails");
            cropCount = getArrayCount(clusterLog, "cropDetails");

            landUtilizationCount =
                    getArrayCount(clusterLog, "landUtilizationDetails");

            irrigationCount =
                    getArrayCount(clusterLog, "irrigationDetails");

            availablePlotCount =
                    getArrayCount(clusterLog, "availablePlotDetails");
        }

// Preserve original record counts
        snapshot.put("nucRecordsFound", nucCount);
        snapshot.put("cropRecordsFound", cropCount);
        snapshot.put("landUtilizationRecordsFound", landUtilizationCount);
        snapshot.put("irrigationRecordsFound", irrigationCount);
        snapshot.put("availablePlotRecordsFound", availablePlotCount);

// Tour Diary count must come from its actual data source.
// Do not assume zero if it has not been checked.
        snapshot.put("tourDiaryRecordsFound", "NOT_CHECKED");

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
    private int getArrayCount(JsonNode response, String fieldName) {

        JsonNode array = response.path(fieldName);

        return array.isArray() ? array.size() : 0;
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
                        .ifPresent(d -> districtName.set(d.getDistNameEn()));
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

    @Transactional
    public KeyplotDeletionAudit createAudit(
            KeyplotDeletionRequest request,
            UUID approvedBy,
            String adminRemark,
            Map<String, Object> impactAtApproval) {

        if (auditRepository.existsByRequestId(request.getRequestId())) {
            throw new IllegalStateException(
                    "Deletion audit already exists for this request");
        }

        KeyplotDeletionAudit audit = new KeyplotDeletionAudit();

        audit.setRequestId(request.getRequestId());
        audit.setKeyplotId(request.getKeyplotId());
        audit.setZoneId(request.getZoneId());
        audit.setAgriYear(request.getAgriYear());
        audit.setRequestedBy(request.getRequestedBy());
        audit.setApprovedBy(approvedBy);
        audit.setDeletionReason(request.getReason());
        audit.setAdminRemark(adminRemark);
        audit.setRequestedAt(request.getRequestedAt());

        audit.setApprovedAt(OffsetDateTime.now());
        audit.setExecutionStartedAt(null);

        audit.setKeyplotsDeleted(0);
        audit.setClusterMasterDeleted(0);
        audit.setClusterDetailsDeleted(0);
        audit.setTourDiaryDeleted(0);
        audit.setFormDataDeleted(0);
        audit.setNucDeleted(0);
        audit.setCropRecordsDeleted(0);
        audit.setBtrRecordsDeleted(0);

        audit.setStatus("IN_PROGRESS");

        Map<String, Object> summary = new LinkedHashMap<>();

        summary.put("impactAtRequest", request.getImpactSnapshot());
        summary.put("impactAtApproval", impactAtApproval);
        summary.put("actualDeleted", new LinkedHashMap<String, Integer>());

        audit.setDeletionSummary(summary);
        audit.setFailureDetails(new LinkedHashMap<>());

        return auditRepository.save(audit);
    }
    @Transactional
    public void approveDeletionRequest(
            UUID requestId,
            UUID adminId,
            String adminRemark) {

        KeyplotDeletionRequest request =
                keyplotDeletionRequestRepository.findByRequestIdForUpdate(requestId)
                        .orElseThrow(() ->
                                new EntityNotFoundException("Request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException(
                    "Only pending requests can be approved");
        }

        if (auditRepository.existsByRequestId(requestId)) {
            throw new IllegalStateException(
                    "Deletion audit already exists for this request");
        }

        // Get cluster ID from request-time snapshot
        Object clusterIdValue =
                request.getImpactSnapshot().get("clusterMasterId");

        Long clusterId = clusterIdValue != null
                ? Long.valueOf(clusterIdValue.toString())
                : null;

        // Fetch latest Form-service impact at approval time
        Map<String, Object> impactAtApproval =
                getFormImpactSnapshot(clusterId);

        // Create permanent audit
        createAudit(
                request,
                adminId,
                adminRemark,
                impactAtApproval
        );

        request.setReviewedBy(adminId);
        request.setReviewedAt(OffsetDateTime.now());
        request.setAdminRemark(adminRemark);
        request.setStatus("IN_PROGRESS");

        keyplotDeletionRequestRepository.save(request);
    }

    private Map<String, Object> getFormImpactSnapshot(Long clusterId) {

        Map<String, Object> snapshot = new LinkedHashMap<>();

        if (clusterId == null) {
            snapshot.put("formDataStatus", "NOT_APPLICABLE");
            return snapshot;
        }

        JsonNode response = formEntryClient.getClusterLog(clusterId);

        snapshot.put("nucRecordsFound",
                response.path("nucDetails").size());

        snapshot.put("cropRecordsFound",
                response.path("cropDetails").size());

        snapshot.put("landUtilizationRecordsFound",
                response.path("landUtilizationDetails").size());

        snapshot.put("irrigationRecordsFound",
                response.path("irrigationDetails").size());

        snapshot.put("availablePlotRecordsFound",
                response.path("availablePlotDetails").size());

        snapshot.put("formDataStatus", "CHECKED");

        return snapshot;
    }
}

