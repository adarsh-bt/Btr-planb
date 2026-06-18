package cdti.aidea.earas.service;
import cdti.aidea.earas.config.FormEntryClient;
import cdti.aidea.earas.contract.FormEntryDto.*;
import cdti.aidea.earas.contract.Projection.ClusterSummaryFastProjection;
import cdti.aidea.earas.contract.Projection.ClusterSummaryProjection;
import cdti.aidea.earas.contract.RequestsDTOs.ClusterUpdateDTO;
import cdti.aidea.earas.contract.RequestsDTOs.PlotSaveMobileAppRequest;
import cdti.aidea.earas.contract.Response.*;
import cdti.aidea.earas.contract.ValidationErrorResponse;
import cdti.aidea.earas.model.Btr_models.*;
import cdti.aidea.earas.model.Btr_models.Masters.*;
import cdti.aidea.earas.repository.Btr_repo.*;
import cdti.aidea.earas.repository.Btr_repo.projection.ClusterAreaProjection;
import cdti.aidea.earas.utils.AgriYearUtil;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClusterService {

    private final ClusterMasterRepository clusterMasterRepository;
    private final UserZoneAssignmentRepositoty userZoneAssignmentRepositoty;
    private final TblBtrDataRepository tblBtrDataRepository;
    private final KeyPlotsRepository keyPlotsRepository;
    private final ClusterFormDataRepository clusterFormDataRepository;
    private final TblZoneRevenueVillageMappingRepository tblZoneRevenueVillageMappingRepository;
    private final TblMasterVillageRepository tblMasterVillageRepository;
    private final TblMasterVillageBlockRepository tblMasterVillageBlockRepository;
    //  private final KeyPlotsRepository keyPlotsRepository;
    private final LocalBodyRepository localBodyRepository;
    private final CceCropService cceCropService;
    private final FormEntryClient formEntryClient;
    private final LocalBodyTypeRepository localBodyTypeRepository;
    private final CropAssignmentTrailRepository cropAssignmentTrailRepository;
    private final ClusterLimitLogRepository clusterLimitLogRepository;
    private final ClusterApprovalRepository clusterApprovalRepository;
    private final TblMasterZoneRepository tblMasterZoneRepository;

    public List<ClusterFormResponseDTO> getFormDataByClusterId(Long clusterId) {
        ClusterMaster clusterMaster =
                clusterMasterRepository
                        .findById(clusterId)
                        .orElseThrow(() -> new RuntimeException("Cluster not found with ID: " + clusterId));

        List<ClusterFormData> formDataList =
                clusterFormDataRepository.findByClusterMaster(clusterMaster);

        // Use a Set to filter by unique plotLabel
        Map<String, ClusterFormResponseDTO> uniqueByLabel = new LinkedHashMap<>();

        for (ClusterFormData data : formDataList) {
            String label = data.getPlotLabel();
            if (!uniqueByLabel.containsKey(label)) {
                uniqueByLabel.put(
                        label,
                        new ClusterFormResponseDTO(
                                data.getClusterMaster().getCluMasterId(), data.getPlot().getId(), label));
            }
        }

        return new ArrayList<>(uniqueByLabel.values());
    }

    public UserClusterSummaryResponse getUserClusterSummaryByYear(Integer zoneId,Integer startYear,
                                                            Integer endYear) {

        // ✅ 1. Fetch all cluster data (SINGLE QUERY)
        List<ClusterSummaryProjection> clusters =
                clusterMasterRepository.findClusterSummary(zoneId,startYear,
                         endYear);

        // ✅ 2. Get cluster IDs
        List<Long> clusterIds = clusters.stream()
                .map(ClusterSummaryProjection::getClusterId)
                .toList();

        // ✅ 3. Fetch area (ONE QUERY)
        Map<Long, Double> areaMap =
                clusterFormDataRepository.findTotalAreaByClusterIds(clusterIds)
                        .stream()
                        .collect(Collectors.toMap(
                                ClusterAreaProjection::getClusterId,
                                ClusterAreaProjection::getTotalArea
                        ));

        // ✅ 4. CCE API (keep as is)
        Set<Long> assignedClusterIds = new HashSet<>();
        Map<Long, Set<String>> clusterCropMap = new HashMap<>();
        String cceMessage = null;

        CcePlotResult cceResult = cceCropService.getAssignedCcePlotsByZoneId(Long.valueOf(zoneId));

        if (cceResult.isFallbackUsed()) {
            cceMessage = "CCE data not available currently.";
        }

        for (AvailableCcePlotResponse plot : cceResult.getPlots()) {

            if (plot.getCropId() != null && "random".equalsIgnoreCase(plot.getCceSourceType())) {
                assignedClusterIds.add(plot.getClusterId());

                clusterCropMap
                        .computeIfAbsent(plot.getClusterId(), k -> new HashSet<>())
                        .add(plot.getCropName());
            }
        }
        // ✅ 5. Build response (NO DB CALLS)
        int completed = 0, ongoing = 0, notStarted = 0, underreview = 0;
        List<ClusterStatusResponse> payload = new ArrayList<>();

        for (ClusterSummaryProjection c : clusters) {
            boolean isCce = assignedClusterIds.contains(c.getClusterId());
            Double area = areaMap.getOrDefault(c.getClusterId(), 0.0);
            switch (c.getStatus()) {
                case "Not Started" -> notStarted++;
                case "On Going" -> ongoing++;
                case "Under Review" -> underreview++;
                default -> completed++;
            }

            payload.add(new ClusterStatusResponse(
                    c.getClusterNumber(),
                    c.getKeyplotId(),
                    isCce,
                    c.getVillageName(),
                    c.getVcode(),
                    c.getLocalBodyName() + " " + c.getLocalBodyType(),
                    c.getLbcode(),
                    c.getBcode(),
//                    c.getSurveyNo(),
                    null,
                    area,
                    c.getClusterId(),
                    c.getLandType() != null ? c.getLandType().toLowerCase() : "unknown",
                    c.getStatus(),
                    null,
                    new ArrayList<>(clusterCropMap.getOrDefault(c.getClusterId(), Collections.emptySet()))
            ));
        }

        payload.sort(Comparator.comparingInt(ClusterStatusResponse::getClusterNo));

        return new UserClusterSummaryResponse(
                "Successfully fetched",
                completed,
                ongoing,
                notStarted,
                underreview,
                cceMessage,
                payload
        );
    }

    private List<SeasonStatusDto> buildSeasonStatus(
            Long clusterId,
            Map<Long, List<ExternalClusterStatusResponse>> clusterSeasonMap
    ) {
        // 🔥 Expected seasons (hardcoded)
        List<Long> expectedSeasons = List.of(1L, 2L, 3L);

        // Map: seasonId -> status (from API)
        Map<Long, String> seasonStatusMap =
                clusterSeasonMap.getOrDefault(clusterId, List.of())
                        .stream()
                        .collect(Collectors.toMap(
                                ExternalClusterStatusResponse::getSeasonId,
                                ExternalClusterStatusResponse::getStatus,
                                (existing, duplicate) -> existing // keep first
                        ));

        List<SeasonStatusDto> result = new ArrayList<>();

        for (Long seasonId : expectedSeasons) {
            result.add(
                    new SeasonStatusDto(
                            seasonId,
                            seasonStatusMap.getOrDefault(seasonId, "NOT STARTED")
                    )
            );
        }

        return result;
    }

    public UserClusterSummaryResponse getClusterSummaryWithExternalStatus(Integer zoneId) {

        // ✅ 1. SINGLE QUERY (NO ENTITY LOAD)
        List<ClusterSummaryFastProjection> clusters =
                clusterMasterRepository.getClusterSummaryFast(zoneId);

        // ✅ 2. External API (SAFE + FAST FAIL)
        List<ExternalClusterStatusResponse> externalStatus;
        try {
            externalStatus = formEntryClient.fetchClusterStatus(zoneId);
        } catch (Exception e) {
            externalStatus = Collections.emptyList();
        }

        // ✅ 3. GROUP (O(1) lookup)
        Map<Long, List<ExternalClusterStatusResponse>> clusterSeasonMap =
                externalStatus.stream()
                        .collect(Collectors.groupingBy(ExternalClusterStatusResponse::getClusterId));

        // ✅ 4. CCE (same)
        CcePlotResult cceResult =
                cceCropService.getAssignedCcePlotsByZoneId(Long.valueOf(zoneId));

        Map<Long, Set<String>> cropMap = new HashMap<>();

        for (AvailableCcePlotResponse plot : cceResult.getPlots()) {
            if (plot.getCropId() != null &&
                    "random".equalsIgnoreCase(plot.getCceSourceType())) {

                cropMap.computeIfAbsent(plot.getClusterId(), k -> new HashSet<>())
                        .add(plot.getCropName());
            }
        }

        // ✅ 5. LOOP (NO DB CALLS INSIDE)
        int completed = 0, ongoing = 0, notStarted = 0, underreview = 0;

        List<ClusterStatusResponse> payload = new ArrayList<>();

        for (ClusterSummaryFastProjection c : clusters) {

            Long clusterId = c.getClusterId();

            // 🔥 season status
            List<SeasonStatusDto> seasonStatusList =
                    buildSeasonStatus(clusterId, clusterSeasonMap);

            String clusterStatus = "NOT STARTED";

            if (seasonStatusList.stream().anyMatch(s -> "ON GOING".equalsIgnoreCase(s.getStatus()))) {
                clusterStatus = "ON GOING";
            } else if (seasonStatusList.stream().anyMatch(s -> "UNDER REVIEW".equalsIgnoreCase(s.getStatus()))) {
                clusterStatus = "UNDER REVIEW";
            } else if (!seasonStatusList.isEmpty() &&
                    seasonStatusList.stream().allMatch(s -> "COMPLETED".equalsIgnoreCase(s.getStatus()))) {
                clusterStatus = "COMPLETED";
            }

            switch (clusterStatus) {
                case "ON GOING" -> ongoing++;
                case "UNDER REVIEW" -> underreview++;
                case "NOT STARTED" -> notStarted++;
                default -> completed++;
            }

            List<String> cropList =
                    new ArrayList<>(cropMap.getOrDefault(clusterId, Set.of()));

            payload.add(
                    new ClusterStatusResponse(
                            c.getClusterNumber(),
                            c.getKeyplotId(),
                            !cropList.isEmpty(),
                            c.getVillageName(),
                            c.getVcode(),
                            c.getLocalBodyName() + " " + c.getLocalBodyType(),
                            c.getLbcode(),
                            c.getBcode(),
//                            c.getSurveyNo(),
                            "11/1",
                            c.getTotCent(), // ✅ from projection
                            clusterId,
                            c.getLandType() != null ? c.getLandType().toLowerCase() : "unknown",
                            clusterStatus,
                            seasonStatusList,
                            cropList
                    )
            );
        }

        payload.sort(Comparator.comparingInt(ClusterStatusResponse::getClusterNo));

        return new UserClusterSummaryResponse(
                "Successfully fetched",
                completed,
                ongoing,
                notStarted,
                underreview,
                cceResult.isFallbackUsed() ? "CCE data not available currently." : null,
                payload
        );
    }

    //    cluster data for App
    public Map<String, Object> getGroupedFormDataByClusterId(Long clusterId) {
        ClusterMaster clusterMaster =
                clusterMasterRepository
                        .findById(clusterId)
                        .orElseThrow(() -> new RuntimeException("Cluster not found"));

        List<ClusterFormData> formDataList =
                clusterFormDataRepository.findByClusterMasterOrderByDisplayOrderAsc(clusterMaster);
        Optional<ClusterLimitLog> currentActiveOpt =
                clusterLimitLogRepository.findByInActiveTrue();

        BigDecimal clusterMin =
                currentActiveOpt.map(ClusterLimitLog::getClusterMin).orElse(null);

        BigDecimal clusterMax =
                currentActiveOpt.map(ClusterLimitLog::getClusterMax).orElse(null);

        BigDecimal tsoClusterLimit =
                currentActiveOpt.map(ClusterLimitLog::getTsoApprovalLimit).orElse(null);

        Map<String, List<Map<String, Object>>> labelToPlotsMap = new LinkedHashMap<>();
        Map<String, Double> labelToTotalAreaMap = new LinkedHashMap<>();

        for (ClusterFormData data : formDataList) {
            String label = data.getPlotLabel();
            TblBtrData plot = data.getPlot();

            double area = data.getEnumeratedArea() != null ? data.getEnumeratedArea() : 0.0;

            Map<String, Object> plotInfo = new HashMap<>();
            plotInfo.put("cluster_plot_id", data.getCluDetailId());
            plotInfo.put("plot_id",plot.getId());
            plotInfo.put("svno", plot.getResvno() + "/" + plot.getResbdno());
            plotInfo.put("area", area);
            plotInfo.put("actual_area",data.getPlot().getTotCent());
            plotInfo.put("btr_type",data.getPlot().getBtrtype().getBTypeId());
            plotInfo.put("wardNo",data.getPlot().getWardnumber());
            plotInfo.put("houseNo",data.getPlot().getHouseno());
            plotInfo.put("ownerName",data.getPlot().getOwnername());
            plotInfo.put("address",data.getPlot().getAddress());
            plotInfo.put("tpNo",data.getPlot().getTpno());
            plotInfo.put("tpSubNo",data.getPlot().getTbsubdivisionno());
            plotInfo.put("oldSvNo",data.getPlot().getOldsvno());
            plotInfo.put("oldSubNo",data.getPlot().getOldsubno());

            // Add plot to label group
            labelToPlotsMap.computeIfAbsent(label, k -> new ArrayList<>()).add(plotInfo);

            // Sum up area per label
            double updatedArea = labelToTotalAreaMap.getOrDefault(label, 0.0) + area;
            labelToTotalAreaMap.put(label, Math.round(updatedArea * 100.0) / 100.0);
        }

        // 🔄 Fetch crop data from external service using Feign
        Response cropsResponse = formEntryClient.fetchCceCrops(clusterId);

        // Convert payload into list of crop responses
        List<FetchAvailableCceCropsResponse> crops = new ArrayList<>();
        if (cropsResponse.getPayload() instanceof List<?>) {
            for (Object obj : (List<?>) cropsResponse.getPayload()) {
                if (obj instanceof LinkedHashMap) {
                    LinkedHashMap map = (LinkedHashMap) obj;
                    FetchAvailableCceCropsResponse crop = new FetchAvailableCceCropsResponse();
                    crop.setCropId(Long.parseLong(map.get("cropId").toString()));
                    crop.setCropName(map.get("cropName").toString());
                    crop.setIsActive((Boolean) map.get("isActive"));
                    if (map.get("cceAvailablePlotId") != null) {
                        crop.setCceAvailablePlotId(UUID.fromString(map.get("cceAvailablePlotId").toString()));
                    }
                    crops.add(crop);
                }
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("clusterId", clusterId);
        response.put("labels", labelToPlotsMap);
        response.put("cluster_min", clusterMin);
        response.put("cluster_max", clusterMax);
        response.put("tso_cluster_limit", tsoClusterLimit);
        response.put("total_area", labelToTotalAreaMap); // per-label total area
        response.put("crops", crops); // ✅ Add crops list to response

        return response;
    }
    //delete cluster
    @Transactional
    public void deleteClusterFormDataById(Long id) {

        ClusterFormData formData =
                clusterFormDataRepository
                        .findById(id)
                        .orElseThrow(() -> new RuntimeException("ClusterFormData not found with ID: " + id));
        clusterFormDataRepository.delete(formData);
    }
    public List<VillagesListResponse> getVillagesListByLbCode(String lbcode) {
        // Step 1: Get all BTR data entries matching the lbcode
        List<TblBtrData> btrDataList = tblBtrDataRepository.findByLbcode(lbcode);

        if (btrDataList.isEmpty()) {
            return Collections.emptyList();
        }

        // Step 2: Extract unique LSG codes from the BTR data
        Set<Integer> lsgCodes =
                btrDataList.stream()
                        .map(TblBtrData::getLsgcode)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

        // Step 3: Get all villages matching the lsgCodes
        List<TblMasterVillage> villageList = tblMasterVillageRepository.findByLsgCodeIn(lsgCodes);

        // Step 4: For each village, get blocks and prepare the response
        return villageList.stream()
                .map(
                        village -> {
                            List<TblMasterVillageBlock> blocks =
                                    tblMasterVillageBlockRepository.findByVillageId(village.getVillageId());

                            List<BlockCodeResponse> blockCodes =
                                    blocks.stream()
                                            .map(b -> new BlockCodeResponse(b.getBlockCode()))
                                            .collect(Collectors.toList());

                            return new VillagesListResponse(
                                    village.getVillageNameEn(), Long.valueOf(village.getVillageId()), blockCodes);
                        })
                .collect(Collectors.toList());
    }
    public List<Integer> getResvnoList(UUID kpId, Integer villageId, String blockCode) {
        KeyPlots keyPlot =
                keyPlotsRepository
                        .findById(kpId)
                        .orElseThrow(() -> new EntityNotFoundException("KeyPlot not found for id: " + kpId));

        String ltype = "Wet".equalsIgnoreCase(keyPlot.getLandType()) ? "W" : "D";

        TblMasterVillage village =
                tblMasterVillageRepository
                        .findById(villageId)
                        .orElseThrow(
                                () -> new EntityNotFoundException("Village not found for id: " + villageId));

        Integer lsgcode = village.getLsgCode();

        List<TblBtrData> matchedPlots =
                tblBtrDataRepository.findByLsgcodeAndBcodeAndLtype(lsgcode, blockCode, ltype);

        // Return distinct resvno values
        return matchedPlots.stream()
                .map(TblBtrData::getResvno)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    @Transactional()
    public ResbdnoListReponse getResbdnoAreaList(
            UUID kpId, Integer villageId, String blockCode, Integer resvnoStart, Integer resvnoEnd) {
        KeyPlots keyPlot =
                keyPlotsRepository
                        .findById(kpId)
                        .orElseThrow(() -> new EntityNotFoundException("KeyPlot not found for id: " + kpId));

        String ltype = "Wet".equalsIgnoreCase(keyPlot.getLandType()) ? "W" : "D";
        String lbcode = keyPlot.getBtrData().getLbcode();
        Integer excludedResvno = keyPlot.getBtrData().getResvno();
        String excludedResbdno = keyPlot.getBtrData().getResbdno();

        TblMasterVillage village =
                tblMasterVillageRepository
                        .findById(villageId)
                        .orElseThrow(
                                () -> new EntityNotFoundException("Village not found for id: " + villageId));

        Integer lsgcode = village.getLsgCode();
        String villageName = village.getVillageNameMal();

        List<TblBtrData> matchedPlots;
        if (resvnoStart != null && resvnoEnd != null) {
            matchedPlots =
                    tblBtrDataRepository.findByLsgcodeAndBcodeAndLtypeAndResvnoBetween(
                            lsgcode, blockCode, ltype, resvnoStart, resvnoEnd);
        } else {
            matchedPlots = tblBtrDataRepository.findByLsgcodeAndBcodeAndLtype(lsgcode, blockCode, ltype);

    }
        // 🔍 Fetch ClusterMaster
        ClusterMaster clusterMaster = clusterMasterRepository.findByKeyPlotId(kpId).orElse(null);
        Map<Long, Double> enumeratedAreaMap;

        if (clusterMaster != null) {
            List<Long> matchedPlotIds =
                    matchedPlots.stream().map(TblBtrData::getId).collect(Collectors.toList());

            List<ClusterFormData> clusterFormDataList =
                    clusterFormDataRepository.findByPlotIdIn(matchedPlotIds);

            // Group by plotId and sum area
            enumeratedAreaMap =
                    clusterFormDataList.stream()
                            .collect(
                                    Collectors.groupingBy(
                                            data -> data.getPlot().getId(),
                                            Collectors.summingDouble(
                                                    data ->
                                                            data.getEnumeratedArea() != null ? data.getEnumeratedArea() : 0.0)));
        } else {
            enumeratedAreaMap = new HashMap<>();
        }

        List<ResbdnoAreaResponse> details =
                matchedPlots.stream()
                        .filter(
                                p ->
                                        !(Objects.equals(p.getResvno(), excludedResvno)
                                                && Objects.equals(p.getResbdno(), excludedResbdno)))
                        .map(
                                p -> {
                                    Double originalArea =
                                            BigDecimal.valueOf(p.getTotCent())
                                                    .setScale(2, RoundingMode.HALF_UP)
                                                    .doubleValue();

                                    Double enteredArea = enumeratedAreaMap.getOrDefault(p.getId(), 0.0);
                                    double balanceArea =
                                            BigDecimal.valueOf(originalArea - enteredArea)
                                                    .setScale(2, RoundingMode.HALF_UP)
                                                    .doubleValue();

                                    if (balanceArea <= 0.0) return null; // Skip fully entered plots

                                    return new ResbdnoAreaResponse(
                                            p.getResvno(), p.getResbdno(), originalArea, balanceArea, p.getId());
                                })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        return new ResbdnoListReponse(
                kpId,
                lbcode,
                resvnoStart,
                villageName,
                blockCode,
                details,
                details.isEmpty() ? "No data found" : "Success");
    }
        public CropReplaceClusterResponse getNextCluster (CropReplaceClusterRequest request){
            // 1. Fetch current cluster
            Optional<ClusterMaster> currentClusterOpt =
                    clusterMasterRepository.findById(request.getClusterId());
            if (currentClusterOpt.isEmpty()) {
                throw new RuntimeException("Cluster not found with ID: " + request.getClusterId());
            }

            ClusterMaster currentCluster = currentClusterOpt.get();
            Integer currentClusterNumber = currentCluster.getClusterNumber();

            // 2. Get cleaned land type
            String landType = request.getCropLandType().trim();

            // 3. Check if this cluster was already rejected for this crop by the same user
            boolean isAlreadyRejected =
                    cropAssignmentTrailRepository
                            .existsByCropIdAndCluster_CluMasterIdAndIsRejectedTrueAndRejectedBy(
                                    request.getCropId(), request.getClusterId(), request.getUserId());


            if (isAlreadyRejected) {
                return new CropReplaceClusterResponse(null, null, "Cluster already rejected by user.");
            }

            // 4. Find the CURRENT assignment for this crop (the one we're replacing)
            Optional<CropAssignmentTrail> currentAssignmentOpt =
                    cropAssignmentTrailRepository.findByCropIdAndCluster_CluMasterIdAndIsCurrentAssignmentTrue(request.getCropId(), request.getClusterId());

            // 5. Fetch next valid cluster
            List<ClusterMaster> nextClusters =
                    clusterMasterRepository.findNextClusterFlexibleLandType(
                            Math.toIntExact(request.getZoneId()), landType, currentClusterNumber);


            if (nextClusters.isEmpty()) {
                // Mark current assignment as rejected with exhaustion reason
                if (currentAssignmentOpt.isPresent()) {
                    CropAssignmentTrail currentAssignment = currentAssignmentOpt.get();
                    currentAssignment.setIsRejected(true);
                    currentAssignment.setIsCurrentAssignment(false);
                    currentAssignment.setRejectionReason("All clusters exhausted");
                    currentAssignment.setRejectedBy(request.getUserId());
                    currentAssignment.setRejectedAt(LocalDateTime.now());
                    currentAssignment.setIsLimitExceeded(true);
                    cropAssignmentTrailRepository.save(currentAssignment);
                }

                return new CropReplaceClusterResponse(
                        null, null, "No next cluster found with land type: " + landType);
            }

            ClusterMaster nextCluster = nextClusters.get(0);

            // 6. Step 1: Update current assignment to mark as rejected
            if (currentAssignmentOpt.isPresent()) {
                CropAssignmentTrail currentAssignment = currentAssignmentOpt.get();
                currentAssignment.setIsRejected(true);
                currentAssignment.setIsCurrentAssignment(false);
                currentAssignment.setRejectionReason("Rejected by user");
                currentAssignment.setRejectedBy(request.getUserId());
                currentAssignment.setRejectedAt(LocalDateTime.now());
                cropAssignmentTrailRepository.save(currentAssignment);
            }

            // 7. Step 2: Find or create assignment for next cluster
            Optional<CropAssignmentTrail> nextAssignmentOpt =
                    cropAssignmentTrailRepository.findByCropIdAndCluster_CluMasterId(
                            request.getCropId(), nextCluster.getCluMasterId());

            CropAssignmentTrail nextAssignment;
            if (nextAssignmentOpt.isPresent()) {
                // Update existing assignment
                nextAssignment = nextAssignmentOpt.get();
                nextAssignment.setIsRejected(false);
                nextAssignment.setIsCurrentAssignment(true);
                nextAssignment.setIsLimitExceeded(false);
                nextAssignment.setRejectionReason(null);
                nextAssignment.setRejectedBy(null);
                nextAssignment.setRejectedAt(null);
                nextAssignment.setAssignedOn(LocalDateTime.now());
                nextAssignment.setCreatedAt(LocalDateTime.now());
            } else {
                // Create new assignment
                nextAssignment = CropAssignmentTrail.builder()
                        .cropId(request.getCropId())
                        .cluster(nextCluster)
                        .keyPlot(nextCluster.getKeyPlot())
                        .landType(landType)
                        .zoneId(request.getZoneId())
                        .isRejected(false)
                        .isCurrentAssignment(true)
                        .assignedOn(LocalDateTime.now())
                        .build();
            }

            cropAssignmentTrailRepository.save(nextAssignment);

            // 8. Return new assignment info to Form-Service
            return new CropReplaceClusterResponse(
                    nextCluster.getCluMasterId(), nextCluster.getKeyPlot().getId(), "Success");
        }

    public ClusterPlotAreaRes getPlotDetails(UUID kpId, Integer resvno, String resbdno) {
        KeyPlots keyPlot =
                keyPlotsRepository
                        .findById(kpId)
                        .orElseThrow(() -> new EntityNotFoundException("KeyPlot not found: " + kpId));

        String lbcode = keyPlot.getBtrData().getLbcode();
        String landType = keyPlot.getLandType(); // "Wet" or "Dry"

        // Map to "W" or "D"
        String ltype = "Wet".equalsIgnoreCase(landType) ? "W" : "D";
        ;
        TblBtrData plot =
                tblBtrDataRepository
                        .findByLbcodeAndResvnoAndResbdnoAndLtype(lbcode, resvno, resbdno, ltype)
                        .orElseThrow(() -> new EntityNotFoundException("Plot not found for given parameters"));
        return new ClusterPlotAreaRes(
                kpId, lbcode, resvno, resbdno, plot.getTotCent(), plot.getBcode(), plot.getId());
    }
    @Transactional
    public void saveClusterData(
            UUID userid,
            Long zoneId,
            UUID keyplotId,
            Integer clusterNo,
            String requestedStatus,   // On Going | Under Review | COMPLETED
            String remarks,
            List<SidePlotDTO> sidePlots
    ) {


//   System.out.println("zone id "+zoneId);
        KeyPlots keyPlot =
                keyPlotsRepository.findById(keyplotId)
                        .orElseThrow(() ->
                                new RuntimeException("KeyPlot not found with ID: " + keyplotId));

        // =====================================================
        // 2️⃣ FETCH ACTIVE LIMIT CONFIG
        // =====================================================
        ClusterLimitLog limit =
                clusterLimitLogRepository.findByInActiveTrue()
                        .orElseThrow(() ->
                                new RuntimeException("Cluster limit configuration missing"));

        BigDecimal clusterMin = limit.getClusterMin();
        BigDecimal clusterMax = limit.getClusterMax();
        BigDecimal clusterMean = limit.getTsoApprovalLimit();

        // =====================================================
        // 3️⃣ FETCH / CREATE CLUSTER MASTER
        // =====================================================
        ClusterMaster clusterMaster =
                clusterMasterRepository.findByKeyPlotId(keyPlot.getId())
                        .orElseGet(() -> {
                            ClusterMaster cm = new ClusterMaster();
                            cm.setKeyPlot(keyPlot);
                            cm.setStatus("On Going");
                            cm.setIsReject(false);
                            cm.setIs_active(true);
                            cm.setCreatedAt(LocalDateTime.now());
                            cm.setUpdatedAt(LocalDateTime.now());
                            return cm;
                        });

        // =====================================================
        // 4️⃣ CALCULATE TOTAL ENUMERATED AREA (BACKEND TRUTH)
        // =====================================================
        double totalEnumeratedArea =
                sidePlots.stream()
                        .flatMap(sp -> sp.getRows().stream())
                        .mapToDouble(r -> r.getActual() != null ? r.getActual() : 0.0)
                        .sum();

        BigDecimal total = BigDecimal.valueOf(totalEnumeratedArea);

        // =====================================================
        // 5️⃣ HARD BACKEND VALIDATION (FINAL AUTHORITY)
        // =====================================================
        Boolean is_edit;
        // ❌ ABOVE MAX → STOP
        if (clusterMax != null && total.compareTo(clusterMax) > 0) {
            throw new RuntimeException("Maximum limit exceeded.");
        }

        // 🔹 BELOW MIN → ONLY SAVE (ON GOING)
        if (clusterMin != null && total.compareTo(clusterMin) < 0) {
            if (!"On Going".equalsIgnoreCase(requestedStatus)) {
                throw new RuntimeException(
                        "Below minimum: only Save allowed (Status: On Going).");
            }
        }

        // 🔹 BETWEEN MIN & MEAN → SAVE or APPROVAL
        if (clusterMin != null && clusterMean != null
                && total.compareTo(clusterMin) >= 0
                && total.compareTo(clusterMean) < 0) {

            if (!List.of("On Going", "Under Review")
                    .contains(requestedStatus)) {
                throw new RuntimeException(
                        "Between minimum and mean: Save or Send for Approval only.");
            }

            if ("Under Review".equalsIgnoreCase(requestedStatus)
                    && (remarks == null || remarks.isBlank())) {
                throw new RuntimeException(
                        "Remarks required for approval.");
            }
        }

        // 🔹 ABOVE MEAN → SAVE or COMPLETED
        if (clusterMean != null && total.compareTo(clusterMean) >= 0) {
            if (!List.of("On Going", "Completed")
                    .contains(requestedStatus)) {
                throw new RuntimeException(
                        "Above mean: Save or Completed only.");
            }
        }

        if (
                "Under Review".equalsIgnoreCase(requestedStatus) ||
                        "Completed".equalsIgnoreCase(requestedStatus)
        ) {
            clusterMaster.setIs_editable(false);
        }

        clusterMaster.setStatus(requestedStatus);
        clusterMaster.setUpdatedAt(LocalDateTime.now());
        clusterMaster.setInvestigatorRemark(remarks);
        ClusterMaster savedCluster =
                clusterMasterRepository.save(clusterMaster);

        // =====================================================
        // 7️⃣ APPROVAL LOG (ONLY IF Under Review)
        // =====================================================
        if ("Under Review".equalsIgnoreCase(requestedStatus)) {
            ClusterApprovalLog log = new ClusterApprovalLog();
            log.setClusterMaster(savedCluster);
            log.setAddedBy(userid);
            log.setZone(savedCluster.getZone());
//      log.setRemarks(remarks);
            log.setTotalArea(total);
            clusterApprovalRepository.save(log);
        }

        // =====================================================
        // 8️⃣ CLUSTER FORM DATA SAVE / UPDATE / DELETE
        // =====================================================
        List<ClusterFormData> existingData =
                clusterFormDataRepository.findByClusterMaster(savedCluster);

        Map<String, ClusterFormData> existingMap =
                existingData.stream()
                        .collect(Collectors.toMap(
                                d -> d.getPlot().getId() + "_" + d.getPlotLabel(),
                                d -> d,
                                (a, b) -> a
                        ));

        Set<String> submittedKeys = new HashSet<>();
        int orderIndex = 0;
        for (SidePlotDTO sidePlot : sidePlots) {
            for (ClusterFormRowDTO row : sidePlot.getRows()) {

                Long plotId = row.getPlot_id();
                String label = sidePlot.getLabel();
                String key = plotId + "_" + label;
                submittedKeys.add(key);

                // =================================================
                // 🔥 RESTORED TBLBTR AUTO-CREATION (YOUR OLD LOGIC)
                // =================================================
                TblBtrData plot =
                        tblBtrDataRepository.findById(plotId)
                                .orElseGet(() -> {

                                    TblBtrData newPlot = new TblBtrData();
                                    newPlot.setZone(Long.valueOf(keyPlot.getZone().getZoneId()));
                                    newPlot.setResvno(row.getSvNo());
                                    newPlot.setResbdno(row.getSubNo());
                                    newPlot.setBcode(row.getBcode());
                                    newPlot.setTotCent(row.getArea());
                                    newPlot.setLtype(keyPlot.getLandType());

                                    TblBtrData kp = keyPlot.getBtrData();
                                    newPlot.setDcode(kp.getDcode());
                                    newPlot.setTcode(kp.getTcode());
                                    newPlot.setVcode(Integer.valueOf(row.getVillage()));
                                    newPlot.setBtrtype(kp.getBtrtype());
                                    newPlot.setAddress(row.getAddress());
                                    newPlot.setOwnername(row.getOwnername());
                                    newPlot.setHouseno(row.getHouseno());
                                    newPlot.setOldsvno(row.getOldsvno());
                                    newPlot.setOldsubno(row.getOldsubno());
                                    newPlot.setTpno(row.getTpno());
                                    newPlot.setTbsubdivisionno(row.getTbsubdivisionno());
                                    newPlot.setWardnumber(row.getWard_number());
                                    newPlot.setLbcode(kp.getLbcode());
                                    newPlot.setLtype(kp.getLtype());
                                    LocalDate now = LocalDate.now();
                                    LocalDate agreStart = LocalDate.of(now.getYear(), 7, 1); // July 1 of current year
                                    LocalDate agreEnd = LocalDate.of(now.getYear() + 1, 6, 30); // June 30 of next year
                                    newPlot.setAgreStartYear(agreStart);
                                    newPlot.setAgreEndYear(agreEnd);
                                    newPlot.setUpdated_by(userid);
                                    newPlot.setCreated_by(userid);
                                    newPlot.setInsertionTime(LocalDateTime.now());
                                    newPlot.setUpdationTime(LocalDateTime.now());
                                    TblMasterVillage village =
                                            tblMasterVillageRepository
                                                    .findById(Integer.valueOf(row.getVillage()))
                                                    .orElseThrow();
                                    newPlot.setLsgcode(village.getLsgCode());

                                    return tblBtrDataRepository.save(newPlot);
                                });

                if (row.getActual() == null) {
                    throw new RuntimeException(
                            "Enumerated area cannot be null for plot " + plotId);
                }

                ClusterFormData formData;
                if (existingMap.containsKey(key)) {
                    formData = existingMap.get(key);
                    formData.setEnumeratedArea(row.getActual());
                    formData.setUpdatedAt(LocalDateTime.now());
                    existingMap.remove(key);
                } else {
                    formData = new ClusterFormData();
                    formData.setClusterMaster(savedCluster);
                    formData.setPlot(plot);
                    formData.setPlotLabel(label);
                    formData.setEnumeratedArea(row.getActual());
                    formData.setStatus(true);
                    formData.setCreatedAt(LocalDateTime.now());
                    formData.setUpdatedAt(LocalDateTime.now());
                    formData.setCreatedBy(userid);
                }
                formData.setDisplayOrder(orderIndex);
                formData.setUpdatedAt(LocalDateTime.now());
                clusterFormDataRepository.save(formData);
                orderIndex++;
            }
        }

        // =====================================================
        // 9️⃣ DELETE REMOVED PLOTS
        // =====================================================
        clusterFormDataRepository.deleteAll(existingMap.values());
    }

    public List<KeyPlotClusterDTO> getClustersByZoneId(Integer zoneId) {
        // Fetch KeyPlots by zoneId

        Optional<UserZoneAssignment> userzone =
                userZoneAssignmentRepositoty.findByTblMasterZone_ZoneId(zoneId);

        List<KeyPlots> keyPlots = keyPlotsRepository.findByZone(userzone.get().getTblMasterZone());

        if (keyPlots.isEmpty()) {
            return Collections.emptyList();
        }

        // Fetch Clusters for these KeyPlots
        List<ClusterMaster> clusters = clusterMasterRepository.findByKeyPlotIn(keyPlots);

        // Map clusters to DTOs, joining with KeyPlot info
        List<KeyPlotClusterDTO> dtos =
                clusters.stream()
                        .map(
                                cluster -> {
                                    KeyPlots plot = cluster.getKeyPlot();

                                    String localBodyName =
                                            plot.getLocalbody() != null
                                                    ? plot.getLocalbody().getLocalbodyNameEn()
                                                    : null; // Assuming TblLocalBody has getName()

                                    return new KeyPlotClusterDTO(
                                            cluster.getCluMasterId(),
                                            plot.getLandType(),
                                            localBodyName,
                                            cluster.getClusterNumber());
                                })
                        .collect(Collectors.toList());

        return dtos;
    }
    @Transactional
    public Map<String, Object> savePlotFromMobile(PlotSaveMobileAppRequest request) {
        // 1. Get KeyPlot and its associated data
        KeyPlots keyPlot = keyPlotsRepository.findById(request.getKeyplotId())
                .orElseThrow(() -> new RuntimeException("KeyPlot not found with ID: " + request.getKeyplotId()));

        TblBtrData keyPlotBtr = keyPlot.getBtrData();
        if (keyPlotBtr == null) {
            throw new RuntimeException("KeyPlot does not have associated BTR data.");
        }
        if (request.getActual() == null || request.getArea() == null || request.getUserId() == null){
            throw  new RuntimeException("Actual area , Enumerate Area and UserId must fill");
        }
        if (keyPlotBtr.getBtrtype().getBTypeId()==1){
            if (request.getSvNo() == null){

                throw new RuntimeException("Resurvey number must fill");
            }
        } else if (keyPlotBtr.getBtrtype().getBTypeId() == 2) {
            if (request.getWard_number() == null || request.getHouseno() == null) {
                throw new RuntimeException("Ward number and House number must fill");
            }
        } else if (keyPlotBtr.getBtrtype().getBTypeId() == 3) {
            if (request.getOwnername() == null || request.getAddress() == null || request.getActual() == null){
                throw new RuntimeException("Owner name , address and area must fill");
            }
        } else if (keyPlotBtr.getBtrtype().getBTypeId() == 4) {
            if (request.getTp_no() == null){
                throw new RuntimeException("Tp number number must fill");
            }

        }else if (keyPlotBtr.getBtrtype().getBTypeId() == 5) {
            if (request.getOld_survey_number() == null){
                throw new RuntimeException("Old survey number Must fill number must fill");
            }

        }
        {

        }
        // 2. Create new BTR entry from mobile data
        TblBtrData btrData = new TblBtrData();
        btrData.setResvno(request.getSvNo());
        btrData.setResbdno(request.getSubNo());
        btrData.setTotCent(request.getArea());
        btrData.setCreated_by(request.getUserId());
        btrData.setUpdated_by(request.getUserId());

        if(request.getVillage() != null){
            btrData.setVcode(Integer.valueOf(request.getVillage()));
            btrData.setBcode(request.getBcode());
        }

        btrData.setLtype(keyPlotBtr.getLtype());
        btrData.setOldsvno(request.getOld_survey_number());
        btrData.setOldsubno(request.getOld_subdivision_number());
        btrData.setWardnumber(request.getWard_number());
        btrData.setTpno(request.getTp_no());
        btrData.setTbsubdivisionno(request.getTb_subdivision_no());
        btrData.setOwnername(request.getOwnername());
        btrData.setHouseno(request.getHouseno());
        btrData.setAddress(request.getAddress());
        btrData.setCreated_by(request.getUserId());
        btrData.setUpdated_by(request.getUserId());
        btrData.setUpdationTime(LocalDateTime.now());
        btrData.setInsertionTime(LocalDateTime.now());
        btrData.setZone(keyPlotBtr.getZone());
        // btrData.setCl_no(request.getCl_no());
        // Copy values from keyplot BTR
        btrData.setDcode(keyPlotBtr.getDcode());
        btrData.setTcode(keyPlotBtr.getTcode());
        btrData.setLtype(keyPlotBtr.getLtype());
        btrData.setLbcode(keyPlotBtr.getLbcode());
        btrData.setBtrtype(keyPlotBtr.getBtrtype());
        btrData.setInsertionTime(LocalDateTime.now());
        btrData.setUpdationTime(LocalDateTime.now());
        LocalDate now = LocalDate.now();
        LocalDate agreStart = LocalDate.of(now.getYear(), 7, 1); // July 1 of current year
        LocalDate agreEnd = LocalDate.of(now.getYear() + 1, 6, 30); // June 30 of next year
        btrData.setAgreStartYear(agreStart);
        btrData.setAgreEndYear(agreEnd);
        Optional<TblMasterVillage> lsg =
                tblMasterVillageRepository.findById(request.getVillage());

        btrData.setLsgcode(lsg.get().getLsgCode());

        // Set LSG code from village master (if available)
        if (btrData.getVcode() == null) {
            tblMasterVillageRepository.findById(request.getVillage())
                    .ifPresent(v -> btrData.setLsgcode(v.getLsgCode()));
        }

        // Save BTR data
        TblBtrData savedBtr = tblBtrDataRepository.save(btrData);

        // 3. Find or create ClusterMaster
        Optional<ClusterMaster> clusterMaster = clusterMasterRepository.findByKeyPlotId(request.getKeyplotId());


        // 4. Check if ClusterFormData already exists
        Optional<ClusterFormData> existingForm = clusterFormDataRepository
                .findByClusterMasterAndPlotAndPlotLabel(clusterMaster.get(), savedBtr, request.getClusterlabel());

        ClusterFormData formData = existingForm.orElseGet(ClusterFormData::new);
        Integer maxOrder = clusterFormDataRepository
                .findMaxDisplayOrderByLabel(clusterMaster.get(), request.getClusterlabel());

        int nextOrder = (maxOrder == null) ? 0 : maxOrder + 1;

        formData.setDisplayOrder(nextOrder);
        formData.setClusterMaster(clusterMaster.get());
        formData.setPlot(savedBtr);
        formData.setPlotLabel(request.getClusterlabel());
        formData.setEnumeratedArea(request.getActual());
        formData.setStatus(true);
        formData.setUpdatedAt(LocalDateTime.now());
        formData.setCreatedBy(request.getUserId());

        if (formData.getCreatedAt() == null) {
            formData.setCreatedAt(LocalDateTime.now());
        }

        // Save or update form data
        clusterFormDataRepository.save(formData);

        // 5. Optionally recalculate total area and update cluster status (optional)
        double totalArea = clusterFormDataRepository
                .findByClusterMaster(clusterMaster.get())
                .stream()
                .mapToDouble(f -> f.getEnumeratedArea() != null ? f.getEnumeratedArea() : 0.0)
                .sum();

        Optional<ClusterLimitLog> activeLimit = clusterLimitLogRepository.findByInActiveTrue();

        String status = "On Going";
        if (activeLimit.isPresent()) {
            BigDecimal min = activeLimit.get().getClusterMin();
            BigDecimal max = activeLimit.get().getClusterMax();
            BigDecimal tsoLimit = activeLimit.get().getTsoApprovalLimit();

            BigDecimal total = BigDecimal.valueOf(totalArea);
            if (max != null && total.compareTo(max) > 0) {
                throw new RuntimeException("Maximum cluster area exceeded.");
            } else if (tsoLimit != null && total.compareTo(tsoLimit) < 0) {
//        status = "Under Review";

                // Save review log
                ClusterApprovalLog reviewLog = new ClusterApprovalLog();
                reviewLog.setClusterMaster(clusterMaster.get());
                reviewLog.setAddedBy(request.getUserId());
                reviewLog.setZone(clusterMaster.get().getZone());
                reviewLog.setRemarks("Cluster is under review from mobile");
                reviewLog.setTotalArea(total);
                clusterApprovalRepository.save(reviewLog);
            } else if (min != null && total.compareTo(min) >= 0) {
//        status = "Completed";
            }
        }

        // Update status if changed
//    clusterMaster.get().setStatus(status);
        clusterMaster.get().setUpdatedAt(LocalDateTime.now());
        clusterMasterRepository.save(clusterMaster.get());

        // 6. Return response
        Map<String, Object> result = new HashMap<>();
        result.put("status", "Success");
        result.put("message", "Plot saved to cluster successfully.");
        result.put("plotId", savedBtr.getId());
        result.put("clusterStatus", status);
        return result;
    }

    public List<BtrClusterUsageResponse> getBtrClusterUsage(Long btrId) {
        return clusterFormDataRepository.findClusterUsageByBtrId(btrId);
    }

    @Transactional
    public void updateClusterPlot(Long clusterPlotId, Double enumeratedArea, UUID userId) {
        // 1. Find the ClusterFormData entity by its primary key
        ClusterFormData formData = clusterFormDataRepository.findById(clusterPlotId)
                .orElseThrow(() -> new EntityNotFoundException("ClusterFormData not found with ID: " + clusterPlotId));

        // 2. Update the enumerated area and timestamp
        formData.setEnumeratedArea(enumeratedArea);
        formData.setUpdatedAt(LocalDateTime.now());
        // formData.setUpdatedBy(userId); // Assuming you add an 'updatedBy' field

        clusterFormDataRepository.save(formData);

        // 3. Recalculate total area and update the parent ClusterMaster's status
        ClusterMaster clusterMaster = formData.getClusterMaster();
        if (clusterMaster != null) {
            // Fetch all plots for the cluster to get the new total area
            double totalEnumeratedArea = clusterFormDataRepository.findByClusterMaster(clusterMaster).stream()
                    .mapToDouble(plot -> plot.getEnumeratedArea() != null ? plot.getEnumeratedArea() : 0.0)
                    .sum();

            // Fetch cluster limits
            Optional<ClusterLimitLog> activeLimitOpt = clusterLimitLogRepository.findByInActiveTrue();
            BigDecimal minLimit = activeLimitOpt.map(ClusterLimitLog::getClusterMin).orElse(null);
            BigDecimal maxLimit = activeLimitOpt.map(ClusterLimitLog::getClusterMax).orElse(null);
            BigDecimal tsoLimit = activeLimitOpt.map(ClusterLimitLog::getTsoApprovalLimit).orElse(null);

            // Determine the new status based on the updated total area
            String newStatus;
            BigDecimal totalAreaBD = BigDecimal.valueOf(totalEnumeratedArea);

            if (maxLimit != null && totalAreaBD.compareTo(maxLimit) > 0) {
                throw new RuntimeException("Update rejected: Maximum cluster area exceeded.");
            } else if (minLimit != null && totalAreaBD.compareTo(minLimit) < 0) {
                newStatus = "On Going";
            } else if (tsoLimit != null && totalAreaBD.compareTo(tsoLimit) < 0) {
                newStatus = "Under Review";
                // Optionally create a log entry for the status change
                if (!"Under Review".equals(clusterMaster.getStatus())) {
                    ClusterApprovalLog log = new ClusterApprovalLog();
                    log.setClusterMaster(clusterMaster);
                    log.setAddedBy(userId);
                    log.setZone(clusterMaster.getZone());
                    log.setRemarks("Cluster is now Under Review after a plot update.");
                    log.setTotalArea(totalAreaBD);
                    clusterApprovalRepository.save(log);
                }
            } else {
                newStatus = "Completed";
            }

            // 4. Save the updated status on the ClusterMaster
            clusterMaster.setStatus(newStatus);
            clusterMaster.setUpdatedAt(LocalDateTime.now());
            clusterMasterRepository.save(clusterMaster);
        }
    }

    @Transactional
    public String bulkUpdateClusterNumbers(List<ClusterUpdateDTO> updates) {

        if (updates == null || updates.isEmpty()) {
            return "No updates provided";
        }
        Set<Integer> newNumbers = updates.stream()
                .map(ClusterUpdateDTO::getNewClusterNumber)
                .collect(Collectors.toSet());

        if (newNumbers.size() != updates.size()) {
            throw new RuntimeException("Duplicate cluster numbers in request");
        }

        // 🔹 Step 1: Load all clusters
        Map<Long, ClusterMaster> clusterMap = new HashMap<>();

        for (ClusterUpdateDTO dto : updates) {
            ClusterMaster cluster = clusterMasterRepository.findById(dto.getClusterId())
                    .orElseThrow(() -> new RuntimeException("Cluster not found: " + dto.getClusterId()));

            // ✅ Validate range
            if (dto.getNewClusterNumber() < 1 || dto.getNewClusterNumber() > 100) {
                throw new RuntimeException("Invalid cluster number: " + dto.getNewClusterNumber());
            }

            clusterMap.put(dto.getClusterId(), cluster);
        }

        // 🔹 Step 2: TEMP assign (-1, -2...) to avoid unique conflict
        int temp = -1;
        for (ClusterMaster c : clusterMap.values()) {
            c.setClusterNumber(temp--);
        }
        clusterMasterRepository.saveAll(clusterMap.values());

        // 🔹 Step 3: Apply final values
        for (ClusterUpdateDTO dto : updates) {
            ClusterMaster cluster = clusterMap.get(dto.getClusterId());
            cluster.setClusterNumber(dto.getNewClusterNumber());
        }

        clusterMasterRepository.saveAll(clusterMap.values());

        return "Bulk cluster update successful";
    }
    public ClusterTourResponse getClusterDetails(Long clusterId) {

        ClusterMaster cluster = clusterMasterRepository.findById(clusterId)
                .orElseThrow(() -> new RuntimeException("Cluster not found"));
        ClusterTourResponse response = new ClusterTourResponse();
        Optional<TblLocalBody> tblLocalBody = localBodyRepository.findByCodeApi(cluster.getKeyPlot().getBtrData().getLbcode());
        System.out.println(cluster.getKeyPlot().getBtrData().getLbcode());
        System.out.println(cluster.getKeyPlot().getBtrData());
        response.setClusterNo(cluster.getClusterNumber());
        response.setZoneName(cluster.getZone().getZoneNameEn());
        response.setLandType(cluster.getKeyPlot().getLandType());
        response.setLocalbody(tblLocalBody.get().getLocalbodyNameMal());
        return response;
    }

    //Cluster OverView Status have to link with Form1
    public ClusterStatusOverviewResponse getClusterStatusOverview(
            Integer zoneId,
            String agriYear) {

        LocalDate agriStartDate =
                AgriYearUtil.getAgriYearStart(agriYear);

        LocalDate agriEndDate =
                AgriYearUtil.getAgriYearEnd(agriYear);

        Integer startYear = agriStartDate.getYear();
        Integer endYear = agriEndDate.getYear();

        UserClusterSummaryResponse response =
                getUserClusterSummaryByYear(
                        zoneId,
                        startYear,
                        endYear
                );

        Integer completed = response.getCompleted();
        Integer ongoing = response.getOngoing();
        Integer notStarted = response.getNotStarted();
        Integer underReview = response.getUnderreview();

        Integer totalClusterStatus =
                completed +
                        ongoing +
                        notStarted +
                        underReview;

        return new ClusterStatusOverviewResponse(
                totalClusterStatus,
                completed,
                ongoing,
                notStarted,
                underReview
        );
    }
}
