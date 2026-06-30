package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.RequestsDTOs.*;
import cdti.aidea.earas.contract.RequestsDTOs.ClusterLimitRequest;
import cdti.aidea.earas.contract.Response.*;
import cdti.aidea.earas.model.Btr_models.*;
import cdti.aidea.earas.model.Btr_models.Masters.*;
import cdti.aidea.earas.repository.Btr_repo.*;
import cdti.aidea.earas.utils.AgriYearUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
  private final TblZoneLocalbodyMappingRepository tblZoneLocalbodyMappingRepository;
  private final ClusterEditAllowedRepository editAllowedRepository;
  private final TblMasterZoneRepository masterZoneRepository;
  private final ZoneLocalbodyBlockMappingRepository zoneLocalbodyBlockMappingRepository;
  private final MasterBlockRepository masterBlockRepository;
  private final LocalBodyRepository localBodyRepository;
  private final LocalBodyTypeRepository localBodyTypeRepository;
  private final TblWorkAllocationRepository tblWorkAllocationRepository;
  private final TblWorkAllocationApprovalRepository tblWorkAllocationApprovalRepository;
  private AgriYearUtil agriYearUtil;

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

    List<TblMasterZone> zones;
System.out.println(">>>>||   "+type+"   "+idValue);
    if ("Taluk".equalsIgnoreCase(type)) {
      zones = tblMasterZoneRepository.findByDesTalukId(idValue);

    } else if ("District".equalsIgnoreCase(type)) {
      zones = tblMasterZoneRepository.findByDistId(idValue);

    } else {
      zones = tblMasterZoneRepository.findAll();
    }

    // Load lookup tables ONCE
    Map<Integer, DesTaluk> talukMap = desTalukRepository.findAll()
            .stream()
            .collect(Collectors.toMap(
                    DesTaluk::getDesTalukId,
                    Function.identity()
            ));

    Map<Integer, DistrictMaster> districtMap = districtMasterRepository.findAll()
            .stream()
            .collect(Collectors.toMap(
                    DistrictMaster::getDist_id,
                    Function.identity()
            ));

    Map<Integer, UUID> assignmentMap = userZoneAssignmentRepositoty
            .findAllActiveAssignments()
            .stream()
            .collect(Collectors.toMap(
                    assignment -> assignment.getTblMasterZone().getZoneId(),
                    UserZoneAssignment::getUserId
            ));

    return zones.stream()
            .map(zone -> {

              DesTaluk taluk = talukMap.get(zone.getDesTalukId());
              DistrictMaster district = districtMap.get(zone.getDistId());

              return new ZoneListResponse(
                      zone.getZoneId(),
                      zone.getZoneCode(),
                      zone.getZoneNameEn(),
                      zone.getZoneNameMal(),
                      zone.getBtrType().getBtrType(),
                      zone.getDesTalukId(),
                      zone.getDistId(),
                      taluk != null ? taluk.getDesTalukNameEn() : "",
                      district != null ? district.getDist_name_en() : "",
                      assignmentMap.get(zone.getZoneId())
              );
            })
            .sorted(
                    Comparator.comparing(ZoneListResponse::getDesDistId)
                            .thenComparing(ZoneListResponse::getDesTalukId)
            )
            .toList();
  }

