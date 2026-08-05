package cdti.aidea.earas.service;

// import cdti.aidea.earas.model.*;
import cdti.aidea.earas.contract.Projection.BtrStatsProjection;
import cdti.aidea.earas.contract.RequestsDTOs.TblWorkAllocationDTO;
import cdti.aidea.earas.contract.RequestsDTOs.ZoneAssignedRequset;
import cdti.aidea.earas.contract.Response.*;
import cdti.aidea.earas.contract.ZoneLocationResponse;
import cdti.aidea.earas.model.Btr_models.*;
import cdti.aidea.earas.model.Btr_models.Masters.*;
import cdti.aidea.earas.repository.Btr_repo.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class Zone_Service {

  private final ModelMapper modelMapper;
  private final TblMasterZoneRepository tblMasterZoneRepository;
  private final UserZoneAssignmentRepositoty userZoneAssignmentRepositoty;
  private final TblZoneRevenueVillageMappingRepository tblZoneRevenueVillageMappingRepository;
  private final TblMasterVillageRepository tblMasterVillageRepository;
  private final LocalBodyRepository localBodyRepository;
  private final ClusterMasterRepository clusterMasterRepository;
  private final TblBtrDataRepository tblBtrDataRepository;
  private final LandTypeClassificationService landTypeClassificationService;
  private final DistrictMasterRepository districtMasterRepository;
  private final ZoneRevenueTalukMappingRepository zoneRevenueTalukMappingRepository;
  private final TblBtrDataOldRepository tblBtrDataOldRepository;
  private final TblMasterVillageBlockRepository tblMasterVillageBlockRepository;
  private final DesTalukRepository desTalukRepository;
  private final ZoneLocalbodyBlockMappingRepository zoneLocalbodyBlockMappingRepository;
  private final MasterBlockRepository masterBlockRepository;
  private final LocalBodyTypeRepository localBodyTypeRepository;
  private final TblWorkAllocationRepository tblWorkAllocationRepository;
  private final TblSeasonMasterRepository seasonMasterRepository;
  private final ClusterFormDataRepository clusterFormDataRepository;

  public List<ZoneListResponse> UserZonesByType(String type, Integer idValue) {
    try {
      List<TblMasterZone> zones = null;

      // Decide which ID to use based on the type (Taluk or District)
      if ("Taluk".equalsIgnoreCase(type)) {
        zones = tblMasterZoneRepository.findByDesTalukId(idValue);
      } else if ("District".equalsIgnoreCase(type)) {
        zones = tblMasterZoneRepository.findByDistId(idValue);
      } else if ("Directorate".equalsIgnoreCase(type)) {
        // If type is DIRECTORATE, exclude the District logic and only fetch by Taluk
        zones = tblMasterZoneRepository.findByDistId(idValue);
      } else {
        throw new IllegalArgumentException("Invalid type. Use 'Taluk' or 'District'.");
      }

      if (zones.isEmpty()) {
        throw new IllegalArgumentException("No zones found for the given ID.");
      }

      // Fetch all assigned zone IDs
      List<Long> assignedZoneIds = userZoneAssignmentRepositoty.findActiveAssignedZoneIds();
      // Filter out the zones that are already assigned
      List<TblMasterZone> availableZones =
              zones.stream()
                      .filter(zone -> !assignedZoneIds.contains(zone.getZoneId().longValue()))
                      .collect(Collectors.toList());

      // Map the available zones to the response DTO
      List<ZoneListResponse> zoneList =
              availableZones.stream()
                      .map(
                              zone ->
                                      new ZoneListResponse(
                                              zone.getZoneId(),
                                              zone.getZoneCode(),
                                              zone.getZoneNameEn(),
                                              zone.getZoneNameMal(), zone.getBtrType().getBtrType(),
                                              0, 0, null, null, null))
                      .collect(Collectors.toList());

      return zoneList;
    } catch (Exception e) {
      throw new IllegalArgumentException("Something went wrong while fetching zones", e);
    }
  }

  public List<ZoneIdNameResponse> getAssignedZones(UUID userId) {

    List<UserZoneAssignment> assignments =
            userZoneAssignmentRepositoty.findAllByUserIdAndIsActiveTrue(userId);

    if (assignments.isEmpty()) {
      throw new IllegalArgumentException("User has no assigned zones.");
    }

    // Fetch all default seasons
    List<SeasonResponse> seasons = seasonMasterRepository.findAll().stream()
            .filter(TblSeasonMaster::getIsActive)
            .map(s -> new SeasonResponse(
                    s.getId(),
                    s.getSeasonName(),
                    s.getDefaultStart(),
                    s.getDefaultEnd(),
                    null
            ))
            .collect(Collectors.toList());

    // Map zones + attach seasons
    return assignments.stream()
            .map(a -> ZoneIdNameResponse.builder()
                    .zoneId(a.getTblMasterZone().getZoneId())
                    .dist_id(a.getTblMasterZone().getDistId())
                    .zoneName(a.getTblMasterZone().getZoneNameEn())
                    .zone_type_id(a.getTblMasterZone().getBtrType().getBtrTypeId())
                    .zone_type_name(a.getTblMasterZone().getBtrType().getBtrType())
                    .seasons(seasons) // attach seasons list
                    .build()
            )
            .collect(Collectors.toList());
  }

  public UserZoneAssignment updateZoneAssignmentStatus(ZoneAssignedRequset request) {
    try {
      // 🔁 Convert Integer to Long
      Long zoneId = request.getZoneId().longValue();
      System.out.println("request " + request);
      // 1. Fetch the current assignment to update
      Optional<UserZoneAssignment> existingAssignment =
              userZoneAssignmentRepositoty.findByUserIdAndTblMasterZone_ZoneIdAndIsActiveTrue(request.getUser_id(), zoneId);

      if (existingAssignment.isEmpty()) {
        throw new IllegalArgumentException("Zone assignment not found for this user and zone");
      }

      UserZoneAssignment assignment = existingAssignment.get();

      // 2. If activating, deactivate all other assignments
//            if (Boolean.TRUE.equals(request.getIs_active())) {
//                List<UserZoneAssignment> allAssignments =
//                        userZoneAssignmentRepositoty.findByUserId(request.getUser_id());
//
//                for (UserZoneAssignment a : allAssignments) {
//                    if (!a.getTblMasterZone().getZoneId().equals(zoneId)) {
//                        a.setIsActive(false);
//                    }
//                }
//
//                // Save updated inactive zones
//                userZoneAssignmentRepositoty.saveAll(allAssignments);
//            }

      // 3. Update the selected zone’s status
      assignment.setIsActive(request.getIs_active());
      assignment.setAssignedBy(request.getAssigner_id());
      assignment.setUpdatedAt(LocalDateTime.now());

      // 4. Save and return the updated assignment
      return userZoneAssignmentRepositoty.save(assignment);

    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to update zone status: " + e.getMessage());
    }
  }

  public UserZoneAssignment ZoneAssignedService(ZoneAssignedRequset request) {
    System.out.println(
            "ZoneAssignedService "
                    + request.getAssigner_id()
                    + " "
                    + request.getZoneId()
                    + " "
                    + request.getUser_id());

    try {
      // 1. Validate zone exists
      TblMasterZone tblMasterZone =
              tblMasterZoneRepository
                      .findById(request.getZoneId())
                      .orElseThrow(() -> new IllegalArgumentException("Zone does not exist"));

      // 2. Check if zone is already assigned to another user
      Optional<UserZoneAssignment> existingZoneAssignment =
              userZoneAssignmentRepositoty.findByTblMasterZone_ZoneIdAndIsActiveTrue(request.getZoneId());

      if (existingZoneAssignment.isPresent()) {
        throw new IllegalArgumentException("Zone is already assigned to another user");
      }

      // 3. Check if user already has a zone assignment
//      Optional<UserZoneAssignment> existingUserAssignment =
//              userZoneAssignmentRepositoty.findByUserId(request.getUser_id());
//
//      if (existingUserAssignment.isPresent()) {
//        UserZoneAssignment currentAssignment = existingUserAssignment.get();
//
//        // If zone is the same, no action needed (optional)
//        if (currentAssignment.getTblMasterZone().getZoneId().equals(request.getZoneId())) {
//          throw new IllegalArgumentException("User is already assigned to this zone");
//        }
//
//        // Otherwise, allow multiple assignments → create a new record
//        // Optional: you could deactivate the old assignment if you only want one active
//        //                currentAssignment.setIsActive(false);
//        //                userZoneAssignmentRepositoty.save(currentAssignment);
//      }

      // 4. Create new assignment
      UserZoneAssignment newAssignment = new UserZoneAssignment();
      newAssignment.setUserId(request.getUser_id());
      newAssignment.setAssignedBy(request.getAssigner_id());
      newAssignment.setTblMasterZone(tblMasterZone);
      newAssignment.setIsActive(true);

      return userZoneAssignmentRepositoty.save(newAssignment);

    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
      throw e;
    } catch (Exception e) {
      System.out.println(e.getMessage());
      throw new IllegalArgumentException("Something went wrong during zone assignment");
    }
  }

  public BtrMainResponse<List<BtrDataListResponse>> UserAssignedLand(
          Integer zone_id, int page, int size, String filter) {

    // ✅ Zone validation
    var zoneOpt = tblMasterZoneRepository.findById(zone_id);
    if (zoneOpt.isEmpty()) {
      throw new RuntimeException("Zone not found");
    }

    Long zoneValue = Long.valueOf(zone_id);

    // ✅ Pagination + Sorting
    Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by("lbcode")
                    .and(Sort.by("lsgcode"))
                    .and(Sort.by("bcode"))
                    .and(Sort.by("resvno"))
                    .and(Sort.by("resbdno"))
                    .and(Sort.by("ltype"))
    );

    // ✅ Fetch paginated data only
    Page<TblBtrData> pageResult;
    if (filter == null || filter.isEmpty()) {
      pageResult = tblBtrDataRepository.findByZoneWithOrder(zoneValue, pageable);
    } else {
      pageResult = tblBtrDataRepository.findByZoneWithNamesFilter(zoneValue, filter, pageable);
    }

    // ✅ 🔥 FAST TOTAL CALCULATION (DB SIDE)
    Object result = tblBtrDataRepository.getZoneTotals(zoneValue);

    Object[] totals = (Object[]) result;

    double totalWetArea = totals[0] != null ? ((Number) totals[0]).doubleValue() : 0;
    double totalDryArea = totals[1] != null ? ((Number) totals[1]).doubleValue() : 0;
    double totalConvertedArea = totalWetArea + totalDryArea;

    // ✅ Fetch village mapping (light data)
    var zoneRevenueList = tblZoneRevenueVillageMappingRepository.findByZone(zone_id);

    List<Integer> villageIds = zoneRevenueList.stream()
            .map(TblZoneRevenueVillageMapping::getRevenueVillage)
            .toList();

    List<TblMasterVillage> villageList = tblMasterVillageRepository.findAllById(villageIds);

    Map<Integer, String> villageNameMap = villageList.stream()
            .collect(Collectors.toMap(
                    TblMasterVillage::getLsgCode,
                    TblMasterVillage::getVillageNameEn
            ));

    // ✅ Fetch localbody names only for current page
    List<String> lbCodes = pageResult.getContent().stream()
            .map(TblBtrData::getLbcode)
            .distinct()
            .toList();

    List<TblLocalBody> localBodies = localBodyRepository.findAllByCodeApiIn(lbCodes);

    Map<String, String> localBodyNameMap = new HashMap<>();
    localBodies.forEach(lb ->
            localBodyNameMap.put(lb.getCodeApi(), lb.getLocalbodyNameEn())
    );

    // ✅ DTO mapping (ONLY PAGE DATA)
    List<BtrDataListResponse> responseDtos =
            pageResult.getContent().stream()
                    .map(data -> {

                      BigDecimal bd = new BigDecimal(
                              data.getTotCent() != null ? data.getTotCent() : 0
                      ).setScale(2, RoundingMode.HALF_UP);

                      return new BtrDataListResponse(
                              data.getId(),
                              villageNameMap.get(data.getLsgcode()),
                              data.getBcode(),
                              data.getResvno(),
                              String.valueOf(data.getResbdno()),
                              data.getLtype(),
                              localBodyNameMap.get(data.getLbcode()),
                              data.getLtype(),
                              data.getOwnername(),
                              data.getAddress(),
                              data.getTpno(),
                              data.getTbsubdivisionno(),
                              data.getHouseno(),
                              data.getOldsvno(),
                              data.getOldsubno(),
                              bd.toPlainString()
                      );
                    })
                    .toList();

    // ✅ Final response
    return new BtrMainResponse<>(
            "success",
            "Data fetched successfully",
            responseDtos,
            pageResult.getTotalElements(),
            round(totalConvertedArea),
            round(totalWetArea),
            round(totalDryArea)
    );
  }

  private double round(double value) {
    return new BigDecimal(value)
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();
  }
  //    public BtrMainResponse<List<BtrDataListResponse>> UserAssignedLand(UUID userId, int page,
  // int size, String filter) {
  //        // Fetch user and zone data
  //        var user = userZoneAssignmentRepositoty.findByUserId(userId);
  //        System.out.println("zone id " + user.get().getTblMasterZone().getZoneId());
  //        var zoneRevenueList =
  // tblZoneRevenueVillageMappingRepository.findByZone(user.get().getTblMasterZone().getZoneId());
  //
  //        // Extract village IDs and fetch village data
  //        List<Integer> villageIds = zoneRevenueList.stream()
  //                .map(TblZoneRevenueVillageMapping::getRevenueVillage)
  //                .toList();
  //        List<TblMasterVillage> villageList = tblMasterVillageRepository.findAllById(villageIds);
  ////        System.out.println("village List" + villageList);
  //        List<Integer> lsgcodes =
  // villageList.stream().map(TblMasterVillage::getLsgCode).toList();
  //        System.out.println(lsgcodes);
  //
  //        List<TblBtrData> allData = tblBtrRepository.findAllByLsgcodeIn(lsgcodes);
  //
  //
  //
  //        List<String> landType = allData.stream()
  //                .map(tblBtrData -> tblBtrData.getLtype())
  //                .distinct()
  //                .collect(Collectors.toList());
  //        System.out.println("land Type  " + landType);
  //
  //        List<String> LbcodeList =
  // allData.stream().map(TblBtrData::getLbcode).distinct().collect(Collectors.toList());
  //        System.out.println("Lbcode " + LbcodeList);
  //        List<TblLocalBody> localBodies_full =
  // localBodyRepository.findAllByCodeApiIn(LbcodeList);
  //
  //
  // localBodies_full.stream().map(TblLocalBody::getLocalbodyNameMal).forEach(System.out::println);
  //        localBodies_full.stream().map(TblLocalBody::getCodeApi).forEach(System.out::println);
  //
  //
  //        // Create Pageable object for pagination
  //        Pageable pageable = PageRequest.of(page, size);
  //
  //        Page<TblBtrData> pageResult;
  //
  //        if (filter == null || filter.isEmpty()) {
  //            // If no filter, just query by lsgcodes with pagination
  //            pageResult = tblBtrRepository.findByLsgcodeIn(lsgcodes, pageable);
  //        } else {
  //            pageResult = tblBtrRepository.findByLsgcodeInAndFilter(lsgcodes, filter, pageable);
  //        }
  //
  //
  //        double totalArea = pageResult.getContent().stream()
  //                .mapToDouble(TblBtrData::getNsqm) // Assuming nsqm is the field you want to sum
  // up
  //                .sum();
  //
  //        // Prepare a map for village codes and names
  //        Map<String, String> villageNameMap = villageList.stream()
  //                .collect(Collectors.toMap(TblMasterVillage::getVillageCodeApi,
  // TblMasterVillage::getVillageNameMal));
  //
  //        // Create a map to fetch LocalBody details based on lbcode
  //        Map<String, String> localBodyNameMap = new HashMap<>();
  //        List<String> lbCodes = pageResult.getContent().stream()
  //                .map(TblBtrData::getLbcode)
  //                .distinct()
  //                .collect(Collectors.toList());
  //
  //
  //        List<TblLocalBody> localBodies = localBodyRepository.findAllByCodeApiIn(lbCodes);
  //        localBodies.forEach(localBody ->
  //                localBodyNameMap.put(localBody.getCodeApi(), localBody.getLocalbodyNameMal())
  //        );
  //
  //// Get dynamic land type classification map
  //        Map<String, String> landTypeClassificationMap =
  // landTypeClassificationService.getLandTypeClassificationMap();
  //

  /// / Total area components
  //        double totalWetArea = 0;
  //        double totalDryArea = 0;
  //
  //        for (TblBtrData data : allData) {
  //            String ltype = data.getLtype();
  //            double nsqm = data.getNsqm() != null ? data.getNsqm() : 0;
  //            double nare = data.getNare() != null ? data.getNare() : 0;
  //            double nhect = data.getNhect() != null ? data.getNhect() : 0;
  //
  //            if (landTypeClassificationMap.containsKey(ltype)) {
  //                String classification = landTypeClassificationMap.get(ltype);
  //                double area = nhect * 0.01 + nare * 0.0001 + nsqm * 0.000001;
  //
  //                switch (classification) {
  //                    case "wet":
  //                        totalWetArea += area;
  //                        break;
  //                    case "dry":
  //                    case "others": // Treat others as dry
  //                        totalDryArea += area;
  //                        break;
  //                }
  //            }
  //        }
  //        double totalConvertedArea = totalWetArea + totalDryArea;
  //
  //        double totalWetAreas = new BigDecimal(totalWetArea).setScale(2,
  // RoundingMode.HALF_UP).doubleValue();
  //        double totalDryAreas = new BigDecimal(totalDryArea).setScale(2,
  // RoundingMode.HALF_UP).doubleValue();
  //        double totalConvertedAreas = new BigDecimal(totalConvertedArea).setScale(2,
  // RoundingMode.HALF_UP).doubleValue();
  //
  //
  //        List<BtrDataListResponse> responseDtos = pageResult.getContent().stream()
  //                .map(myTable -> {
  //                    // Apply the formula: totalCent = nhect*247.13 + nare*2.47 + nsqm*0.02471
  //                    double totalCent = (myTable.getNhect() != null ? myTable.getNhect() : 0) *
  // 247.13
  //                            + (myTable.getNare() != null ? myTable.getNare() : 0) * 2.47
  //                            + (myTable.getNsqm() != null ? myTable.getNsqm() : 0) * 0.02471;
  //
  //                    double roundedCent = new BigDecimal(totalCent)
  //                            .setScale(2, RoundingMode.HALF_UP)
  //                            .doubleValue();
  //                    return new BtrDataListResponse(
  //                            myTable.getId(),
  //                            villageNameMap.get(String.valueOf(myTable.getVcode())),
  //                            myTable.getBcode(),
  //                            myTable.getResvno(),
  //                            myTable.getResbdno(),
  //                            myTable.getLbtype(),
  //                            localBodyNameMap.get(myTable.getLbcode()),
  //                            myTable.getLbcode(),
  //                            myTable.getLtype(),
  //                            roundedCent
  //                    );
  //                })
  //                .collect(Collectors.toList());
  //
  //
  //        // Return paginated data along with total count and total area
  //        System.out.println(responseDtos);
  //        return new BtrMainResponse<>(
  //                "success",
  //                "Data fetched successfully",
  //                responseDtos,
  //                pageResult.getTotalElements(),  // Total count of records
  //                totalConvertedAreas,  // This is the converted total area in hectares
  //                totalWetAreas,
  //                totalDryAreas
  //        );
  //    }
  public Object ZoneDetails(Integer zone_id) {

    // ✅ Validate zone
    var zoneOpt = tblMasterZoneRepository.findById(zone_id);
    if (zoneOpt.isEmpty()) {
      throw new RuntimeException("Zone not available");
    }
    var zone = zoneOpt.get();

    // ✅ 🔥 FAST QUERY (optimized)
    List<BtrStatsProjection> stats =
            tblBtrDataRepository.getZoneStats(Long.valueOf(zone_id));

    // ✅ Get all lbcodes from stats (NOT from allData)
    List<String> lbcodeList = stats.stream()
            .map(BtrStatsProjection::getLbcode)
            .toList();

    // ✅ Fetch local bodies
    List<TblLocalBody> localBodies = localBodyRepository.findAllByCodeApiIn(lbcodeList);

    Map<String, String> localBodyNameMap = new HashMap<>();
    Map<String, Short> localBodyTypeIdMap = new HashMap<>();

    for (TblLocalBody lb : localBodies) {
      localBodyNameMap.put(lb.getCodeApi(), lb.getLocalbodyNameEn());
      localBodyTypeIdMap.put(lb.getCodeApi(), lb.getLocalbodyType());
    }

    // ✅ Fetch local body types
    List<Long> typeIds = localBodyTypeIdMap.values().stream()
            .filter(Objects::nonNull)
            .map(Short::longValue)
            .distinct()
            .toList();

    List<LocalBodyType> types = localBodyTypeRepository.findByIdIn(typeIds);

    Map<Integer, String> typeMap = new HashMap<>();
    for (LocalBodyType t : types) {
      typeMap.put(t.getId().intValue(), t.getName());
    }

    // ✅ Build response (NO heavy loop now)
    List<Map<String, Object>> panchayathResponses = new ArrayList<>();

    double totalWetAreaZone = 0;
    double totalDryAreaZone = 0;
    double totalPlotCount = 0;
    double overallTotalArea = 0;
// ✅ Fetch villages + blocks in ONE query
    List<Object[]> vbData = tblBtrDataRepository.getVillageBlockData(Long.valueOf(zone_id));

    Map<String, Map<String, String>> villageBlockMap = new HashMap<>();

    for (Object[] row : vbData) {
      String lbcode = (String) row[0];
      String villages = (String) row[1];
      String blocks = (String) row[2];

      Map<String, String> map = new HashMap<>();
      map.put("villages", villages);
      map.put("blocks", blocks);

      villageBlockMap.put(lbcode, map);
    }
    for (BtrStatsProjection s : stats) {

      Map<String, Object> data = new HashMap<>();

      data.put("p_name", localBodyNameMap.get(s.getLbcode()));
      data.put("lbcode", s.getLbcode());
      data.put("Wet_area", s.getWet_area());
      data.put("Dry_area", s.getDry_area());
      data.put("Total_area", s.getTotal_area());

      data.put("Wet_plot", s.getWet_plot());
      data.put("dry_plot", s.getDry_plot());
      data.put("t_plot", s.getTotal_plot());

      // totals
      totalWetAreaZone += s.getWet_area();
      totalDryAreaZone += s.getDry_area();
      totalPlotCount += s.getTotal_plot();
      overallTotalArea += s.getTotal_area();

      // localbody type
      Short typeId = localBodyTypeIdMap.get(s.getLbcode());
      String typeName = (typeId != null) ? typeMap.get(typeId.intValue()) : "";
      data.put("localbodytype", typeName);

      // ⚠️ TEMP (optional: keep empty or static)
      Map<String, String> vb = villageBlockMap.get(s.getLbcode());

      String villagesStr = vb != null ? vb.get("villages") : "";
      String blocksStr = vb != null ? vb.get("blocks") : "";

      List<String> villagesList = villagesStr.isEmpty()
              ? new ArrayList<>()
              : Arrays.stream(villagesStr.split(","))
              .map(String::trim)
              .toList();

      List<String> blocksList = blocksStr.isEmpty()
              ? new ArrayList<>()
              : Arrays.stream(blocksStr.split(","))
              .map(String::trim)
              .toList();

      data.put("villages", villagesList);
      data.put("blocks", blocksList);
      panchayathResponses.add(data);
    }

    // ✅ District + Taluk
    Optional<DistrictMaster> district =
            districtMasterRepository.findById(Long.valueOf(zone.getDistId()));

    Optional<DesTaluk> taluk =
            desTalukRepository.findById(zone.getDesTalukId());

    String districtName = district.map(DistrictMaster::getDist_name_en).orElse("");
    String talukName = taluk.map(DesTaluk::getDesTalukNameEn).orElse("");

    // ✅ Localbody label
    String localBodyLabel = "";
    String localbodyType = "";

    Optional<ZoneLocalbodyBlockMapping> mapping =
            zoneLocalbodyBlockMappingRepository.findByZoneAndIsValid(zone.getZoneId(), true);

    if (mapping.isPresent()) {
      if (mapping.get().getBlockPanchayatMunicipalArea() == 1) {
        Optional<MasterBlock> block =
                masterBlockRepository.findById(mapping.get().getBlockDetails());
        localBodyLabel = block.map(MasterBlock::getBlockName).orElse("");
        localbodyType = "Block Panchayath";
      } else {
        Optional<TblLocalBody> lb =
                localBodyRepository.findById(mapping.get().getBlockDetails());
        localBodyLabel = lb.map(TblLocalBody::getLocalbodyNameEn).orElse("");

        if (lb.isPresent()) {
          Optional<LocalBodyType> type =
                  localBodyTypeRepository.findById((long) lb.get().getLocalbodyType());
          localbodyType = type.map(LocalBodyType::getName).orElse("");
        }
      }
    }

    return new KeyPlotResponse<>(
            "success",
            "Data fetched successfully",
            districtName,
            talukName,
            localbodyType,
            localBodyLabel,
            zone.getZoneNameEn(),
            panchayathResponses,
            lbcodeList,
            totalPlotCount,
            overallTotalArea,
            totalWetAreaZone,
            totalDryAreaZone,
            new ArrayList<>()
    );
  }

  public List<LbCodeResponse> getLocalBodiesByZone(Integer zoneId) {

    // 1️⃣ Get Zone
    TblMasterZone zone = tblMasterZoneRepository.findById(zoneId)
            .orElseThrow(() -> new RuntimeException("Zone not found"));

    // 2️⃣ Get revenue village mappings for the zone
    List<TblZoneRevenueVillageMapping> zoneRevenueList =
            tblZoneRevenueVillageMappingRepository.findByZone(zone.getZoneId());

    // 3️⃣ Extract village IDs
    List<Integer> villageIds = zoneRevenueList.stream()
            .map(TblZoneRevenueVillageMapping::getRevenueVillage)
            .toList();

    // 4️⃣ Get all villages for those IDs
    List<TblMasterVillage> villageList = tblMasterVillageRepository.findAllById(villageIds);

    // 5️⃣ Extract LSG codes from those villages
    List<Integer> lsgCodes = villageList.stream()
            .map(TblMasterVillage::getLsgCode)
            .toList();

    // 6️⃣ Fetch BTR data (repo returns TblBtrData)
    List<TblBtrData> btrDataNewList = tblBtrDataRepository.findAllByLsgcodeIn(lsgCodes);

    // 7️⃣ Convert TblBtrData → TblBtrDataOld manually
    List<TblBtrData> btrDataList = btrDataNewList.stream()
            .map(btr -> {
              TblBtrData oldData = new TblBtrData();
              oldData.setLbcode(btr.getLbcode());
              oldData.setLsgcode(btr.getLsgcode());
              oldData.setResvno(btr.getResvno());
              oldData.setResbdno(btr.getResbdno());

              return oldData;
            })
            .toList();

    // 8️⃣ Extract unique LB codes from BTR data
    List<String> lbCodes = btrDataList.stream()
            .map(TblBtrData::getLbcode)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

    // 9️⃣ Fetch Local Bodies based on LB codes
    List<TblLocalBody> localBodies = localBodyRepository.findAllByCodeApiIn(lbCodes);

    // 🔟 Map to response DTO
    return localBodies.stream()
            .map(lb -> new LbCodeResponse(lb.getCodeApi(), lb.getLocalbodyNameEn()))
            .toList();
  }


  public ZoneLocationResponse getZoneLocationDetails(Integer zoneId, Long clusterId) {

    List<Object[]> results = tblMasterZoneRepository.getZoneLocationDetails(zoneId);

    if (results.isEmpty()) {
      throw new RuntimeException("Zone not found");
    }

    Object[] result = results.get(0);
    ZoneLocationResponse.ZoneLocationResponseBuilder builder =
            ZoneLocationResponse.builder()
                    .districtId(result[0] == null ? null : ((Number) result[0]).intValue())
                    .districtName((String) result[1])
                    .talukId(result[2] == null ? null : ((Number) result[2]).intValue())
                    .talukName((String) result[3])
                    .blockId(result[4] == null ? null : ((Number) result[4]).intValue())
                    .blockName((String) result[5])
                    .zoneName((String) result[6])
                    .localbodyId(null)
                    .localbodyName(null)
                    .lbCode(null);

    if (clusterId != null) {
      builder.totalClusterEnumArea(
              clusterFormDataRepository.getTotalClusterArea(clusterId));

    List<Object[]> lbResults =
            clusterMasterRepository.getClusterLocalBodyDetails(clusterId);

    if (!lbResults.isEmpty()) {
      Object[] lbResult = lbResults.get(0);

      builder.localbodyId(
              lbResult[0] == null ? null : ((Number) lbResult[0]).intValue());

      builder.localbodyName((String) lbResult[1]);

      builder.lbCode((String) lbResult[2]);

      builder.landType((String) lbResult[3]);
    }
  }
  return builder.build();
 }
}