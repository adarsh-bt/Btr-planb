package cdti.aidea.earas.service;

// import cdti.aidea.earas.model.*;
import cdti.aidea.earas.contract.RequestsDTOs.ZoneAssignedRequset;
import cdti.aidea.earas.contract.Response.*;
import cdti.aidea.earas.model.Btr_models.*;
import cdti.aidea.earas.model.Btr_models.Masters.*;
import cdti.aidea.earas.repository.Btr_repo.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
  private final TblBtrRepository tblBtrRepository;
  private final LandTypeClassificationService landTypeClassificationService;
  private final DistrictMasterRepository districtMasterRepository;
  private final ZoneRevenueTalukMappingRepository zoneRevenueTalukMappingRepository;
  private final TblBtrDataOldRepository tblBtrDataOldRepository;
  private final TblMasterVillageBlockRepository tblMasterVillageBlockRepository;
  private final DesTalukRepository desTalukRepository;
  private final ZoneLocalbodyBlockMappingRepository zoneLocalbodyBlockMappingRepository;
  private final MasterBlockRepository masterBlockRepository;
  private final LocalBodyTypeRepository localBodyTypeRepository;

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
      List<Long> assignedZoneIds = userZoneAssignmentRepositoty.findAssignedZoneIds();
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
                          zone.getZoneNameMal()))
              .collect(Collectors.toList());

      return zoneList;
    } catch (Exception e) {
      throw new IllegalArgumentException("Something went wrong while fetching zones", e);
    }
  }

  public List<ZoneIdNameResponse> getAssignedZones(UUID userId) {
    List<UserZoneAssignment> assignments = userZoneAssignmentRepositoty.findAllByUserId(userId);

    if (assignments.isEmpty()) {
      throw new IllegalArgumentException("User has no assigned zones.");
    }

    return assignments.stream()
        .map(
            a ->
                new ZoneIdNameResponse(
                    a.getTblMasterZone().getZoneId(),
                    a.getTblMasterZone().getZoneNameEn() // Use .getZoneNameMal() if needed
                    ))
        .collect(Collectors.toList());
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
          userZoneAssignmentRepositoty.findByTblMasterZone_ZoneId(request.getZoneId());

      if (existingZoneAssignment.isPresent()) {
        throw new IllegalArgumentException("Zone is already assigned to another user");
      }

      // 3. Check if user already has a zone assignment
      Optional<UserZoneAssignment> existingUserAssignment =
          userZoneAssignmentRepositoty.findByUserId(request.getUser_id());

      if (existingUserAssignment.isPresent()) {
        UserZoneAssignment currentAssignment = existingUserAssignment.get();

        // If zone is the same, no action needed (optional)
        if (currentAssignment.getTblMasterZone().getZoneId().equals(request.getZoneId())) {
          throw new IllegalArgumentException("User is already assigned to this zone");
        }

        // Otherwise, allow multiple assignments → create a new record
        // Optional: you could deactivate the old assignment if you only want one active
        //                currentAssignment.setIsActive(false);
        //                userZoneAssignmentRepositoty.save(currentAssignment);
      }

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
    // Fetch user and zone data

    Optional<TblMasterZone> zone = tblMasterZoneRepository.findById(zone_id);

    //        var user = userZoneAssignmentRepositoty.findByUserId(userId);
    //        System.out.println("zone id " + user.get().getTblMasterZone().getZoneId());
    var zoneRevenueList = tblZoneRevenueVillageMappingRepository.findByZone(zone.get().getZoneId());

    // Extract village IDs and fetch village data
    List<Integer> villageIds =
        zoneRevenueList.stream().map(TblZoneRevenueVillageMapping::getRevenueVillage).toList();
    List<TblMasterVillage> villageList = tblMasterVillageRepository.findAllById(villageIds);
    //        System.out.println("village List" + villageList);
    List<Integer> lsgcodes = villageList.stream().map(TblMasterVillage::getLsgCode).toList();
    System.out.println(lsgcodes);

    //        List<TblBtrData> allData = tblBtrRepository.findAllByLsgcodeIn(lsgcodes);
    List<TblBtrDataOld> allData = tblBtrDataOldRepository.findAllByLsgcodeIn(lsgcodes);

    List<String> landType =
        allData.stream()
            .map(tblBtrData -> tblBtrData.getLtype())
            .distinct()
            .collect(Collectors.toList());
    System.out.println("land Type  " + landType);

    List<String> LbcodeList =
        allData.stream().map(TblBtrDataOld::getLbcode).distinct().collect(Collectors.toList());
    System.out.println("Lbcode " + LbcodeList);
    List<TblLocalBody> localBodies_full = localBodyRepository.findAllByCodeApiIn(LbcodeList);

    localBodies_full.stream().map(TblLocalBody::getLocalbodyNameEn).forEach(System.out::println);
    localBodies_full.stream().map(TblLocalBody::getCodeApi).forEach(System.out::println);

    // Create Pageable object for pagination
    //        Pageable pageable = PageRequest.of(page, size);
    // Create Pageable object with full sorting criteria
    Pageable pageable =
        PageRequest.of(
            page,
            size,
            Sort.by("lbcode")
                .and( // 1. Localbody code
                    Sort.by("lsgcode")
                        .and( // 2. Village
                            Sort.by("bcode")
                                .and( // 3. Block
                                    Sort.by("resvno")
                                        .and( // 4. Survey No part 1
                                            Sort.by("resbdno")
                                                .and( // 4. Survey No part 2
                                                    Sort.by("ltype") // 5. Land Type
                                                    ))))));

    Page<TblBtrDataOld> pageResult;

    if (filter == null || filter.isEmpty()) {
      pageResult = tblBtrDataOldRepository.findByLsgcodeInWithOrder(lsgcodes, pageable);
    } else {
      // For filtered queries, you might need a similar ORDER BY clause
      pageResult =
          tblBtrDataOldRepository.findByLsgcodeInWithNamesFilter(lsgcodes, filter, pageable);
    }

    double totalArea =
        pageResult.getContent().stream()
            .mapToDouble(TblBtrDataOld::getArea) // Assuming nsqm is the field you want to sum up
            .sum();

    // Prepare a map for village codes and names
    Map<Integer, String> villageNameMap =
        villageList.stream()
            .collect(
                Collectors.toMap(TblMasterVillage::getLsgCode, TblMasterVillage::getVillageNameEn));

    // Create a map to fetch LocalBody details based on lbcode
    Map<String, String> localBodyNameMap = new HashMap<>();
    List<String> lbCodes =
        pageResult.getContent().stream()
            .map(TblBtrDataOld::getLbcode)
            .distinct()
            .collect(Collectors.toList());

    List<TblLocalBody> localBodies = localBodyRepository.findAllByCodeApiIn(lbCodes);
    localBodies.forEach(
        localBody -> localBodyNameMap.put(localBody.getCodeApi(), localBody.getLocalbodyNameEn()));

    // Get dynamic land type classification map
    Map<String, String> landTypeClassificationMap =
        landTypeClassificationService.getLandTypeClassificationMap();

    // Total area components
    double totalWetArea = 0;
    double totalDryArea = 0;

    for (TblBtrDataOld data : allData) {
      String ltype = data.getLtype();
      if (ltype != null) {
        ltype = ltype.trim(); // <-- Trim whitespace here
      }
      //            double nsqm = data.getNsqm() != null ? data.getNsqm() : 0;
      //            double nare = data.getNare() != null ? data.getNare() : 0;
      //            double nhect = data.getNhect() != null ? data.getNhect() : 0;
      double areas = data.getArea() != null ? data.getArea() : 0;

      if (landTypeClassificationMap.containsKey(ltype)) {
        String classification = landTypeClassificationMap.get(ltype);
        double area = areas;

        switch (classification) {
          case "wet":
            totalWetArea += area;
            break;
          case "dry":
          case "others": // Treat others as dry
            totalDryArea += area;
            break;
        }
      }
    }
    double totalConvertedArea = totalWetArea + totalDryArea;

    double totalWetAreas =
        new BigDecimal(totalWetArea).setScale(2, RoundingMode.HALF_UP).doubleValue();
    double totalDryAreas =
        new BigDecimal(totalDryArea).setScale(2, RoundingMode.HALF_UP).doubleValue();
    double totalConvertedAreas =
        new BigDecimal(totalConvertedArea).setScale(2, RoundingMode.HALF_UP).doubleValue();

    List<BtrDataListResponse> responseDtos =
        pageResult.getContent().stream()
            .map(
                myTable -> {
                  //                     Apply the formula: totalCent = nhect*247.13 + nare*2.47 +
                  // nsqm*0.02471
                  //                    double totalCent = (myTable.getNhect() != null ?
                  // myTable.getNhect() : 0) * 247.13
                  //                            + (myTable.getNare() != null ? myTable.getNare() :
                  // 0) * 2.47
                  //                            + (myTable.getNsqm() != null ? myTable.getNsqm() :
                  // 0) * 0.02471;
                  //                    double totalCent = myTable.getArea()

                  BigDecimal bd =
                      new BigDecimal(myTable.getArea()).setScale(2, RoundingMode.HALF_UP);

                  String formatted = bd.toPlainString(); // "10.00"

                  return new BtrDataListResponse(
                      myTable.getId(),
                      villageNameMap.get(myTable.getLsgcode()),
                      myTable.getBcode(),
                      myTable.getResvno(),
                      myTable.getResbdno(),
                      myTable.getLtype(),
                      localBodyNameMap.get(myTable.getLbcode()),
                      myTable.getLtype(),
                      formatted);
                })
            .collect(Collectors.toList());

    // Return paginated data along with total count and total area
    System.out.println(responseDtos);
    return new BtrMainResponse<>(
        "success",
        "Data fetched successfully",
        responseDtos,
        pageResult.getTotalElements(), // Total count of records
        totalConvertedAreas, // This is the converted total area in hectares
        totalWetAreas,
        totalDryAreas);
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
  //// Total area components
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

    //    var user = userZoneAssignmentRepositoty.findByTblMasterZone_ZoneId(zone_id);
    if (tblMasterZoneRepository.findById(zone_id).isEmpty()) {
      throw new RuntimeException("Zone are not avialble");
    }
    var zone = tblMasterZoneRepository.findById(zone_id);
    var zoneRevenueList = tblZoneRevenueVillageMappingRepository.findByZone(zone.get().getZoneId());

    List<Integer> villageIds =
        zoneRevenueList.stream().map(TblZoneRevenueVillageMapping::getRevenueVillage).toList();

    List<TblMasterVillage> villageList = tblMasterVillageRepository.findAllById(villageIds);
    List<Integer> lsgcodes = villageList.stream().map(TblMasterVillage::getLsgCode).toList();
    List<String> villages_names =
        villageList.stream().map(TblMasterVillage::getVillageNameEn).toList();
    System.out.println("villages " + villages_names);

    List<TblBtrDataOld> allData = tblBtrDataOldRepository.findAllByLsgcodeIn(lsgcodes);
    Map<String, String> landTypeClassificationMap =
        landTypeClassificationService.getLandTypeClassificationMap();

    List<String> LbcodeList =
        allData.stream().map(TblBtrDataOld::getLbcode).distinct().collect(Collectors.toList());

    List<TblLocalBody> localBodies_full = localBodyRepository.findAllByCodeApiIn(LbcodeList);

    // Map lbcode -> local body name
    Map<String, String> localBodyNameMap = new HashMap<>();
    localBodies_full.forEach(
        localBody -> localBodyNameMap.put(localBody.getCodeApi(), localBody.getLocalbodyNameEn()));

    // Fetch unique localbody type IDs and load LocalBodyType entities
    List<Short> localbodyTypeIds =
        localBodies_full.stream()
            .map(TblLocalBody::getLocalbodyType)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

    List<Long> ids = localbodyTypeIds.stream().map(Short::longValue).collect(Collectors.toList());

    List<LocalBodyType> localBodyType_full = localBodyTypeRepository.findByIdIn(ids);

    // Build Map<typeId, typeName>
    Map<Integer, String> localBodyTypeMap = new HashMap<>();
    localBodyType_full.forEach(
        localBodyType ->
            localBodyTypeMap.put(localBodyType.getId().intValue(), localBodyType.getName()));

    Map<String, List<TblBtrDataOld>> panchayathDataMap =
        allData.stream().collect(Collectors.groupingBy(TblBtrDataOld::getLbcode));

    List<Map<String, Object>> panchayathResponses = new ArrayList<>();
    List<String> unclassifiedPanchayaths = new ArrayList<>();

    double totalWetAreaZone = 0;
    double totalDryAreaZone = 0;

    Map<String, List<String>> lbcodeToVillageNamesMap = new HashMap<>();
    lbcodeToVillageNamesMap.put("01108", Arrays.asList("KILIMANOOR"));
    lbcodeToVillageNamesMap.put("01113", Arrays.asList("NAGAROOR", "VELLALLOOR"));

    Map<String, List<String>> lbcodeToBlockCodesMap = new HashMap<>();
    lbcodeToBlockCodesMap.put("01108", Arrays.asList("029", "030"));
    lbcodeToBlockCodesMap.put("01113", Arrays.asList("037", "038"));

    // Loop through each panchayath data and calculate values
    for (Map.Entry<String, List<TblBtrDataOld>> entry : panchayathDataMap.entrySet()) {
      String lbcode = entry.getKey();
      List<TblBtrDataOld> panchayathData = entry.getValue();

      double wetArea = 0;
      double dryArea = 0;
      int wetCount = 0;
      int dryCount = 0;

      for (TblBtrDataOld dataItem : panchayathData) {
        String landTypeValue = dataItem.getLtype().trim();
        if (landTypeClassificationMap.containsKey(landTypeValue)) {
          String classification = landTypeClassificationMap.get(landTypeValue);
          double area = dataItem.getArea();
          switch (classification) {
            case "wet" -> {
              wetArea += area;
              wetCount++;
            }
            case "dry", "others" -> {
              dryArea += area;
              dryCount++;
            }
          }
        }
      }

      int total_keyplots = wetCount + dryCount;
      double total_area = Math.round(dryArea * 100.0) / 100.0 + Math.round(wetArea * 100.0) / 100.0;

      if (total_keyplots == 0) {
        unclassifiedPanchayaths.add(localBodyNameMap.get(lbcode));
      }

      totalWetAreaZone += wetArea;
      totalDryAreaZone += dryArea;

      Map<String, Object> data = new HashMap<>();
      data.put("p_name", localBodyNameMap.get(lbcode));
      data.put("Total_area", total_area);
      data.put("Dry_area", Math.round(dryArea * 100.0) / 100.0);
      data.put("Wet_area", Math.round(wetArea * 100.0) / 100.0);

      data.put("Wet_plot", wetCount);
      data.put("dry_plot", dryCount);
      data.put("t_plot", total_keyplots);
      data.put("villages", lbcodeToVillageNamesMap.getOrDefault(lbcode, new ArrayList<>()));
      data.put("blocks", lbcodeToBlockCodesMap.getOrDefault(lbcode, new ArrayList<>()));

      // 🔍 Add localbody type name
      TblLocalBody matchingLocalBody =
          localBodies_full.stream()
              .filter(lb -> lb.getCodeApi().equals(lbcode))
              .findFirst()
              .orElse(null);

      String localbodyTypeName = "";
      if (matchingLocalBody != null) {
        Short typeId = matchingLocalBody.getLocalbodyType();
        if (typeId != null && localBodyTypeMap.containsKey(typeId.intValue())) {
          localbodyTypeName = localBodyTypeMap.get(typeId.intValue());
        }
      }
      data.put("localbodytype", localbodyTypeName);

      panchayathResponses.add(data);
    }

    double overallTotalArea =
        panchayathResponses.stream()
            .mapToDouble(response -> (double) response.get("Total_area"))
            .sum();

    //    var zone = zone_id;
    Optional<DistrictMaster> district_name = districtMasterRepository.findById(1L);
    Optional<DesTaluk> taluk = desTalukRepository.findById(zone.get().getDesTalukId());

    String districtName = district_name.map(DistrictMaster::getDist_name_en).orElse("");
    String talukName = taluk.map(DesTaluk::getDesTalukNameEn).orElse("");
    String zoneName = zone.get().getZoneNameEn();

    String localBodyLabel = "";
    String localbodyType = "";

    Optional<ZoneLocalbodyBlockMapping> localbody_type =
        zoneLocalbodyBlockMappingRepository.findByZone(zone.get().getZoneId());
    if (localbody_type.isPresent()) {
      if (localbody_type.get().getBlockPanchayatMunicipalArea() == 1) {
        Optional<MasterBlock> localbody =
            masterBlockRepository.findById(localbody_type.get().getBlockDetails());
        localBodyLabel = localbody.map(MasterBlock::getBlockName).orElse("");
        localbodyType = "Block Panchayath";
      } else {
        Optional<TblLocalBody> localBody =
            localBodyRepository.findById(localbody_type.get().getBlockDetails());
        localBodyLabel = localBody.map(TblLocalBody::getLocalbodyNameEn).orElse("");
        if (localBody.isPresent()) {
          Optional<LocalBodyType> localBodyTypeObj =
              localBodyTypeRepository.findById((long) localBody.get().getLocalbodyType());
          localbodyType = localBodyTypeObj.map(LocalBodyType::getName).orElse("");
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
        zoneName,
        panchayathResponses,
        new ArrayList<>(localBodyNameMap.keySet()),
        505,
        overallTotalArea,
        totalWetAreaZone,
        totalDryAreaZone,
        unclassifiedPanchayaths);
  }
}