//  infinte
//  public Page<ZoneListResponse> AdminViewZonesByType(
//        String type,
//        Integer idValue,
//        Pageable pageable) {
//
//  Page<TblMasterZone> zones;
//
//  if ("Taluk".equalsIgnoreCase(type)) {
//
//    zones = tblMasterZoneRepository.findByDesTalukId(idValue, pageable);
//
//  } else if ("District".equalsIgnoreCase(type)) {
//
//    zones = tblMasterZoneRepository.findByDistId(idValue, pageable);
//
//  } else {
//
//    zones = tblMasterZoneRepository.findAll(pageable);
//
//  }
//
//  Map<Integer, DesTaluk> talukMap =
//          desTalukRepository.findAll()
//                  .stream()
//                  .collect(Collectors.toMap(
//                          DesTaluk::getDesTalukId,
//                          Function.identity()
//                  ));
//
//  Map<Integer, DistrictMaster> districtMap =
//          districtMasterRepository.findAll()
//                  .stream()
//                  .collect(Collectors.toMap(
//                          DistrictMaster::getDist_id,
//                          Function.identity()
//                  ));
//
//  Map<Integer, UUID> assignmentMap =
//          userZoneAssignmentRepositoty.findAllActiveAssignments()
//                  .stream()
//                  .collect(Collectors.toMap(
//                          a -> a.getTblMasterZone().getZoneId(),
//                          UserZoneAssignment::getUserId
//                  ));
//
//  return zones.map(zone -> {
//
//    DesTaluk taluk =
//            talukMap.get(zone.getDesTalukId());
//
//    DistrictMaster district =
//            districtMap.get(zone.getDistId());
//
//    return new ZoneListResponse(
//
//            zone.getZoneId(),
//
//            zone.getZoneCode(),
//
//            zone.getZoneNameEn(),
//
//            zone.getZoneNameMal(),
//
//            zone.getBtrType().getBtrType(),
//
//            zone.getDesTalukId(),
//
//            zone.getDistId(),
//
//            taluk != null
//                    ? taluk.getDesTalukNameEn()
//                    : "",
//
//            district != null
//                    ? district.getDist_name_en()
//                    : "",
//
//            assignmentMap.get(zone.getZoneId())
//
//    );
//
//  });
//
//}


  public List<ClusterApprovalTableDTO> zoneListForClusterss(String type, Integer idValue) {
    List<ClusterApprovalLog> approvalLogs;

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

  public Page<ClusterApprovalTableDTO> zoneListForClusters(
          String type,
          Integer idValue,
          int page,
          int size,
          String agriYear
  ) {
    LocalDate agriStart = AgriYearUtil.getAgriYearStart(agriYear);
    LocalDate agriEnd = AgriYearUtil.getAgriYearEnd(agriYear);

    Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "createdAt")
    );

    Page<ClusterApprovalLog> approvalLogs;

    System.out.println("type " + type + " : " + idValue);

    if ("Taluk".equalsIgnoreCase(type)) {

      approvalLogs =
              clusterApprovalLogRepository
                      .findByTalukAndAgriYear(
                              idValue,
                              agriStart,
                              agriEnd,
                              pageable
                      );

    } else if ("District".equalsIgnoreCase(type)) {

      approvalLogs =
              clusterApprovalLogRepository
                      .findByDistrictAndAgriYear(
                              idValue,
                              agriStart,
                              agriEnd,
                              pageable
                      );

    } else if ("Directorate".equalsIgnoreCase(type)) {

      approvalLogs =
              clusterApprovalLogRepository
                      .findByAgriYear(
                              agriStart,
                              agriEnd,
                              pageable
                      );

    } else {

      throw new IllegalArgumentException(
              "Invalid type. Use 'Taluk', 'District', or 'Directorate'"
      );
    }

    return approvalLogs.map(this::mapToDTO);
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

  //  work allocation
  public Page<WorkAllocationApprovalTableDTO> zoneListForWorkAllocation(
          String type,
          Integer idValue,
          int page,
          int size,
          String agriYear
  ) {

    LocalDate agriStart =
            AgriYearUtil.getAgriYearStart(agriYear);

    LocalDate agriEnd =
            AgriYearUtil.getAgriYearEnd(agriYear);

    Pageable pageable =
            PageRequest.of(
                    page,
                    size,
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );

    Page<TblWorkAllocationApproval> approvals;

    if ("Taluk".equalsIgnoreCase(type)) {

      approvals =
              tblWorkAllocationApprovalRepository
                      .findByTalukIdAndAgriYear(
                              idValue,
                              agriStart,
                              agriEnd,
                              pageable
                      );

    } else if ("District".equalsIgnoreCase(type)) {

      approvals =
              tblWorkAllocationApprovalRepository
                      .findByDistrictIdAndAgriYear(
                              idValue,
                              agriStart,
                              agriEnd,
                              pageable
                      );

    } else if ("Directorate".equalsIgnoreCase(type)) {

      approvals =
              tblWorkAllocationApprovalRepository
                      .findAllSubmittedByAgriYear(
                              agriStart,
                              agriEnd,
                              pageable
                      );

    } else {

      throw new IllegalArgumentException(
              "Invalid type. Use 'Taluk', 'District', or 'Directorate'"
      );
    }

    return approvals.map(this::convertToApprovalDTO);
  }

  private WorkAllocationApprovalTableDTO convertToApprovalDTO(TblWorkAllocationApproval approval) {
    WorkAllocationApprovalTableDTO dto = new WorkAllocationApprovalTableDTO();
    dto.setApprovalId(approval.getId());
    dto.setStatus(approval.getStatus());
    dto.setRequestedBy(approval.getRequestedBy());
    dto.setApprovedBy(approval.getApprovedBy());
    dto.setRemarks(approval.getRemark());
    dto.setCreatedAt(approval.getCreatedAt());
    dto.setApprovedDate(approval.getApprovedDate());
    dto.setCreatedAt(approval.getCreatedAt());

    // Pull connected zone data through an active data row in the target batch
    List<TblWorkAllocation> linkedRows = tblWorkAllocationRepository.findByApprovalId(approval.getId());
    if (!linkedRows.isEmpty()) {
      TblMasterZone zone = linkedRows.get(0).getZone();
      if (zone != null) {
        dto.setZoneId(zone.getZoneId());
        dto.setZoneName(zone.getZoneNameEn());
        dto.setTalukId(zone.getDesTalukId());
        dto.setTalukName(zone.getDesTalukMaster().getDesTalukNameEn()); // Assumes lookup names exist in your TblMasterZone metadata
        dto.setDistrictId(zone.getDistId());
        dto.setDistrictName(zone.getDistrictMaster().getDist_name_en());
      }
    }
    return dto;

  }


  @Transactional
  public WorkAllocationApproveDTO approveOrRejectWorkAllocation(@Valid WorkAllocationApproveDTO dto) {

    if (dto.getApprovalLogId() == null) {
      throw new IllegalArgumentException("Approval ID must be provided");
    }

    // 1. Find the parent approval log tracking record
    TblWorkAllocationApproval approval = tblWorkAllocationApprovalRepository.findById(dto.getApprovalLogId())
            .orElseThrow(() -> new RuntimeException("Approval record not found for ID: " + dto.getApprovalLogId()));

    // 2. Fetch all allocation rows associated with this specific approval batch ID
    List<TblWorkAllocation> allocations = tblWorkAllocationRepository.findByApprovalId(approval.getId());

    String finalStatus;
    boolean targetIsEdit;

    // 3. Determine the 3 branching pathways
    if (Boolean.TRUE.equals(dto.getApprove())) {
      if (Boolean.TRUE.equals(dto.getIs_edit())) {
        // Path 2: Approved but explicitly put under review with edit mode ON
        finalStatus = "UNDER REVIEW";
        targetIsEdit = true;
      } else {
        // Path 1: Standard final approval
        finalStatus = "APPROVED";
        targetIsEdit = false;
      }
    } else {
      // Path 3: Returned to user for correction
      finalStatus = "RETURNED";
      targetIsEdit = true;
    }

    // 4. Update approval record parameters
    approval.setStatus(finalStatus);
    approval.setApprovedBy(dto.getApprover_id());
    approval.setApprovedDate(LocalDateTime.now());
    approval.setRemark(dto.getRemarks()); // Matches entity property 'remark'

    tblWorkAllocationApprovalRepository.save(approval);

    // 5. Mass-update all linked row states to change users' UI inputs access
    for (TblWorkAllocation allocation : allocations) {
      allocation.setIsEdit(targetIsEdit);
      allocation.setUpdated(LocalDate.now());
      tblWorkAllocationRepository.save(allocation);
    }

    // 6. 🔥 RETURN DTO MATCHING THE CLUSTER PATTERN (NOT VOID)
    return new WorkAllocationApproveDTO(
            approval.getId(),
            true,
            approval.getApprovedBy(),
            approval.getRemark(),
            approval.getIsActive()
    );
  }
