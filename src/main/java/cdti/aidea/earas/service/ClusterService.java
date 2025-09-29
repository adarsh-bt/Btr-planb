package cdti.aidea.earas.service;

import cdti.aidea.earas.config.FormEntryClient;
import cdti.aidea.earas.contract.FormEntryDto.*;
import cdti.aidea.earas.contract.Response.*;
import cdti.aidea.earas.model.Btr_models.*;
import cdti.aidea.earas.model.Btr_models.Masters.*;
import cdti.aidea.earas.repository.Btr_repo.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    // Step 1: Get CCE plot assignments with fallback awareness
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

    // Step 2: Build cluster summary

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
      Double keyplot_area = cluster.getKeyPlot().getBtrData().getTotCent();
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

      payload.add(
          new ClusterStatusResponse(
              cluster.getClusterNumber(),
              keyplotId,
              isCce,
              villageName,
              localBodyName,
              keyplot_lbcode,
              keyplot_svno,
              keyplot_area,
              clusterId,
              landType != null ? landType.toLowerCase() : "unknown",
              status,
              new ArrayList<>(cropNames) // Pass the crop names list here
              ));
    }

    payload.sort(Comparator.comparingInt(ClusterStatusResponse::getClusterNo));

    return new UserClusterSummaryResponse(
        "Successfully fetched", completed, ongoing, notStarted, underreview, cceMessage, payload
        // Will be null if CCE data is fetched successfully
        );
  }
  //    cluster data for App
  public Map<String, Object> getGroupedFormDataByClusterId(Long clusterId) {
    ClusterMaster clusterMaster =
        clusterMasterRepository
            .findById(clusterId)
            .orElseThrow(() -> new RuntimeException("Cluster not found"));

    List<ClusterFormData> formDataList =
        clusterFormDataRepository.findByClusterMaster(clusterMaster);

    Map<String, List<Map<String, Object>>> labelToPlotsMap = new LinkedHashMap<>();
    Map<String, Double> labelToTotalAreaMap = new LinkedHashMap<>();

    for (ClusterFormData data : formDataList) {
      String label = data.getPlotLabel();
      TblBtrData plot = data.getPlot();

      double area = data.getEnumeratedArea() != null ? data.getEnumeratedArea() : 0.0;

      Map<String, Object> plotInfo = new HashMap<>();
      plotInfo.put("cluster_plot_id", data.getCluDetailId());
      plotInfo.put("svno", plot.getResvno() + "/" + plot.getResbdno());
      plotInfo.put("area", area);

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
          crops.add(crop);
        }
      }
    }

    Map<String, Object> response = new HashMap<>();
    response.put("clusterId", clusterId);
    response.put("labels", labelToPlotsMap);
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

  @Transactional
  public void saveClusterData(
      UUID userid, UUID keyplotId, Integer clusterNo, List<SidePlotDTO> sidePlots) {

    System.out.println(">>>>>>>>>>>>> " + sidePlots);
    KeyPlots keyPlot =
        keyPlotsRepository
            .findById(keyplotId)
            .orElseThrow(() -> new RuntimeException("KeyPlot not found with ID: " + keyplotId));

    Optional<ClusterLimitLog> currentActiveOpt = clusterLimitLogRepository.findByInActiveTrue();
    BigDecimal clustermin = currentActiveOpt.map(ClusterLimitLog::getClusterMin).orElse(null);
    BigDecimal clustermax = currentActiveOpt.map(ClusterLimitLog::getClusterMax).orElse(null);
    BigDecimal tsoclusterlimit =
        currentActiveOpt.map(ClusterLimitLog::getTsoApprovalLimit).orElse(null);
    ClusterMaster clusterMaster =
        clusterMasterRepository
            .findByKeyPlotId(keyPlot.getId())
            .orElseGet(
                () -> {
                  // If not found, create new ClusterMaster
                  ClusterMaster newCluster = new ClusterMaster();
                  newCluster.setKeyPlot(keyPlot);
                  //                    newCluster.setClusterNo(clusterNo); // Set the clusterNo
                  // here for new master
                  newCluster.setStatus("On Going");
                  newCluster.setIsReject(false);
                  newCluster.setIs_active(true);
                  newCluster.setCreatedAt(LocalDateTime.now());
                  newCluster.setUpdatedAt(LocalDateTime.now());
                  return newCluster;
                });

    // Update existing ClusterMaster properties
    //        clusterMaster.setClusterNo(clusterNo); // Always update clusterNo
    double totalEnumeratedArea =
        sidePlots.stream()
            .flatMap(sp -> sp.getRows().stream())
            .mapToDouble(row -> row.getActual() != null ? row.getActual() : 0.0)
            .sum();

    // 🔹 Decide status based on limits

    String status;
    if (clustermax != null && BigDecimal.valueOf(totalEnumeratedArea).compareTo(clustermax) > 0) {
      throw new RuntimeException("Maximum limit exceeded, please reduce the size.");
    } else if (clustermin != null
        && BigDecimal.valueOf(totalEnumeratedArea).compareTo(clustermin) < 0) {
      status = "On Going";

    } else if (tsoclusterlimit != null
        && BigDecimal.valueOf(totalEnumeratedArea).compareTo(tsoclusterlimit) < 0) {
      status = "Under Review";
      ClusterApprovalLog clusterApprovalLog = new ClusterApprovalLog();
      clusterApprovalLog.setClusterMaster(clusterMaster);
      clusterApprovalLog.setAddedBy(userid);
      clusterApprovalLog.setZone(clusterMaster.getZone());
      clusterApprovalLog.setRemarks("Cluster is Not meet the approval limit");
      clusterApprovalLog.setTotalArea(BigDecimal.valueOf(totalEnumeratedArea));
      clusterApprovalRepository.save(clusterApprovalLog);

    } else {
      status = "Completed";
    }

    // 🔹 Apply status & save ClusterMaster
    clusterMaster.setStatus(status);
    clusterMaster.setUpdatedAt(LocalDateTime.now());
    ClusterMaster savedCluster = clusterMasterRepository.save(clusterMaster);

    // --- Data Management Logic ---

    // 1. Get existing ClusterFormData for this ClusterMaster
    List<ClusterFormData> existingFormData =
        clusterFormDataRepository.findByClusterMaster(savedCluster);

    // Create a map for quick lookup of existing data by a unique key (e.g., plotId + plotLabel)
    // This assumes plot_id + plotLabel uniquely identifies a side plot entry within a cluster.
    // If not, you'll need a more robust unique key.
    Map<String, ClusterFormData> existingFormDataMap =
        existingFormData.stream()
            .collect(
                Collectors.toMap(
                    data -> data.getPlot().getId().toString() + "_" + data.getPlotLabel(),
                    data -> data));

    // Create a set of submitted unique keys for efficient lookup
    Set<String> submittedKeys = new HashSet<>();

    // 2. Process submitted side plots: Add new or Update existing
    for (SidePlotDTO sidePlot : sidePlots) {
      for (ClusterFormRowDTO row : sidePlot.getRows()) {
        Long currentPlotId = row.getPlot_id();
        String currentPlotLabel = sidePlot.getLabel();
        String uniqueKey = currentPlotId.toString() + "_" + currentPlotLabel;
        submittedKeys.add(uniqueKey); // Add to submitted keys set

        TblBtrData plot =
            tblBtrDataRepository
                .findById(currentPlotId)
                //                        .orElseThrow(() -> new RuntimeException("Plot not found
                // for ID: " + currentPlotId));
                .orElseGet(
                    () -> {
                      // Create new TblBtrData if not found
                      TblBtrData newPlot = new TblBtrData();
                      System.out.println("villages" + row.getVillage());
                      // Set basic properties from the row data
                      newPlot.setResvno(row.getSvNo());
                      newPlot.setResbdno(row.getSubNo());
                      newPlot.setBcode(row.getBcode());
                      newPlot.setTotCent(row.getArea());

                      // Get additional properties from keyPlot for consistency
                      TblBtrData keyPlotBtrData = keyPlot.getBtrData();
                      newPlot.setDcode(keyPlotBtrData.getDcode());
                      newPlot.setTcode(keyPlotBtrData.getTcode());
                      newPlot.setVcode(Integer.valueOf(row.getVillage())); // m
                      //
                      // newPlot.setLbtype(keyPlotBtrData.getLbtype());//venda
                      newPlot.setLbcode(keyPlotBtrData.getLbcode());
                      //
                      // newPlot.setGovpriv(keyPlotBtrData.getGovpriv());//venda
                      newPlot.setLtype(keyPlotBtrData.getLtype()); // done
                      //                            newPlot.setLanduse(keyPlotBtrData.getLanduse());
                      // //venda
                      Optional<TblMasterVillage> lsg =
                          tblMasterVillageRepository.findById(Integer.valueOf(row.getVillage()));
                      newPlot.setLsgcode(lsg.get().getLsgCode()); // m

                      // Set default values for optional fields
                      //                            newPlot.setNhect(0.0);
                      //                            newPlot.setNare(0.0);
                      //                            newPlot.setNsqm(0.0);
                      //                            newPlot.setEast(0.0);
                      //                            newPlot.setWest(0.0);
                      //                            newPlot.setNorth(0.0);
                      //                            newPlot.setSouth(0.0);

                      log.info(
                          "Creating new TblBtrData for plot_id: {} with svNo: {} and subNo: {}",
                          currentPlotId,
                          row.getSvNo(),
                          row.getSubNo());

                      return tblBtrDataRepository.save(newPlot);
                    });

        Double enumeratedArea = row.getActual();
        if (enumeratedArea == null) {
          // Handle cases where 'actual' might be null or not a valid number
          // Based on your frontend, it looks like 'enumeratedArea' is what's editable.
          // Let's assume 'actual' in DTO maps to 'enumeratedArea' in entity.
          throw new RuntimeException(
              "Enumerated area cannot be null for plot ID: " + currentPlotId);
        }

        ClusterFormData formData;
        if (existingFormDataMap.containsKey(uniqueKey)) {
          // Update existing entry
          formData = existingFormDataMap.get(uniqueKey);
          formData.setEnumeratedArea(enumeratedArea);
          formData.setUpdatedAt(LocalDateTime.now());
          // Remove from map to mark it as processed
          existingFormDataMap.remove(uniqueKey);
        } else {
          // Add new entry
          formData = new ClusterFormData();
          formData.setClusterMaster(savedCluster);
          formData.setPlot(plot);
          formData.setPlotLabel(currentPlotLabel);
          formData.setEnumeratedArea(enumeratedArea);
          formData.setStatus(true); // Assuming true for new entries
          formData.setCreatedAt(LocalDateTime.now());
          formData.setUpdatedAt(LocalDateTime.now());
          formData.setCreatedBy(userid);
        }
        clusterFormDataRepository.save(formData); // Save or update
      }
    }

    // 3. Delete old ClusterFormData entries that are no longer submitted
    // Any remaining entries in existingFormDataMap were not in the current submission
    clusterFormDataRepository.deleteAll(existingFormDataMap.values());
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
    // 1. Fetch zone assignment
    Optional<UserZoneAssignment> zoneAssignmentOpt =
        userZoneAssignmentRepositoty.findByUserIdAndTblMasterZone_ZoneId(
            request.getUserId(), request.getZoneId());
    if (zoneAssignmentOpt.isEmpty()) {
      throw new RuntimeException("UserZone assignment not found for ID: " + request.getUserId());
    }

    // 2. Fetch current cluster
    Optional<ClusterMaster> currentClusterOpt =
        clusterMasterRepository.findById(request.getClusterId());
    if (currentClusterOpt.isEmpty()) {
      throw new RuntimeException("Cluster not found with ID: " + request.getClusterId());
    }

    ClusterMaster currentCluster = currentClusterOpt.get();
    Integer currentClusterNumber = currentCluster.getClusterNumber();

    // 3. Get cleaned land type
    String landType = request.getCropLandType().trim();

    // 3.5 Check if this cluster was already rejected for this crop by the same user
    boolean isAlreadyRejected =
        cropAssignmentTrailRepository
            .existsByCropIdAndCluster_CluMasterIdAndIsRejectedTrueAndRejectedBy(
                request.getCropId(), request.getClusterId(), request.getUserId());

    if (isAlreadyRejected) {
      return new CropReplaceClusterResponse(null, null, "Cluster already rejected by user.");
    }

    // 4. Fetch next valid cluster
    List<ClusterMaster> nextClusters =
        clusterMasterRepository.findNextClusterFlexibleLandType(
            Math.toIntExact(request.getZoneId()), landType, currentClusterNumber);

    if (nextClusters.isEmpty()) {
      // Optional: add trail entry showing rejection & exhausted case
      CropAssignmentTrail exhaustedTrail =
          CropAssignmentTrail.builder()
              .cropId(request.getCropId())
              .cluster(currentCluster)
              .keyPlot(currentCluster.getKeyPlot())
              .landType(landType)
              .isRejected(true)
              .zoneId(request.getZoneId())
              .isLimitExceeded(true)
              .rejectionReason("All clusters exhausted")
              .rejectedBy(request.getUserId())
              .rejectedAt(LocalDateTime.now())
              .isCurrentAssignment(false)
              .build();
      cropAssignmentTrailRepository.save(exhaustedTrail);

      return new CropReplaceClusterResponse(
          null, null, "No next cluster found with land type: " + landType);
    }

    ClusterMaster nextCluster = nextClusters.get(0);

    // 5. Step 1: Save Rejection Trail for current cluster
    CropAssignmentTrail rejectionTrail =
        CropAssignmentTrail.builder()
            .cropId(request.getCropId())
            .cluster(currentCluster)
            .keyPlot(currentCluster.getKeyPlot())
            .landType(landType)
            .isRejected(true)
            .zoneId(request.getZoneId())
            .isCurrentAssignment(false)
            .rejectionReason("Rejected by user")
            .rejectedBy(request.getUserId())
            .rejectedAt(LocalDateTime.now())
            .build();
    cropAssignmentTrailRepository.save(rejectionTrail);

    // 6. Step 2: Save Assignment Trail for next cluster
    CropAssignmentTrail assignTrail =
        CropAssignmentTrail.builder()
            .cropId(request.getCropId())
            .cluster(nextCluster)
            .keyPlot(nextCluster.getKeyPlot())
            .landType(landType)
            .zoneId(request.getZoneId())
            .isRejected(false)
            .isCurrentAssignment(true)
            .assignedOn(LocalDateTime.now())
            .build();
    cropAssignmentTrailRepository.save(assignTrail);

    // 7. Return new assignment info to Form-Service
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

}
