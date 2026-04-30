package cdti.aidea.earas.service;

import cdti.aidea.earas.config.FormEntryClient;
import cdti.aidea.earas.contract.FormEntryDto.*;
import cdti.aidea.earas.contract.RequestsDTOs.PlotSaveMobileAppRequest;
import cdti.aidea.earas.contract.Response.*;
import cdti.aidea.earas.contract.ValidationErrorResponse;
import cdti.aidea.earas.model.Btr_models.*;
import cdti.aidea.earas.model.Btr_models.Masters.*;
import cdti.aidea.earas.repository.Btr_repo.*;
import cdti.aidea.earas.repository.Btr_repo.projection.ClusterAreaProjection;
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

  public UserClusterSummaryResponse getUserClusterSummary(Integer zone_Id) {

    //        Optional<UserZoneAssignment> userOpt =
    // userZoneAssignmentRepositoty.findByUserId(userId);

    Optional<TblMasterZone> zone = tblMasterZoneRepository.findById(zone_Id);
    if (zone.isEmpty()) {
      throw new NoSuchElementException("User not found");
    }
    //        UserZoneAssignment user = userOpt.get();
    Long zoneId = Long.valueOf(zone.get().getZoneId());
    Set<Long> assignedClusterIds = new HashSet<>();
    String cceMessage = null;

    CcePlotResult cceResult = cceCropService.getAssignedCcePlotsByZoneId(zoneId);
    if (cceResult.isFallbackUsed()) {
      cceMessage = "CCE data not available currently.";
    }
    List<AvailableCcePlotResponse> assignedCcePlots = cceResult.getPlots();
    assignedClusterIds =
        assignedCcePlots.stream()
            .filter(
                plot ->
                    plot.getCropId() != null && "random".equalsIgnoreCase(plot.getCceSourceType()))
            .map(AvailableCcePlotResponse::getClusterId)
            .collect(Collectors.toSet());
    Map<Long, Set<String>> clusterCropMap = new HashMap<>();
    for (AvailableCcePlotResponse plot : assignedCcePlots) {
      if (plot.getCropId() != null && "random".equalsIgnoreCase(plot.getCceSourceType())) {
        clusterCropMap
            .computeIfAbsent(plot.getClusterId(), k -> new HashSet<>())
            .add(plot.getCropName());
      }
    }
    List<ClusterMaster> clusters =
        clusterMasterRepository.findAllByZoneIdAndIsRejectFalse(zone.get().getZoneId());
    int completed = 0, ongoing = 0, notStarted = 0, underreview = 0;
    List<ClusterStatusResponse> payload = new ArrayList<>();
    for (ClusterMaster cluster : clusters) {
      Long clusterId = cluster.getCluMasterId();
      UUID keyplotId = cluster.getKeyPlot().getId();
      Set<String> cropNames = clusterCropMap.getOrDefault(clusterId, Collections.emptySet());
      boolean isCce = !cropNames.isEmpty();
      String status = cluster.getStatus();
      String landType = cluster.getKeyPlot().getLandType();
      String keyplot_svno =
          cluster.getKeyPlot().getBtrData().getResvno()
              + "/"
              + cluster.getKeyPlot().getBtrData().getResbdno();
      String keyplot_lbcode = cluster.getKeyPlot().getBtrData().getBcode();
      String local_body_code = cluster.getKeyPlot().getBtrData().getLbcode();
//      Double keyplot_area = cluster.getKeyPlot().getBtrData().getTotCent();
      // code by k:
      //            String keyplot_svno = cluster.getKeyPlot().getBtrData().getResvno() + "/" +
      // cluster.getKeyPlot().getBtrData().getResbdno();
      //            Integer keyplot_bcode = cluster.getKeyPlot().getBtrData().getBcode();   // use
      // Integer
      //            String keyplot_lbcode = cluster.getKeyPlot().getBtrData().getLbcode();  // use
      // String
      //            Double keyplot_area = cluster.getKeyPlot().getBtrData().getNare();      // or
      // nhect/nsqm depending on "area"

      // code  by k:

      // ⚠️ TblBtrData does not have "area". Use nsqm, nhect, or nare instead
      // Double keyplot_area = cluster.getKeyPlot().getBtrData().getNsqm();

      TblLocalBody localBody = localBodyRepository.findByCodeApi(local_body_code).orElse(null);
      String localBodyName = "Local body not found";
      if (localBody != null) {
        String baseName = localBody.getLocalbodyNameEn();
        String localBodyTypeName = "Unknown";

        if (localBody.getLocalbodyType() != null) {
          Optional<LocalBodyType> localBodyTypeOpt =
              localBodyTypeRepository.findById(localBody.getLocalbodyType().longValue());
          if (localBodyTypeOpt.isPresent()) {
            localBodyTypeName = localBodyTypeOpt.get().getName();
          }
        }

        localBodyName = baseName + " " + localBodyTypeName;
      }

      String villageName =
          tblMasterVillageRepository
              .findFirstByLsgCode(cluster.getKeyPlot().getBtrData().getLsgcode())
              .map(TblMasterVillage::getVillageNameEn)
              .orElse("Village not found");

      switch (status) {
        case "Not Started" -> notStarted++;
        case "On Going" -> ongoing++;
        case "Under Review" -> underreview++;
        default -> completed++;
      }
        List<Long> clusterIds =
                clusters.stream()
                        .map(ClusterMaster::getCluMasterId)
                        .toList();
        Map<Long, Double> clusterAreaMap =
                clusterFormDataRepository
                        .findTotalAreaByClusterIds(clusterIds)
                        .stream()
                        .collect(Collectors.toMap(
                                ClusterAreaProjection::getClusterId,
                                ClusterAreaProjection::getTotalArea
                        ));
        Double clusterTotalArea =
                clusterAreaMap.getOrDefault(clusterId, 0.0);
        payload.add(
          new ClusterStatusResponse(
              cluster.getClusterNumber(),
              keyplotId,
              isCce,
              villageName,
              cluster.getKeyPlot().getBtrData().getVcode(),
              localBodyName,
              local_body_code,
              keyplot_lbcode,
              keyplot_svno,
                  clusterTotalArea,
              clusterId,
              landType != null ? landType.toLowerCase() : "unknown",
              status,
              null,
              new ArrayList<>(cropNames) // Pass the crop names list here
              ));
    }

    payload.sort(Comparator.comparingInt(ClusterStatusResponse::getClusterNo));

    return new UserClusterSummaryResponse(
        "Successfully fetched", completed, ongoing, notStarted, underreview, cceMessage, payload
        // Will be null if CCE data is fetched successfully
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
                                ExternalClusterStatusResponse::getStatus
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

        // 1️⃣ Validate zone
        TblMasterZone zone = tblMasterZoneRepository.findById(zoneId)
                .orElseThrow(() -> new NoSuchElementException("Zone not found"));

        Long zoneKey = Long.valueOf(zone.getZoneId());

        // 2️⃣ Fetch clusters
        List<ClusterMaster> clusters =
                clusterMasterRepository.findAllByZoneIdAndIsRejectFalse(Math.toIntExact(zoneKey));

        // 3️⃣ Call external API (SAFE)
        List<ExternalClusterStatusResponse> externalStatus;
        try {
            externalStatus = formEntryClient.fetchClusterStatus(zoneId);
        } catch (FeignException.InternalServerError ex) {
            // Business meaning: no form entry exists
            log.warn("No form entry found for zoneId {}. Treating all clusters as NOT STARTED", zoneId);
            externalStatus = Collections.emptyList();
        }

        // 4️⃣ Group by clusterId
        Map<Long, List<ExternalClusterStatusResponse>> clusterSeasonMap =
                externalStatus.stream()
                        .collect(Collectors.groupingBy(
                                ExternalClusterStatusResponse::getClusterId
                        ));

        // 5️⃣ CCE logic (unchanged)
        CcePlotResult cceResult = cceCropService.getAssignedCcePlotsByZoneId(zoneKey);
        String cceMessage = cceResult.isFallbackUsed()
                ? "CCE data not available currently."
                : null;

        Map<Long, Set<String>> cropMap = new HashMap<>();
        for (AvailableCcePlotResponse plot : cceResult.getPlots()) {
            if (plot.getCropId() != null &&
                    "random".equalsIgnoreCase(plot.getCceSourceType())) {
                cropMap.computeIfAbsent(plot.getClusterId(), k -> new HashSet<>())
                        .add(plot.getCropName());
            }
        }

        int completed = 0, ongoing = 0, notStarted = 0, underreview = 0;
        List<ClusterStatusResponse> payload = new ArrayList<>();

        // 6️⃣ Build response per cluster
        for (ClusterMaster cluster : clusters) {

            Long clusterId = cluster.getCluMasterId();

            // 🔥 Season handling (ALL edge cases covered)
            List<SeasonStatusDto> seasonStatusList =
                    buildSeasonStatus(clusterId, clusterSeasonMap);

            // 🔥 Derive cluster-level status
            String clusterStatus = "NOT STARTED";

            if (seasonStatusList.stream()
                    .anyMatch(s -> "ON GOING".equalsIgnoreCase(s.getStatus()))) {
                clusterStatus = "ON GOING";
            } else if (seasonStatusList.stream()
                    .anyMatch(s -> "UNDER REVIEW".equalsIgnoreCase(s.getStatus()))) {
                clusterStatus = "UNDER REVIEW";
            } else if (!seasonStatusList.isEmpty() &&
                    seasonStatusList.stream()
                            .allMatch(s -> "COMPLETED".equalsIgnoreCase(s.getStatus()))) {
                clusterStatus = "COMPLETED";
            }

            // 7️⃣ Count summary
            switch (clusterStatus) {
                case "ON GOING" -> ongoing++;
                case "UNDER REVIEW" -> underreview++;
                case "NOT STARTED" -> notStarted++;
                default -> completed++;
            }

            // 8️⃣ Existing mapping
            var keyPlot = cluster.getKeyPlot();
            var btr = keyPlot.getBtrData();

            String svNo = btr.getResvno() + "/" + btr.getResbdno();
            String localbodyCode = btr.getLbcode();
            Double area = btr.getTotCent();

            String villageName = tblMasterVillageRepository
                    .findFirstByLsgCode(btr.getLsgcode())
                    .map(TblMasterVillage::getVillageNameEn)
                    .orElse("Village not found");

            String localBodyName = localBodyRepository.findByCodeApi(localbodyCode)
                    .map(lb -> {
                        String type = localBodyTypeRepository
                                .findById(lb.getLocalbodyType().longValue())
                                .map(LocalBodyType::getName)
                                .orElse("");
                        return lb.getLocalbodyNameEn() + " " + type;
                    })
                    .orElse("Local body not found");

            List<String> cropList =
                    new ArrayList<>(cropMap.getOrDefault(clusterId, Set.of()));

            payload.add(
                    new ClusterStatusResponse(
                            cluster.getClusterNumber(),
                            keyPlot.getId(),
                            !cropList.isEmpty(),
                            villageName,
                            btr.getVcode(),
                            localBodyName,
                            localbodyCode,
                            btr.getBcode(),
                            svNo,
                            area,
                            clusterId,
                            keyPlot.getLandType() != null
                                    ? keyPlot.getLandType().toLowerCase()
                                    : "unknown",
                            clusterStatus,
                            seasonStatusList,
                            cropList
                    )
            );
        }

        payload.sort(Comparator.comparingInt(ClusterStatusResponse::getClusterNo));

        // 9️⃣ Final response
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


//    public Map<String, Object> updateClusterNumber(Long keyplotId, Integer newClusterNumber, String userId) {
//        Map<String, Object> response = new HashMap<>();
//
//        // Find the keyplot and its cluster
//        KeyPlots keyPlot = keyPlotsRepository.findById(keyplotId)
//                .orElseThrow(() -> new RuntimeException("KeyPlot not found"));
//
//        ClusterMaster sourceCluster = clusterMasterRepository.findByKeyPlot(keyPlot)
//                .orElseThrow(() -> new RuntimeException("Cluster not found"));
//
//        Integer oldClusterNumber = sourceCluster.getClusterNumber();
//
//        // If same number, no change needed
//        if (oldClusterNumber.equals(newClusterNumber)) {
//            response.put("status", "No Change");
//            response.put("message", "Cluster number is already " + newClusterNumber);
//            return response;
//        }
//
//        // Check if new cluster number is within valid range (1-100)
//        if (newClusterNumber < 1 || newClusterNumber > 100) {
//            throw new RuntimeException("Cluster number must be between 1 and 100");
//        }
//
//        // Check if new cluster number is already taken in the same zone and agricultural year
//        Optional<ClusterMaster> existingCluster = clusterMasterRepository
//                .findByZoneAndClusterNumberAndAgriYear(
//                        keyPlot.getZone().getZoneId(),
//                        newClusterNumber,
//                        keyPlot.getAgriStartYear(),
//                        keyPlot.getAgriEndYear()
//                );
//
//        if (existingCluster.isPresent()) {
//            ClusterMaster targetCluster = existingCluster.get();
//
//            // Option 1: Auto-swap numbers
//            // Set the target cluster's number to the old number
//            targetCluster.setClusterNumber(oldClusterNumber);
//            clusterMasterRepository.save(targetCluster);
//
//            // Set the source cluster to the new number
//            sourceCluster.setClusterNumber(newClusterNumber);
//            clusterMasterRepository.save(sourceCluster);
//
//            response.put("status", "Swapped");
//            response.put("message", String.format(
//                    "Cluster numbers swapped: %d ↔ %d", oldClusterNumber, newClusterNumber));
//            response.put("swappedWith", targetCluster.getKeyPlot().getId());
//        } else {
//            // Simple update if number is free
//            sourceCluster.setClusterNumber(newClusterNumber);
//            clusterMasterRepository.save(sourceCluster);
//
//            response.put("status", "Updated");
//            response.put("message", "Cluster number updated successfully");
//        }
//
//        response.put("oldNumber", oldClusterNumber);
//        response.put("newNumber", newClusterNumber);
//        return response;
//    }
//
//    // Alternative: Manual conflict resolution with temporary number
//    public Map<String, Object> updateClusterNumberWithTemp(
//            Long keyplotId,
//            Integer newClusterNumber,
//            Integer tempClusterNumber,
//            String userId) {
//
//        Map<String, Object> response = new HashMap<>();
//
//        KeyPlots keyPlot = keyPlotsRepository.findById(keyplotId)
//                .orElseThrow(() -> new RuntimeException("KeyPlot not found"));
//
//        ClusterMaster sourceCluster = clusterMasterRepository.findByKeyPlot(keyPlot)
//                .orElseThrow(() -> new RuntimeException("Cluster not found"));
//
//        // Validate numbers
//        if (newClusterNumber < 1 || newClusterNumber > 100) {
//            throw new RuntimeException("Cluster number must be between 1 and 100");
//        }
//
//        if (tempClusterNumber != null && (tempClusterNumber < 1 || tempClusterNumber > 100)) {
//            throw new RuntimeException("Temporary cluster number must be between 1 and 100");
//        }
//
//        // Check if target is taken
//        Optional<ClusterMaster> targetCluster = clusterMasterRepository
//                .findByZoneAndClusterNumberAndAgriYear(
//                        keyPlot.getZone().getZoneId(),
//                        newClusterNumber,
//                        keyPlot.getAgriStartYear(),
//                        keyPlot.getAgriEndYear()
//                );
//
//        if (targetCluster.isPresent() && !targetCluster.get().getKeyPlot().getId().equals(keyplotId)) {
//            if (tempClusterNumber == null) {
//                // Require temporary number
//                response.put("status", "CONFLICT");
//                response.put("message", "Cluster number " + newClusterNumber + " is already taken");
//                response.put("conflictingKeyplotId", targetCluster.get().getKeyPlot().getId());
//                response.put("conflictingSyNo", targetCluster.get().getKeyPlot().getBtrData().getResvno());
//                return response;
//            } else {
//                // Check if temporary number is available
//                Optional<ClusterMaster> tempCheck = clusterMasterRepository
//                        .findByZoneAndClusterNumberAndAgriYear(
//                                keyPlot.getZone().getZoneId(),
//                                tempClusterNumber,
//                                keyPlot.getAgriStartYear(),
//                                keyPlot.getAgriEndYear()
//                        );
//
//                if (tempCheck.isPresent()) {
//                    throw new RuntimeException("Temporary number " + tempClusterNumber + " is also taken");
//                }
//
//                // Move conflicting cluster to temporary number
//                ClusterMaster conflictingCluster = targetCluster.get();
//                conflictingCluster.setClusterNumber(tempClusterNumber);
//                clusterMasterRepository.save(conflictingCluster);
//
//                // Set our cluster to new number
//                sourceCluster.setClusterNumber(newClusterNumber);
//                clusterMasterRepository.save(sourceCluster);
//
//                response.put("status", "Updated with temp");
//                response.put("message", String.format(
//                        "Cluster number updated. Conflicting cluster moved to %d", tempClusterNumber));
//            }
//        } else {
//            // Simple update
//            sourceCluster.setClusterNumber(newClusterNumber);
//            clusterMasterRepository.save(sourceCluster);
//
//            response.put("status", "Updated");
//            response.put("message", "Cluster number updated successfully");
//        }
//
//        return response;
//    }


  //    public List<VillagesListResponse> getVillagesListByKeyPlotId(UUID userId) {
  //        Optional<UserZoneAssignment> user = userZoneAssignmentRepositoty.findByUserId(userId);
  //
  //        List<TblZoneRevenueVillageMapping> zoneRevenueList =
  //                tblZoneRevenueVillageMappingRepository.findByZone(
  //                        Math.toIntExact(user.get().getTblMasterZone().getZoneId())
  //                );
  //
  //        List<Integer> villageIds = zoneRevenueList.stream()
  //                .map(TblZoneRevenueVillageMapping::getRevenueVillage)
  //                .collect(Collectors.toList());
  //
  //        List<TblMasterVillage> villageList = tblMasterVillageRepository.findAllById(villageIds);
  //
  //        return villageList.stream().map(v -> {
  //            List<TblMasterVillageBlock> blocks =
  // tblMasterVillageBlockRepository.findByVillageId(v.getVillageId());
  //
  //            List<BlockCodeResponse> blockCodes = blocks.stream()
  //                    .map(b -> new BlockCodeResponse(b.getBlockCode()))
  //                    .collect(Collectors.toList());
  //
  //            return new VillagesListResponse(
  //                    v.getVillageNameEn(),
  //                    Long.valueOf(v.getVillageId()),
  //                    blockCodes
  //            );
  //        }).collect(Collectors.toList());
  //    }

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
  //    public ResbdnoListReponse getResbdnoAreaList(UUID kpId, Integer villageId, String blockCode,
  // Integer resvnoOpt) {
  //        System.out.println("okkk " + resvnoOpt);
  //
  //        // 1. Fetch KeyPlot
  //        KeyPlots keyPlot = keyPlotsRepository.findById(kpId)
  //                .orElseThrow(() -> new EntityNotFoundException("KeyPlot not found for id: " +
  // kpId));
  //
  //        String ltype = "Wet".equalsIgnoreCase(keyPlot.getLandType()) ? "W" : "D";
  //        String lbcode = keyPlot.getBtrDataOld().getLbcode();
  //
  //        Integer excludedResvno = keyPlot.getBtrDataOld().getResvno();
  //        Integer excludedResbdno = keyPlot.getBtrDataOld().getResbdno();
  //
  //        // 2. Fetch Village
  //        TblMasterVillage village = tblMasterVillageRepository.findById(villageId)
  //                .orElseThrow(() -> new EntityNotFoundException("Village not found for id: " +
  // villageId));
  //
  //        Integer lsgcode = village.getLsgCode();
  //        String villageName = village.getVillageNameMal();
  //
  //        // 3. Query for plots
  //        List<TblBtrDataOld> matchedPlots;
  //        if (resvnoOpt != null) {
  //            matchedPlots =
  // tblBtrDataOldRepository.findByLsgcodeAndBcodeAndLtypeAndResvno(lsgcode, blockCode, ltype,
  // resvnoOpt);
  //        } else {
  //            matchedPlots = tblBtrDataOldRepository.findByLsgcodeAndBcodeAndLtype(lsgcode,
  // blockCode, ltype);
  //        }
  //
  //        // 4. Filter out the reserved combination
  //        List<ResbdnoAreaResponse> details = matchedPlots.stream()
  //                .filter(p -> !(Objects.equals(p.getResvno(), excludedResvno) &&
  //                        Objects.equals(p.getResbdno(), excludedResbdno))) // Exclude exact match
  //                .map(p -> new ResbdnoAreaResponse(p.getResvno(), p.getResbdno(), p.getArea(),
  // p.getId()))
  //                .collect(Collectors.toList());
  //
  //        // 5. Prepare response
  //        return new ResbdnoListReponse(
  //                kpId,
  //                lbcode,
  //                resvnoOpt,
  //                villageName,
  //                blockCode,
  //                details
  //        );
  //    }

  //    public ResbdnoListReponse getResbdnoList(UUID kpId, Integer resvno) {
  //        // Step 1: Load KeyPlot
  //        KeyPlots keyPlot = keyPlotsRepository.findById(kpId)
  //                .orElseThrow(() -> new EntityNotFoundException("KeyPlot not found: " + kpId));
  //
  //        // Step 2: Extract lbcode and ltype
  //        String lbcode = keyPlot.getBtrDataOld().getLbcode();
  //        String ltype = "Wet".equalsIgnoreCase(keyPlot.getLandType()) ? "W" : "D";
  //
  //        // Step 3: Fetch matching plots based on resvno filter
  //        List<TblBtrDataOld> matchedPlots = (resvno != null)
  //                ? tblBtrDataOldRepository.findAllByLbcodeAndResvnoAndLtype(lbcode, resvno,
  // ltype)
  //                : tblBtrDataOldRepository.findAllByLbcodeAndLtype(lbcode, ltype);
  //
  //        // Step 4: Extract distinct resbdno values
  //        List<Integer> resbdnos = matchedPlots.stream()
  //                .map(TblBtrDataOld::getResbdno)
  //                .filter(Objects::nonNull)
  //                .distinct()
  //                .collect(Collectors.toList());
  //
  //        // Step 5: Return the response
  //        return new ResbdnoListReponse(kpId, lbcode, resvno, resbdnos);
  //    }
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

//  @Transactional
//  public void saveClusterData(
//      UUID userid, UUID keyplotId, Integer clusterNo, String status, String remarks,List<SidePlotDTO> sidePlots) {
//
//
//    KeyPlots keyPlot =
//        keyPlotsRepository
//            .findById(keyplotId)
//            .orElseThrow(() -> new RuntimeException("KeyPlot not found with ID: " + keyplotId));
//
//    Optional<ClusterLimitLog> currentActiveOpt = clusterLimitLogRepository.findByInActiveTrue();
//    BigDecimal clustermin = currentActiveOpt.map(ClusterLimitLog::getClusterMin).orElse(null);
//    BigDecimal clustermax = currentActiveOpt.map(ClusterLimitLog::getClusterMax).orElse(null);
//    BigDecimal tsoclusterlimit =
//        currentActiveOpt.map(ClusterLimitLog::getTsoApprovalLimit).orElse(null);
//    ClusterMaster clusterMaster =
//        clusterMasterRepository
//            .findByKeyPlotId(keyPlot.getId())
//            .orElseGet(
//                () -> {
//                  // If not found, create new ClusterMaster
//                  ClusterMaster newCluster = new ClusterMaster();
//                  newCluster.setKeyPlot(keyPlot);
//                  //                    newCluster.setClusterNo(clusterNo); // Set the clusterNo
//                  // here for new master
//                  newCluster.setStatus("On Going");
//                  newCluster.setIsReject(false);
//                  newCluster.setIs_active(true);
//                  newCluster.setCreatedAt(LocalDateTime.now());
//                  newCluster.setUpdatedAt(LocalDateTime.now());
//                  return newCluster;
//                });
//
//    // Update existing ClusterMaster properties
//    //        clusterMaster.setClusterNo(clusterNo); // Always update clusterNo
//    double totalEnumeratedArea =
//        sidePlots.stream()
//            .flatMap(sp -> sp.getRows().stream())
//            .mapToDouble(row -> row.getActual() != null ? row.getActual() : 0.0)
//            .sum();
//
//    // 🔹 Decide status based on limits
//
//    String status;
//    System.out.println("total " + totalEnumeratedArea);
//    if (clustermax != null && BigDecimal.valueOf(totalEnumeratedArea).compareTo(clustermax) > 0) {
//      throw new RuntimeException("Maximum limit exceeded, please reduce the size.");
//    } else if (clustermin != null
//        && BigDecimal.valueOf(totalEnumeratedArea).compareTo(clustermin) < 0) {
//      status = "On Going";
//
//    } else if (tsoclusterlimit != null
//        && BigDecimal.valueOf(totalEnumeratedArea).compareTo(tsoclusterlimit) < 0) {
//      status = "Under Review";
//      ClusterApprovalLog clusterApprovalLog = new ClusterApprovalLog();
//      clusterApprovalLog.setClusterMaster(clusterMaster);
//      clusterApprovalLog.setAddedBy(userid);
//      clusterApprovalLog.setZone(clusterMaster.getZone());
//      clusterApprovalLog.setRemarks("Cluster is Not meet the approval limit");
//      clusterApprovalLog.setTotalArea(BigDecimal.valueOf(totalEnumeratedArea));
//      clusterApprovalRepository.save(clusterApprovalLog);
//
//    } else {
//      status = "Completed";
//    }
//
//    // 🔹 Apply status & save ClusterMaster
//    clusterMaster.setStatus(status);
//    clusterMaster.setUpdatedAt(LocalDateTime.now());
//    ClusterMaster savedCluster = clusterMasterRepository.save(clusterMaster);
//
//    // --- Data Management Logic ---
//
//    // 1. Get existing ClusterFormData for this ClusterMaster
//    List<ClusterFormData> existingFormData =
//        clusterFormDataRepository.findByClusterMaster(savedCluster);
//    Map<String, ClusterFormData> existingFormDataMap =
//        existingFormData.stream()
//            .collect(
//                    Collectors.toMap(
//                            data -> data.getPlot().getId().toString() + "_" + data.getPlotLabel(),
//                            data -> data,
//                            (existing, duplicate) -> {
//                              // Keep the latest one OR whichever you want
//                              return existing;  // ignore duplicate
//                            }
//                    ));
//
//    // Create a set of submitted unique keys for efficient lookup
//    Set<String> submittedKeys = new HashSet<>();
//
//    // 2. Process submitted side plots: Add new or Update existing
//    for (SidePlotDTO sidePlot : sidePlots) {
//      for (ClusterFormRowDTO row : sidePlot.getRows()) {
//        Long currentPlotId = row.getPlot_id();
//        System.out.println("village   "+row.getVillage());
//        String currentPlotLabel = sidePlot.getLabel();
//        String uniqueKey = currentPlotId.toString() + "_" + currentPlotLabel;
//        submittedKeys.add(uniqueKey); // Add to submitted keys set
//
//        TblBtrData plot =
//            tblBtrDataRepository
//                .findById(currentPlotId)
//                //                        .orElseThrow(() -> new RuntimeException("Plot not found
//                // for ID: " + currentPlotId));
//                .orElseGet(
//                    () -> {
//                      // Create new TblBtrData if not found
//                      TblBtrData newPlot = new TblBtrData();
//                      System.out.println("villages" + row.getVillage());
//                      // Set basic properties from the row data
//                      newPlot.setResvno(row.getSvNo());
//                      newPlot.setResbdno(row.getSubNo());
//                      newPlot.setBcode(row.getBcode());
//                      newPlot.setTotCent(row.getArea());
//                      newPlot.setLtype(keyPlot.getLandType());
//                      // Get additional properties from keyPlot for consistency
//                      TblBtrData keyPlotBtrData = keyPlot.getBtrData();
//                      newPlot.setDcode(keyPlotBtrData.getDcode());
//                      newPlot.setTcode(keyPlotBtrData.getTcode());
//                      newPlot.setVcode(Integer.valueOf(row.getVillage())); // m
//                      newPlot.setBtrtype(keyPlotBtrData.getBtrtype());
//                      newPlot.setAddress(row.getAddress());
//                      newPlot.setOwnername(row.getOwnername());
//                      newPlot.setHouseno(row.getHouseno());
//                      newPlot.setOldsvno(row.getOldsvno());
//                      newPlot.setOldsubno(row.getOldsubno());
//                      newPlot.setTpno(row.getTpno());
//                      newPlot.setTbsubdivisionno(row.getTbsubdivisionno());
//                      newPlot.setWardnumber(row.getWard_number());
//                      //
//                      // newPlot.setLbtype(keyPlotBtrData.getLbtype());//venda
//                      newPlot.setLbcode(keyPlotBtrData.getLbcode());
//                      //
//                      // newPlot.setGovpriv(keyPlotBtrData.getGovpriv());//venda
//                      newPlot.setLtype(keyPlotBtrData.getLtype()); // done
//                      //                            newPlot.setLanduse(keyPlotBtrData.getLanduse());
//                      // //venda
//                      Optional<TblMasterVillage> lsg =
//                          tblMasterVillageRepository.findById(Integer.valueOf(row.getVillage()));
//                      newPlot.setLsgcode(lsg.get().getLsgCode()); // m
//
//                      // Set default values for optional fields
//                      //                            newPlot.setNhect(0.0);
//                      //                            newPlot.setNare(0.0);
//                      //                            newPlot.setNsqm(0.0);
//                      //                            newPlot.setEast(0.0);
//                      //                            newPlot.setWest(0.0);
//                      //                            newPlot.setNorth(0.0);
//                      //                            newPlot.setSouth(0.0);
//
//                      log.info(
//                          "Creating new TblBtrData for plot_id: {} with svNo: {} and subNo: {}",
//                          currentPlotId,
//                          row.getSvNo(),
//                          row.getSubNo());
//
//                      return tblBtrDataRepository.save(newPlot);
//                    });
//
//        Double enumeratedArea = row.getActual();
//        if (enumeratedArea == null) {
//          // Handle cases where 'actual' might be null or not a valid number
//          // Based on your frontend, it looks like 'enumeratedArea' is what's editable.
//          // Let's assume 'actual' in DTO maps to 'enumeratedArea' in entity.
//          throw new RuntimeException(
//              "Enumerated area cannot be null for plot ID: " + currentPlotId);
//        }
//
//        ClusterFormData formData;
//        if (existingFormDataMap.containsKey(uniqueKey)) {
//          // Update existing entry
//          formData = existingFormDataMap.get(uniqueKey);
//          formData.setEnumeratedArea(enumeratedArea);
//          formData.setUpdatedAt(LocalDateTime.now());
//          // Remove from map to mark it as processed
//          existingFormDataMap.remove(uniqueKey);
//        } else {
//          // Add new entry
//          formData = new ClusterFormData();
//          formData.setClusterMaster(savedCluster);
//          formData.setPlot(plot);
//          formData.setPlotLabel(currentPlotLabel);
//          formData.setEnumeratedArea(enumeratedArea);
//          formData.setStatus(true); // Assuming true for new entries
//          formData.setCreatedAt(LocalDateTime.now());
//          formData.setUpdatedAt(LocalDateTime.now());
//          formData.setCreatedBy(userid);
//        }
//        clusterFormDataRepository.save(formData); // Save or update
//      }
//    }
//
//    // 3. Delete old ClusterFormData entries that are no longer submitted
//    // Any remaining entries in existingFormDataMap were not in the current submission
//    clusterFormDataRepository.deleteAll(existingFormDataMap.values());
//  }

  @Transactional
  public void saveClusterData(
          UUID userid,
          UUID keyplotId,
          Integer clusterNo,
          String requestedStatus,   // On Going | Under Review | COMPLETED
          String remarks,
          List<SidePlotDTO> sidePlots
  ) {

   System.out.println("status :::  "+requestedStatus);
   System.out.println("reddd  "+sidePlots);
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



  @Transactional
  public void deleteClusterFormDataById(Long id) {

    ClusterFormData formData =
        clusterFormDataRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("ClusterFormData not found with ID: " + id));
    clusterFormDataRepository.delete(formData);
  }


  public CropReplaceClusterResponse getNextCluster(CropReplaceClusterRequest request) {
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

    System.out.println("is reject: " + isAlreadyRejected);
    if (isAlreadyRejected) {
      return new CropReplaceClusterResponse(null, null, "Cluster already rejected by user.");
    }

    // 4. Find the CURRENT assignment for this crop (the one we're replacing)
    Optional<CropAssignmentTrail> currentAssignmentOpt =
            cropAssignmentTrailRepository.findByCropIdAndCluster_CluMasterIdAndIsCurrentAssignmentTrue(request.getCropId(),request.getClusterId());

    // 5. Fetch next valid cluster
    List<ClusterMaster> nextClusters =
            clusterMasterRepository.findNextClusterFlexibleLandType(
                    Math.toIntExact(request.getZoneId()), landType, currentClusterNumber);

    System.out.println("Next clusters found: " + nextClusters.size());

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
    System.out.println("request   "+request);
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
    System.out.println("request>> "+keyPlot.getBtrData().getBtrtype().getBTypeId());
    System.out.println("village  "+request.getVillage());
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
    System.out.println("lsg   "+lsg);
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

  //    @Autowired
  //    public ClusterService(ClusterMasterRepository clusterMasterRepository,
  //                          ClusterFormDataRepository clusterFormDataRepository,
  //                          KeyPlotsRepository keyPlotsRepository) {
  //        this.clusterMasterRepository = clusterMasterRepository;
  //        this.clusterFormDataRepository = clusterFormDataRepository;
  //        this.keyPlotsRepository = keyPlotsRepository;
  //    }
  //
  //    @Transactional
  //    public void saveClusterData(ClusterSaveRequest request) {
  //        // 1. Validate and fetch KeyPlots entity based on syNo from keyplotDetails
  //        String syNo = request.getKeyplotDetails() != null ?
  // request.getKeyplotDetails().getSyNo() : null;
  //        if (syNo == null || syNo.trim().isEmpty()) {
  //            throw new IllegalArgumentException("Keyplot SY.No. is required to save cluster
  // data.");
  //        }
  //
  //        Optional<KeyPlots> keyPlotsOptional = keyPlotsRepository.findBySyNo(syNo);
  //        if (keyPlotsOptional.isEmpty()) {
  //            // Handle case where KeyPlots with given syNo does not exist
  //            // You might want to create it, or throw an error depending on your business logic
  //            throw new RuntimeException("KeyPlot with SY.No: " + syNo + " not found. Please
  // ensure it exists.");
  //        }
  //        KeyPlots keyPlots = keyPlotsOptional.get();
  //
  //        // 2. Process each side plot (which maps to a ClusterMaster entry)
  //        if (request.getKeyplots() != null) {
  //            for (SidePlotDTO sidePlotDTO : request.getKeyplots()) {
  //                // Create and save ClusterMaster
  //                ClusterMaster clusterMaster = new ClusterMaster();
  //                clusterMaster.setKeyPlots(keyPlots); // Link to the main KeyPlots
  //                clusterMaster.setLabel(sidePlotDTO.getLabel()); // Set side plot label (e.g.,
  // "W", "W1")
  //                clusterMaster.setStatus(true); // Default status
  //                clusterMaster.setReject(false); // Default reject
  //                clusterMaster.setRemark(null); // No remark from UI
  //
  //                clusterMaster = clusterMasterRepository.save(clusterMaster); // Save to get the
  // generated ID
  //
  //                // 3. Process each row within the side plot (which maps to ClusterFormData
  // entries)
  //                if (sidePlotDTO.getRows() != null) {
  //                    for (ClusterFormRowDTO rowDTO : sidePlotDTO.getRows()) {
  //                        // Handle incomplete data: convert empty strings to null for Double
  //                        Double area = null;
  //                        if (rowDTO.getArea() != null && !rowDTO.getArea().trim().isEmpty()) {
  //                            try {
  //                                area = Double.parseDouble(rowDTO.getArea());
  //                                if (area < 0) { // UI already handles this, but good to have
  // server-side validation
  //                                    throw new IllegalArgumentException("Area cannot be
  // negative.");
  //                                }
  //                            } catch (NumberFormatException e) {
  //                                // Log or handle invalid number format for area
  //                                System.err.println("Invalid number format for area: " +
  // rowDTO.getArea());
  //                                // 'area' remains null
  //                            }
  //                        }
  //
  //                        ClusterFormData formData = new ClusterFormData();
  //                        formData.setClusterMaster(clusterMaster); // Link to the current
  // ClusterMaster
  //                        formData.setSvNo(rowDTO.getSvNo());
  //                        formData.setSub(rowDTO.getSub());
  //                        formData.setBlock(rowDTO.getBlock());
  //                        formData.setActual(rowDTO.getActual());
  //                        formData.setArea(area); // Saved as Double, null if invalid/empty
  //                        formData.setStatus(true); // Default status
  //
  //                        clusterFormDataRepository.save(formData);
  //                    }
  //                }
  //            }
  //        }
  //        // You can also save wardNumber, wetDry, keyplotType, reserveKeyplot if you extend
  // KeyPlots or create a new Cluster entity.
  //        // For now, these are not mapped to the provided entities.
  //    }
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


    public List<BtrClusterUsageResponse> getBtrClusterUsage(Long btrId) {
        return clusterFormDataRepository.findClusterUsageByBtrId(btrId);
    }

    public ClusterTourResponse getClusterDetails(Long clusterId) {

        ClusterMaster cluster = clusterMasterRepository.findById(clusterId)
                .orElseThrow(() -> new RuntimeException("Cluster not found"));

        ClusterTourResponse response = new ClusterTourResponse();

        response.setClusterNo(cluster.getClusterNumber());
        response.setZoneName(cluster.getZone().getZoneNameEn());
        response.setLandType(cluster.getKeyPlot().getLandType());

        return response;
    }
}