//  end

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

  public ZonePageResponse getAllZones(int page, int size, String search) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("zoneId").ascending());
    Page<TblMasterZone> zonePage;

    if (search != null && !search.trim().isEmpty()) {
      zonePage = tblMasterZoneRepository.searchZones(search.toLowerCase(), pageable);
    } else {
      zonePage = tblMasterZoneRepository.findAll(pageable); // ✅ ALL zones
    }

    List<ZoneListResponse> zoneList = zonePage.getContent().stream()
            .map(zone -> {

              String talukName = desTalukRepository
                      .findById(zone.getDesTalukId())
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
                      zone.getDesTalukId(),
                      zone.getDistId(),
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
    System.out.println("request " + request);
    Integer zoneId = Math.toIntExact(request.getZoneId());
    Optional<DesTaluk> des = desTalukRepository.findById(request.getTalukId());
    zoneRevenueTalukMappingRepository
            .findByZoneAndRevenueTalukAndIsValidTrue(zoneId, request.getTalukId())
            .orElseGet(() -> {

              ZoneRevenueTalukMapping taluk = new ZoneRevenueTalukMapping();
              taluk.setZone(zoneId);
              taluk.setRevenueTaluk(request.getTalukId());
              taluk.setRevTalukNameEn(des.get().getDesTalukNameEn());
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

  public List<LocalbodyDTO> getZoneLocalBodies(Integer zoneId) {

    List<Object[]> rows =
            tblZoneLocalbodyMappingRepository.findLocalBodiesByZone(zoneId);

    List<LocalbodyDTO> result = new ArrayList<>();

    for (Object[] row : rows) {

      LocalbodyDTO dto = new LocalbodyDTO(
              row[0] != null ? Integer.valueOf(row[0].toString()) : null,
              row[1] != null ? Integer.valueOf(row[1].toString()) : null,
              row[2] != null ? row[2].toString() : null,
              row[3] != null ? row[3].toString() : null,  // ✅ type name
              zoneId,
              null
      );

      result.add(dto);
    }

    return result;
  }

  public List<LocalbodyDTO> getAvailableLocalBodies(Integer zoneId) {

    List<Object[]> rows =
            tblZoneLocalbodyMappingRepository.findAvailableLocalBodies(zoneId);

    List<LocalbodyDTO> result = new ArrayList<>();

    for (Object[] row : rows) {
      result.add(new LocalbodyDTO(
              null,
              Integer.valueOf(row[0].toString()),
              row[1].toString(),
              row[2] != null ? row[2].toString() : null, // ✅ type name
              zoneId,
              null
      ));
    }

    return result;
  }

  @Transactional
  public void saveLocalBodyMapping(LocalbodyDTO request) {
    if (request.getUserId() == null) {
      throw new RuntimeException("UserId is required");
    }
    Integer zoneId = request.getZoneId();
    Integer localbodyId = request.getLocalbodyId();

    Optional<TblZoneLocalbodyMapping> existing =
            tblZoneLocalbodyMappingRepository
                    .findByZoneAndLocalbody(zoneId, localbodyId);
    if (existing.isPresent()) {
      TblZoneLocalbodyMapping mapping = existing.get();
      // 🔥 CASE 1: Already active → do nothing
      if (Boolean.TRUE.equals(mapping.getIsValid())) {
        return;
      }
      // 🔥 CASE 2: Was deleted → reactivate
      mapping.setIsValid(true);
      mapping.setUpdatedBy(request.getUserId());

      tblZoneLocalbodyMappingRepository.save(mapping);

    } else {

      // 🔥 CASE 3: New mapping
      TblZoneLocalbodyMapping mapping = new TblZoneLocalbodyMapping();
      mapping.setZone(zoneId);
      mapping.setLocalbody(localbodyId);
      mapping.setIsValid(true);
      mapping.setAddedBy(request.getUserId());
      mapping.setCreatedAt(LocalDateTime.now());
      tblZoneLocalbodyMappingRepository.save(mapping);
    }
  }

  @Transactional
  public void removeLocalBodyMapping(Integer id, UUID userId) {

    if (userId == null) {
      throw new RuntimeException("UserId is required");
    }
    TblZoneLocalbodyMapping mapping =
            tblZoneLocalbodyMappingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("LocalBody mapping not found"));
    // 🔥 Already removed → ignore (idempotent)
    if (Boolean.FALSE.equals(mapping.getIsValid())) {
      return;
    }
    // 🔥 Soft delete
    mapping.setIsValid(false);
    mapping.setUpdatedBy(userId);
    mapping.setUpdatedAt(LocalDateTime.now());
    tblZoneLocalbodyMappingRepository.save(mapping);
  }

  @Transactional
  public ClusterEditAllowed createClusterEditRequest(ClusterEditRequestDTO dto) {

    // 🔹 clusterId validation
    if (dto.getClusterId() == null) {
      throw new RuntimeException("clusterId is required");
    }

    // 🔹 zoneId validation
    if (dto.getZoneId() == null) {
      throw new RuntimeException("zoneId is required");
    }

    // 🔹 status validation
    if (dto.getStatus() == null || dto.getStatus().isBlank()) {
      throw new RuntimeException("status is required");
    }

    // 🔹 APPROVED → approvedBy must be present
    if ("APPROVED".equalsIgnoreCase(dto.getStatus()) && dto.getApprovedBy() == null) {
      throw new RuntimeException("approvedBy is required when status is APPROVED");
    }

    //Fetch Cluster
    ClusterMaster cluster = clusterMasterRepository.findById(dto.getClusterId())
            .orElseThrow(() -> new RuntimeException("Cluster not found"));
    //Fetch Zone
    TblMasterZone zone = masterZoneRepository.findByZoneId(dto.getZoneId())
            .orElseThrow(() -> new RuntimeException("Zone not found"));

    ClusterEditAllowed entity = new ClusterEditAllowed();
    entity.setClusterMaster(cluster);
    entity.setZone(zone);
    entity.setRemarks(dto.getRemarks());
//    in case of not request that will be removed
    entity.setRequestedBy(dto.getApprovedBy());
    entity.setApprovedBy(dto.getApprovedBy());
    entity.setTotalArea(BigDecimal.valueOf(dto.getTotalArea()));
    //Enum handling
    if (dto.getStatus() != null) {
      try {
        entity.setStatus(EditRequestStatus.valueOf(dto.getStatus().toUpperCase()));
        // System.out.println(dto.getStatus());
      } catch (Exception e) {
        // entity.setStatus(EditRequestStatus.PENDING);
        throw new RuntimeException("Invalid status value. Allowed: PENDING, APPROVED, REJECTED");
      }
    }
    entity.setIsActive(true);
    entity.setCreatedAt(LocalDateTime.now());
    entity.setUpdatedAt(LocalDateTime.now());

    // ⚠️ Only set approvedAt if approved
    if (entity.getStatus() == EditRequestStatus.APPROVED) {
      entity.setApprovedAt(LocalDateTime.now());
    }

    // return editAllowedRepository.save(entity);
    ClusterEditAllowed saved = editAllowedRepository.save(entity);

    cluster.setIs_editable(true);
    cluster.setStatus("On Going"); // match your DB value exactly
    cluster.setUpdatedAt(LocalDateTime.now());
    clusterMasterRepository.save(cluster);
    return saved;
  }


  public List<ZoneBlockMappingResponseDto> getBlockMappings(Integer zoneId) {

    Optional<ZoneLocalbodyBlockMapping> optionalMapping =
            zoneLocalbodyBlockMappingRepository.findByZoneAndIsValid(zoneId, true);

    List<ZoneBlockMappingResponseDto> response = new ArrayList<>();

    if (optionalMapping.isPresent()) {
      ZoneLocalbodyBlockMapping map = optionalMapping.get();

      ZoneBlockMappingResponseDto dto = new ZoneBlockMappingResponseDto();
      dto.setId(map.getId());
      dto.setTypeCode(map.getBlockPanchayatMunicipalArea());
      dto.setIsActive(map.getIsValid());

      if (map.getBlockPanchayatMunicipalArea() == 1) {

        Optional<MasterBlock> block =
                masterBlockRepository.findById(map.getBlockDetails());

        dto.setType("Block Panchayat");
        dto.setName(block.map(MasterBlock::getBlockName).orElse(""));
        dto.setIsActive(map.getIsValid());

      } else {

        Optional<TblLocalBody> localBody =
                localBodyRepository.findById(map.getBlockDetails());

        if (localBody.isPresent()) {

          dto.setName(localBody.get().getLocalbodyNameEn());

          Optional<LocalBodyType> type =
                  localBodyTypeRepository.findById(
                          (long) localBody.get().getLocalbodyType()
                  );

          dto.setType(type.map(LocalBodyType::getName).orElse("Local Body"));
        }
      }

      response.add(dto);
    }

    return response;
  }

  @Transactional
  public void saveOrUpdateBlockMapping(ZoneBlockMappingRequestDto request) {

    // ✅ 1. Basic Validation
    if (request.getZoneId() == null) {
      throw new RuntimeException("Zone ID is required");
    }

    if (request.getTypeCode() == null) {
      throw new RuntimeException("Type is required");
    }

    if (request.getReferenceId() == null) {
      throw new RuntimeException("Reference ID is required");
    }

    // ✅ 2. Validate based on type
    if (request.getTypeCode() == 1) {

      boolean exists = masterBlockRepository.existsById(request.getReferenceId());
      if (!exists) {
        throw new RuntimeException("Invalid Block selected");
      }

    } else {

      boolean exists = localBodyRepository.existsById(request.getReferenceId());
      if (!exists) {
        throw new RuntimeException("Invalid Local Body selected");
      }
    }

    // ✅ 3. Check existing mapping
    Optional<ZoneLocalbodyBlockMapping> existing =
            zoneLocalbodyBlockMappingRepository.findByZoneAndIsValid(
                    request.getZoneId(), true);

    // 🔁 4. Soft delete old mapping (if exists)
    if (existing.isPresent()) {
      ZoneLocalbodyBlockMapping old = existing.get();

      old.setIsValid(false);
      old.setUpdatedAt(LocalDateTime.now());
      old.setUpdatedby(request.getUserId());

      zoneLocalbodyBlockMappingRepository.save(old);
    }

    // ✅ 5. Insert new mapping
    ZoneLocalbodyBlockMapping entity = new ZoneLocalbodyBlockMapping();

    entity.setZone(request.getZoneId());
    entity.setBlockPanchayatMunicipalArea(request.getTypeCode());
    entity.setBlockDetails(request.getReferenceId());
    entity.setCreatedAt(LocalDateTime.now());
    entity.setAddedby(request.getUserId());
    entity.setIsValid(true);

    zoneLocalbodyBlockMappingRepository.save(entity);
  }

  public List<MasterBlock> getBlocksByDistrict(Integer zoneId) {

    if (zoneId == null) {
      throw new RuntimeException("Zone ID is required");
    }
    Optional<TblMasterZone> zone = tblMasterZoneRepository.findById(zoneId);
    System.out.println("print   >>> "+masterBlockRepository.findByDistrictAndIsValidTrue(zone.get().getDistId()));
    return masterBlockRepository.findByDistrictAndIsValidTrue(zone.get().getDistId());
  }

  public List<TblLocalBody> getLocalBodies(Short typeId, int zonetId) {

    if (typeId == null) {
      throw new RuntimeException("Type and Zone are required");
    }
    Optional<TblMasterZone> zone = tblMasterZoneRepository.findById(zonetId);
    if (zone.isEmpty()){
      throw new IllegalArgumentException("Zone Id not Match");
    }
    return localBodyRepository
            .findByLocalbodyTypeAndDistIdAndIsActiveTrue(typeId, zone.get().getDistId());
  }
  @Transactional
  public void softDeleteBlockMapping(Long id) {

    ZoneLocalbodyBlockMapping mapping =
            zoneLocalbodyBlockMappingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Mapping not found"));

    // Already deleted check (optional)
    if (!mapping.getIsValid()) {
      return; // already deleted, no action
    }

    mapping.setIsValid(false);
    mapping.setUpdatedAt(LocalDateTime.now());

    zoneLocalbodyBlockMappingRepository.save(mapping);
  }

}