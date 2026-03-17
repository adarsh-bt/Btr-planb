package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.RequestsDTOs.*;
import cdti.aidea.earas.contract.RequestsDTOs.ClusterLimitRequest;
import cdti.aidea.earas.contract.Response.*;
import cdti.aidea.earas.model.Btr_models.*;
import cdti.aidea.earas.model.Btr_models.Masters.*;
import cdti.aidea.earas.repository.Btr_repo.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminManage {

  private final KeyplotsLimitLogRepository repository;

  private final ClusterLimitLogRepository clusterLimitLogRepository;

  private final TblMasterZoneRepository tblMasterZoneRepository;
  private final DesTalukRepository desTalukRepository;
  private final DistrictMasterRepository districtMasterRepository;
  private final ClusterApprovalLogRepository clusterApprovalLogRepository;
  private final ClusterMasterRepository clusterMasterRepository;
  private final TblBtrTypeRepository tblBtrTypeRepository;

  private final UserZoneAssignmentRepositoty userZoneAssignmentRepositoty;
  private final ZoneRevenueTalukMappingRepository zoneRevenueTalukMappingRepository;
  private final RevenueTalukRepository revenueTalukRepository;
  private final TblMasterVillageRepository tblMasterVillageRepository;
  private final TblMasterVillageBlockRepository tblMasterVillageBlockRepository;
  private final TblZoneRevenueVillageMappingRepository zoneRevenueVillageMappingRepository;
  private final TblZoneVillageBlockMappingRepository tblZoneVillageBlockMappingRepository;


  public List<KeyplotsLimitLogResponse> getAllKeyplots() {
    List<KeyplotsLimitLog> entities = repository.findAll();

    return entities.stream()
            .map(entity -> new KeyplotsLimitLogResponse(
                    entity.getId(),
                    entity.getKeyplotsLimit(),
                    entity.getIsEdited(),
                    entity.getIsActive(),
                    entity.getIsInActive(),
                    entity.getAddedBy(),
                    entity.getEditPermitter(),
                    entity.getRemarks(),
                    entity.getAgriStartYear(),
                    entity.getAgriEndYear()
            ))
            .collect(Collectors.toList());
  }

  public List<ZoneListResponse> AdminViewZonesByType(String type, Integer idValue) {
    try {
      List<TblMasterZone> zones = null;
      System.out.println("sssss");
      // Decide which ID to use based on the type (Taluk, District, or Directorate)
      if ("Taluk".equalsIgnoreCase(type)) {
        zones = tblMasterZoneRepository.findByDesTalukId(idValue);
      } else if ("District".equalsIgnoreCase(type)) {
        zones = tblMasterZoneRepository.findByDistId(idValue);
      } else if ("Directorate".equalsIgnoreCase(type)) {
        // If type is DIRECTORATE, use appropriate repository method (change if needed)
        zones = tblMasterZoneRepository.findAll();
      } else {
        throw new IllegalArgumentException("Invalid type. Use 'Taluk', 'District', or 'Directorate'.");
      }

      if (zones == null || zones.isEmpty()) {
        throw new IllegalArgumentException("No zones found for the given ID.");
      }

      // Directly map all zones to the response DTO
      List<ZoneListResponse> zoneList = zones.stream()
              .map(zone -> {
                // Fetch taluk
                Optional<DesTaluk> taluk = desTalukRepository.findById(Math.toIntExact(zone.getDesTalukMaster().getDesTalukId()));
                String talukName = taluk.map(DesTaluk::getDesTalukNameEn).orElse("Unknown Taluk");

                // Fetch district
                Optional<DistrictMaster> district = districtMasterRepository.findById(Long.valueOf(zone.getDistId()));
                String districtName = district.map(DistrictMaster::getDist_name_en).orElse("Unknown District");

                // Fetch active user assignment
                Optional<UserZoneAssignment> activeAssignment =
                        userZoneAssignmentRepositoty.findByTblMasterZoneAndIsActiveTrue(zone);

                UUID assignedUserId = activeAssignment.map(UserZoneAssignment::getUserId).orElse(null);

                // Build response including assigned user
                return new ZoneListResponse(
                        zone.getZoneId(),
                        zone.getZoneCode(),
                        zone.getZoneNameEn(),
                        zone.getZoneNameMal(),
                        zone.getBtrType().getBtrType(),
                        Math.toIntExact(zone.getDesTalukMaster().getDesTalukId()),
                        zone.getDistrictMaster().getDist_id(),
                        talukName,
                        districtName,
                        assignedUserId  // <-- New field added
                );
              })
              .collect(Collectors.toList());
      zoneList.sort(Comparator.comparing(ZoneListResponse::getDesTalukId).reversed());
      zoneList.sort(Comparator.comparing(ZoneListResponse::getDesDistId).reversed());
      return zoneList;

    } catch (Exception e) {
      throw new IllegalArgumentException("Something went wrong while fetching zones", e);
    }
  }

  public List<ClusterApprovalTableDTO> zoneListForClusters(String type, Integer idValue) {
    List<ClusterApprovalLog> approvalLogs;
    System.out.println("type " + type + " : " + idValue);
    if ("Taluk".equalsIgnoreCase(type)) {
      approvalLogs = clusterApprovalLogRepository
              .findByZone_DesTalukId(idValue);
    } else if ("District".equalsIgnoreCase(type)) {
      approvalLogs = clusterApprovalLogRepository
              .findByZone_DistId(idValue);
    } else if ("Directorate".equalsIgnoreCase(type)) {
      approvalLogs = clusterApprovalLogRepository.findAll();

    } else {
      throw new IllegalArgumentException(
              "Invalid type. Use 'Taluk', 'District', or 'Directorate'");
    }

    if (approvalLogs.isEmpty()) {
      return Collections.emptyList();
    }

    return approvalLogs.stream()
            .map(this::mapToDTO)
            .sorted(Comparator
                    .comparing(ClusterApprovalTableDTO::getDistrictId).reversed()
                    .thenComparing(ClusterApprovalTableDTO::getTalukId).reversed())
            .collect(Collectors.toList());
  }

  @Transactional
  public ClusterApprovalResponseDTO clusterApprovals(
          @Valid ClusterApprovalActionDTO request) {

    if (request.getApprovalLogId() == null) {
      throw new IllegalArgumentException("Approval ID must be provided");
    }

    ClusterApprovalLog approvalLog = clusterApprovalLogRepository
            .findById(request.getApprovalLogId())
            .orElseThrow(() ->
                    new IllegalArgumentException("Approval Log not found"));

    ClusterMaster clusterMaster = approvalLog.getClusterMaster();
    if (request.getIs_edit() != null && request.getIs_edit()) {
      clusterMaster.setIs_editable(true);
      clusterMaster.setStatus("On Going");
    } else {
      clusterMaster.setStatus("Completed");
    }

    clusterMasterRepository.save(clusterMaster);

    approvalLog.setAdminId(request.getApprover_id());
    approvalLog.setInApproved(true);
    approvalLog.setApprovedDate(LocalDateTime.now());
    approvalLog.setRemarks(request.getRemarks());

    clusterApprovalLogRepository.save(approvalLog);

    // 🔥 RETURN DTO (NOT ENTITY)
    return new ClusterApprovalResponseDTO(
            approvalLog.getId(),
            approvalLog.getInApproved(),
            approvalLog.getAdminId(),
            approvalLog.getApprovedDate(),
            "SUCCESS"
    );
  }


  public List<ClusterLimitRequest> getAllClusterLimits() {
    List<ClusterLimitLog> entity = clusterLimitLogRepository.findAll();

    return entity.stream()
            .map(entitys -> new ClusterLimitRequest(
                    entitys.getId(),
                    entitys.getClusterMin(),
                    entitys.getClusterMax(),
                    entitys.getTsoApprovalLimit(),
//                    entity.getIsEdited(),
                    entitys.getAddedBy(),
                    entitys.getRemarks(),
                    entitys.getAgriStartYear(),
                    entitys.getAgriEndYear(),
                    entitys.getInActive()
            ))
            .collect(Collectors.toList());
  }

  private ClusterApprovalTableDTO mapToDTO(ClusterApprovalLog log) {

    TblMasterZone zone = log.getZone();
    ClusterMaster cluster = log.getClusterMaster();

    // Taluk
    DesTaluk taluk = desTalukRepository
            .findById(Math.toIntExact(zone.getDesTalukMaster().getDesTalukId()))
            .orElse(null);

    // District
    DistrictMaster district = districtMasterRepository
            .findById(Long.valueOf(zone.getDistId()))
            .orElse(null);

    return new ClusterApprovalTableDTO(

            // Approval
            log.getId(),

            // Cluster
            cluster.getCluMasterId(),
            cluster.getClusterNumber(),
            cluster.getKeyPlot().getLandType(),
            // Zone
            zone.getZoneId(),
            zone.getZoneNameEn(),
            // Taluk
            taluk != null ? taluk.getDesTalukId() : null,
            taluk != null ? taluk.getDesTalukNameEn() : null,
            // District
            district != null ? district.getDist_id() : null,
            district != null ? district.getDist_name_en() : null,
            // Area & status
            log.getTotalArea(),
            log.getInApproved(),
            // Audit
            log.getAddedBy(),
            log.getAdminId(),
            log.getRemarks(),
            cluster.getInvestigatorRemark(),
            log.getCreatedAt(),
            log.getApprovedDate()
    );
  }


  public KeyplotsLimitLog saveOrUpdateKeyplotsLimit(KeyplotsLimitLogRequest request) {
    KeyplotsLimitLog log;

    // === UPDATE PATH ===
    if (request.getId() != null) {
      log = repository.findById(request.getId())
              .orElseThrow(() -> new RuntimeException("Record not found with ID: " + request.getId()));

      // ❗ Only allow edit if marked editable
      if (!Boolean.TRUE.equals(log.getIsEdited())) {
        throw new IllegalStateException("Edit not allowed. This record is not editable.");
      }

      // ❗ Editor (addedBy) must not be same as editPermitter (admin who allowed edit)
      if (request.getAddedBy() == null || request.getAddedBy().equals(log.getEditPermitter())) {
        throw new IllegalStateException("Edit not allowed by admin. Another user must perform the edit.");
      }

      // ✅ Update allowed
      log.setKeyplotsLimit(request.getKeyplotsLimit());
      log.setRemarks(request.getRemarks());
      log.setUpdatedAt(LocalDateTime.now());

    } else {
      // === CREATE PATH ===
      int year = LocalDate.now().getYear();

      LocalDate agriStart = LocalDate.of(year, 7, 1);
      LocalDate agriEnd = LocalDate.of(year + 1, 6, 30);

      // ❗ Prevent duplicate active agri year
      boolean exists = repository.existsByAgriStartYearAndIsActive(agriStart, true);
      if (exists) {
        throw new RuntimeException("A record already exists for the current agri year: " + agriStart + " To " + agriEnd);
      }

      log = new KeyplotsLimitLog();
      log.setKeyplotsLimit(request.getKeyplotsLimit());
      log.setIsEdited(request.getIsEdited() != null ? request.getIsEdited() : false);
//            log.setEditPermitter(request.getEditPermitter());
      log.setAddedBy(request.getAddedBy());
      log.setRemarks(request.getRemarks());
      log.setAgriStartYear(agriStart);
      log.setAgriEndYear(agriEnd);
      log.setIsActive(true);

      log.setCreatedAt(LocalDateTime.now());
      log.setUpdatedAt(LocalDateTime.now());
    }

    return repository.save(log);
  }


  @Transactional
  public ClusterLimitLog saveOrUpdateClusterLimit(ClusterLimitRequest request) {
    ClusterLimitLog log;

    if (request.getId() != null) {
      // update path unchanged except do not flip inActive unless explicitly intended
      log = clusterLimitLogRepository.findById(request.getId())
              .orElseThrow(() -> new RuntimeException("Record not found with ID: " + request.getId()));

      if (!Boolean.TRUE.equals(log.getIsEdited())) {
        throw new IllegalStateException("Edit not allowed. This record is not editable.");
      }
      if (request.getAddedBy() == null || request.getAddedBy().equals(log.getEditPermitter())) {
        throw new IllegalStateException("Edit not allowed by admin. Another user must perform the edit.");
      }

      log.setClusterMin(request.getClusterMin());
      log.setClusterMax(request.getClusterMax());
      log.setRemarks(request.getRemarks());

      // If you do NOT want updates to change active status, remove this block:
      // if (request.getInActive() != null) {
      //     log.setInActive(request.getInActive());
      // }

      log.setUpdatedAt(LocalDateTime.now());
      return clusterLimitLogRepository.save(log);
    } else {
//      LocalDate agriStart = LocalDate.now();
//      LocalDate agriEnd = agriStart.plusYears(1).minusDays(1);

      int year = LocalDate.now().getYear();

      LocalDate agriStart = LocalDate.of(year, 7, 1);
      LocalDate agriEnd = LocalDate.of(year + 1, 6, 30);
      int startYear = agriStart.getYear();
      int endYear = agriEnd.getYear();

      // ✅ Prevent duplicate agri year by year only
      if (clusterLimitLogRepository.existsByAgriStartAndEndYear(startYear, endYear)) {
        throw new IllegalStateException("A record already exists for agri year " + startYear + " - " + endYear);
      }

      // Deactivate all previous active records
      clusterLimitLogRepository.deactivateAll();

      // Create new active record (inActive=true means current active per your requirement)
      log = new ClusterLimitLog();
      log.setClusterMin(request.getClusterMin());
      log.setClusterMax(request.getClusterMax());
      log.setAddedBy(request.getAddedBy());
      log.setRemarks(request.getRemarks());
      log.setAgriStartYear(agriStart);
      log.setAgriEndYear(agriEnd);
      log.setTsoApprovalLimit(request.getTsoLimit());
      log.setIsEdited(false);

      // Single source of truth: inActive=true for the new active config
      log.setInActive(true);

      // If isActive exists but is redundant, keep it aligned or remove it from the entity
      // log.setIsActive(true);

      log.setCreatedAt(LocalDateTime.now());
      log.setUpdatedAt(LocalDateTime.now());
      return clusterLimitLogRepository.save(log);
    }
  }

  public ZonePageResponse getAllZones(int page, int size) {

    Pageable pageable = PageRequest.of(page, size, Sort.by("zoneId").ascending());

    Page<TblMasterZone> zonePage = tblMasterZoneRepository.findAll(pageable);

    List<ZoneListResponse> zoneList = zonePage.getContent().stream()
            .map(zone -> {

              String talukName = desTalukRepository
                      .findById(Math.toIntExact(zone.getDesTalukMaster().getDesTalukId()))
                      .map(DesTaluk::getDesTalukNameEn)
                      .orElse("Unknown Taluk");

              String districtName = districtMasterRepository
                      .findById(Long.valueOf(zone.getDistId()))
                      .map(DistrictMaster::getDist_name_en)
                      .orElse("Unknown District");

              return new ZoneListResponse(
                      zone.getZoneId(),
                      zone.getZoneCode(),
                      zone.getZoneNameEn(),
                      zone.getZoneNameMal(),
                      zone.getBtrType() != null ? zone.getBtrType().getBtrType() : null,
                      Math.toIntExact(zone.getDesTalukMaster().getDesTalukId()),
                      zone.getDistrictMaster().getDist_id(),
                      talukName,
                      districtName,
                      null
              );
            })
            .toList();

    return new ZonePageResponse(
            zoneList,
            zonePage.getNumber(),
            zonePage.getTotalElements(),
            zonePage.getTotalPages()
    );
  }

  public ZoneMappingResponseDTO getZoneDetails(Integer zoneId) {

    TblMasterZone zone = tblMasterZoneRepository.findById(zoneId)
            .orElseThrow(() -> new RuntimeException("Zone not found"));

    List<Object[]> rows = zoneRevenueTalukMappingRepository.getZoneHierarchy(zoneId);

    Map<Integer, TalukDTO> talukMap = new LinkedHashMap<>();

    for (Object[] row : rows) {

      Long talukMappingId = row[0] != null ? Long.valueOf(row[0].toString()) : null;
      Integer talukId = row[1] != null ? Integer.valueOf(row[1].toString()) : null;
      String talukName = row[2] != null ? row[2].toString() : null;

      Long villageMappingId = row[3] != null ? Long.valueOf(row[3].toString()) : null;
      Integer villageId = row[4] != null ? Integer.valueOf(row[4].toString()) : null;
      String villageName = row[5] != null ? row[5].toString() : null;

      Long blockMappingId = row[6] != null ? Long.valueOf(row[6].toString()) : null;
      String blockCode = row[7] != null ? row[7].toString() : null;

      TalukDTO talukDTO = talukMap.computeIfAbsent(
              talukId,
              id -> new TalukDTO(talukMappingId, talukId, talukName)
      );

      // If village is null → skip village/block processing
      if (villageId == null) {
        continue;
      }

      VillageDTO villageDTO = talukDTO.getVillages()
              .stream()
              .filter(v -> v.getVillageId().equals(villageId))
              .findFirst()
              .orElseGet(() -> {

                VillageDTO newVillage =
                        new VillageDTO(villageMappingId, villageId, villageName);

                talukDTO.getVillages().add(newVillage);
                return newVillage;

              });

      if (blockCode != null) {

        boolean exists = villageDTO.getBlocks()
                .stream()
                .anyMatch(b -> b.getBlockCode().equals(blockCode));

        if (!exists) {
          villageDTO.getBlocks().add(
                  new BlockDTO(blockMappingId, blockCode)
          );
        }
      }
    }

    return new ZoneMappingResponseDTO(
            zone.getZoneId(),
            zone.getZoneNameEn(),
            zone.getBtrType() != null ? zone.getBtrType().getBtrType() : null,
            new ArrayList<>(talukMap.values())
    );
  }

  public List<TalukDTO> getTaluksByZone(Integer zoneId) {

    TblMasterZone zone = tblMasterZoneRepository
            .findByZoneIdAndIsActiveTrue(zoneId)
            .orElseThrow(() -> new RuntimeException("Zone not found"));

    Integer distId = zone.getDistId();

    List<RevTaluk> taluks = revenueTalukRepository
            .findByDistIdAndIsActiveTrue(distId);

    return taluks.stream()
            .map(t -> new TalukDTO(
                    Math.toIntExact(t.getRevTalukId()),
                    t.getRevTalukNameEn()
            ))
            .toList();
  }

  public List<VillageDTO> getVillages(Integer talukId) {

    List<TblMasterVillage> villages =
            tblMasterVillageRepository.findByRevTalukIdAndIsActiveTrue(talukId);

    return villages.stream()
            .map(v -> new VillageDTO(
                    null,
                    v.getVillageId(),
                    v.getVillageNameEn()
            ))
            .toList();
  }

  public List<BlockDTO> getBlocksByVillage(Integer villageId) {

    List<TblMasterVillageBlock> blocks =
            tblMasterVillageBlockRepository.findByVillageId(villageId);

    return blocks.stream()
            .map(b -> new BlockDTO(
                    b.getVillageBlockId().longValue(),
                    b.getBlockCode()
            ))
            .toList();
  }


  @Transactional
  public void saveZoneMapping(AddZoneMappingRequest request) {
System.out.println("request "+ request);
    Integer zoneId = Math.toIntExact(request.getZoneId());

    zoneRevenueTalukMappingRepository
            .findByZoneAndRevenueTalukAndIsValidTrue(zoneId, request.getTalukId())
            .orElseGet(() -> {

              ZoneRevenueTalukMapping taluk = new ZoneRevenueTalukMapping();
              taluk.setZone(zoneId);
              taluk.setRevenueTaluk(request.getTalukId());
              taluk.setIsValid(true);
              taluk.setCreatedAt(LocalDateTime.now());
              taluk.setAddedBy(request.getUserId());
              return zoneRevenueTalukMappingRepository.save(taluk);
            });

    if (request.getVillageId() == null) {
      return;
    }

    zoneRevenueVillageMappingRepository
            .findByZoneAndRevenueVillageAndIsValidTrue(zoneId, request.getVillageId())
            .orElseGet(() -> {

              TblZoneRevenueVillageMapping village = new TblZoneRevenueVillageMapping();
              village.setZone(zoneId);
              village.setRevenueVillage(request.getVillageId());
              village.setIsValid(true);
              village.setCreatedAt(LocalDateTime.now());
              village.setAddedBy(request.getUserId());
              village.setRevenueTaluk(request.getTalukId());

              return zoneRevenueVillageMappingRepository.save(village);
            });

    /* AUTO MAP BLOCKS IF NONE PROVIDED */

    if (request.getBlockCodes() == null || request.getBlockCodes().isEmpty()) {

      List<TblMasterVillageBlock> blocks =
              tblMasterVillageBlockRepository.findByVillageId(request.getVillageId());

      for (TblMasterVillageBlock block : blocks) {

        Integer blockId = block.getVillageBlockId();

        boolean exists =
                tblZoneVillageBlockMappingRepository
                        .existsByZoneAndVillageBlockIdAndIsValidTrue(zoneId, blockId);

        if (!exists) {

          TblZoneVillageBlockMapping blockMapping = new TblZoneVillageBlockMapping();
          blockMapping.setZone(zoneId);
          blockMapping.setVillageBlockId(blockId);
          blockMapping.setVillageId(request.getVillageId());
          blockMapping.setIsValid(true);
          blockMapping.setCreatedAt(LocalDateTime.now());
          blockMapping.setAddedBy(request.getUserId());

          tblZoneVillageBlockMappingRepository.save(blockMapping);
        }
      }

      return;
    }

    /* USER SELECTED BLOCKS */

    for (String blockId : request.getBlockCodes()) {

      Integer block = Integer.valueOf(blockId);

      boolean exists =
              tblZoneVillageBlockMappingRepository
                      .existsByZoneAndVillageBlockIdAndIsValidTrue(zoneId, block);

      if (!exists) {

        TblZoneVillageBlockMapping blockMapping = new TblZoneVillageBlockMapping();
        blockMapping.setZone(zoneId);
        blockMapping.setVillageBlockId(block);
        blockMapping.setVillageId(request.getVillageId()); // ✅ FIX
        blockMapping.setIsValid(true);
        blockMapping.setCreatedAt(LocalDateTime.now());
        blockMapping.setAddedBy(request.getUserId());

        tblZoneVillageBlockMappingRepository.save(blockMapping);
      }
    }
  }


  public TblMasterZone saveOrUpdate(ZoneCreateRequest request) {

    TblMasterZone zone;

    // 🔹 UPDATE
    if (request.getZoneId() != null) {

      zone = tblMasterZoneRepository.findById(request.getZoneId())
              .orElseThrow(() -> new RuntimeException("Zone not found with id: " + request.getZoneId()));

      if (request.getZoneNameEn() != null) zone.setZoneNameEn(request.getZoneNameEn());
      if (request.getZoneNameMal() != null) zone.setZoneNameMal(request.getZoneNameMal());
      if (request.getIsActive() != null) zone.setIsActive(request.getIsActive());
      if (request.getZoneUser() != null) zone.setZoneUser(request.getZoneUser());

      if (request.getBtrTypeId() != null) {
        TblBtrType btr = tblBtrTypeRepository.findById(request.getBtrTypeId())
                .orElseThrow(() -> new RuntimeException("BTR Type not found with id: " + request.getBtrTypeId()));
        zone.setBtrType(btr);
      }

      if (request.getDesDistId() != null) {
        DistrictMaster district = districtMasterRepository.findById(Long.valueOf(request.getDesDistId()))
                .orElseThrow(() -> new RuntimeException("District not found with id: " + request.getDesDistId()));
        zone.setDistId(district.getDist_id());
        zone.setDistrictMaster(district);
      }

      if (request.getDesTalukId() != null) {
        DesTaluk taluk = desTalukRepository.findById(request.getDesTalukId())
                .orElseThrow(() -> new RuntimeException("Taluk not found with id: " + request.getDesTalukId()));
        zone.setDesTalukId(taluk.getDesTalukId());
        zone.setDesTalukMaster(taluk);
      }
      zone.setUpdatedAt(LocalDateTime.now());
      zone.setUpdatedBy(request.getUserId());
    } else {
      // 🔹 CREATE
      if (request.getDesDistId() == null) throw new RuntimeException("District ID is required");
      if (request.getDesTalukId() == null) throw new RuntimeException("Taluk ID is required");

      zone = new TblMasterZone();
      zone.setZoneCode(request.getZoneCode());
      zone.setZoneNameEn(request.getZoneNameEn());
      zone.setZoneNameMal(request.getZoneNameMal());
      zone.setZoneUser(request.getZoneUser());
      zone.setAddedBy(request.getUserId());
      zone.setCreatedAt(LocalDateTime.now());
      zone.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

      // Fetch and set Taluk
      DesTaluk taluk = desTalukRepository.findById(request.getDesTalukId())
              .orElseThrow(() -> new RuntimeException("Taluk not found with id: " + request.getDesTalukId()));
      zone.setDesTalukId(taluk.getDesTalukId());
      zone.setDesTalukMaster(taluk);

      // Fetch and set District
      DistrictMaster district = districtMasterRepository.findById(Long.valueOf(request.getDesDistId()))
              .orElseThrow(() -> new RuntimeException("District not found with id: " + request.getDesDistId()));
      zone.setDistId(district.getDist_id());
      zone.setDistrictMaster(district);

      // Set BTR Type if provided
      if (request.getBtrTypeId() != null) {
        TblBtrType btr = tblBtrTypeRepository.findById(request.getBtrTypeId())
                .orElseThrow(() -> new RuntimeException("BTR Type not found with id: " + request.getBtrTypeId()));
        zone.setBtrType(btr);
      }
    }

    return tblMasterZoneRepository.save(zone);
  }
}