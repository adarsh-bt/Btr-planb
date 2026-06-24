package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.*;
import cdti.aidea.earas.config.Form1EditLogResponse;
import cdti.aidea.earas.config.FormEntryClient;
import cdti.aidea.earas.config.FormEntryClient;
import cdti.aidea.earas.contract.RequestsDTOs.*;
import cdti.aidea.earas.contract.RequestsDTOs.ClusterLimitRequest;
import cdti.aidea.earas.contract.Response.*;
import cdti.aidea.earas.contract.UserAccessDTOs.AssignedUserResponse;
import cdti.aidea.earas.contract.UserAccessDTOs.UserLoginDetailsResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNumeric;

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
    private final TblSeasonMasterRepository seasonMasterRepository;
    private final TblZoneSeasonScheduleRepository scheduleRepository ;
    private final UserAccessClientService userAccessClientService;
  private final ClusterEditAllowedRepository editAllowedRepository;
  private final TblMasterZoneRepository masterZoneRepository;
  private final ZoneLocalbodyBlockMappingRepository zoneLocalbodyBlockMappingRepository;
  private final MasterBlockRepository masterBlockRepository;
  private final LocalBodyRepository localBodyRepository;
  private final LocalBodyTypeRepository localBodyTypeRepository;
  private final TblWorkAllocationRepository tblWorkAllocationRepository;
  private final TblWorkAllocationApprovalRepository tblWorkAllocationApprovalRepository;
  private AgriYearUtil agriYearUtil;
  private final FormEntryClient formEntryClient;

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
                String districtName = district.map(DistrictMaster::getDistNameEn).orElse("Unknown District");

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
                        zone.getDistrictMaster().getDistId(),
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

  public List<ClusterApprovalTableDTO> zoneListForClusterss(String type, Integer idValue) {
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
        dto.setDistrictName(zone.getDistrictMaster().getDistNameEn());
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
            district != null ? district.getDistId() : null,
            district != null ? district.getDistNameEn() : null,
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
                      .map(DistrictMaster::getDistNameEn)
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
System.out.println("request "+ request);
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
        zone.setDistId(district.getDistId());
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
      zone.setDistId(district.getDistId());
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

    //To get zone assigned details after interconnection
    public List<ZoneUserAssignDto> getAllZonesWithUsers() {

        List<ZoneUserAssignDto> zones = tblMasterZoneRepository.findActiveZonesWithAssignment ();

        zones.forEach(zone -> {

            UUID loginId = zone.getAssignedUserLoginId();

            if (Boolean.TRUE.equals(zone.getIsAssigned()) && loginId != null) {

                UserLoginDetailsResponse userDetails =
                        userAccessClientService.getUserDetails(loginId);

                AssignedUserResponse assignedUser =
                        AssignedUserResponse.builder()
                                .userId(userDetails.getUserId())
                                .name(userDetails.getName())
                                .designationId(userDetails.getDesignationId())
                                .designation(userDetails.getDesignation())
                                .roleId(userDetails.getRoleId())
                                .roles(userDetails.getRoles())
                                .build();

                zone.setAssignedUserId(assignedUser);

            } else {
                zone.setAssignedUserId(null);
            }
        });

        return zones;
    }
//saveOrUpdate of DistrictMaster
    public String saveOrUpdateDistrict(DistrictRequestDTO dto) {

        // 🔹 Duplicate check by English Name
        Optional<DistrictMaster> duplicate =
                districtMasterRepository.findByDistNameEnIgnoreCase(dto.getDist_name_en());

        // ==================================================
        // UPDATE CASE (if ID exists)
        // ==================================================
        if (dto.getDist_id() != null) {

            DistrictMaster district = districtMasterRepository
                    .findById(Long.valueOf(dto.getDist_id()))
                    .orElseThrow(() -> new RuntimeException("District ID not found"));

            // Duplicate check excluding same ID
            if (duplicate.isPresent()
                    && !duplicate.get().getDistId().equals(dto.getDist_id())) {
                return "District name already exists";
            }

            district.setDistNameEn(dto.getDist_name_en());
            district.setDistNameMal(dto.getDist_name_mal());
            district.setDist_lsg_code(dto.getDist_lsg_code());
            district.setDist_code(dto.getDist_code());
            district.setCensus_code_2011(dto.getCensus_code_2011());
            district.setCensus_code_2001(dto.getCensus_code_2001());
            district.setDes_dist_code(dto.getDes_dist_code());
            district.setActive(dto.is_active());
            district.setUpdatedBy(dto.getAddedBy());
            district.setUpdatedAt(LocalDateTime.now());


            districtMasterRepository.save(district);

            return "District updated successfully";
        }

        // ==================================================
        // INSERT CASE (if ID not present)
        // ==================================================
        if (duplicate.isPresent()) {
            return "District name already exists";
        }

        DistrictMaster district = DistrictMaster.builder()
                .distNameEn(dto.getDist_name_en())
                .distNameMal(dto.getDist_name_mal())
                .des_dist_code(dto.getDes_dist_code())
                .dist_code(dto.getDist_code())
                .census_code_2011(dto.getCensus_code_2011())
                .census_code_2001(dto.getCensus_code_2001())
                .dist_lsg_code(dto.getDist_lsg_code())
                .isActive(dto.is_active())
                .addedBy(dto.getAddedBy())
                .createdAt(LocalDateTime.now())
                .build();

        districtMasterRepository.save(district);

        return "District added successfully";
    }
    //getAll Districts
    public List<DistrictResponse> getAllDistricts() {
        List<DistrictMaster> districts = districtMasterRepository.findAll();
        return districts.stream()
                .map(d -> DistrictResponse.builder()
                        .districtId(d.getDistId())
                        .districtNameEn(d.getDistNameEn())
                        .districtNameMal(d.getDistNameMal())
                        .build())
                .collect(Collectors.toList());
    }

    //saveOrUpdate of DesTalukMaster
    public String saveOrUpdate(DesTalukDTO dto) {

        // USER ID VALIDATION
        if (dto.getUserId() == null ) {
            return "User Id is required";
        }
        //DistId validation
        if (dto.getDistId() == null ) {
            return "Dist Id is required";
        }


        // IF ID COMES -> UPDATE
        if (dto.getDesTalukId() != null) {

            Optional<DesTaluk> existing =
                    desTalukRepository.findById(dto.getDesTalukId());

            if (existing.isPresent()) {

                // UPDATE
                DesTaluk taluk = existing.get();

                taluk.setDesTalukNameEn(dto.getDesTalukNameEn());
                taluk.setDesTalukNameMal(dto.getDesTalukNameMal());
                taluk.setDistId(dto.getDistId());
                taluk.setActive(dto.getIsActive());
                taluk.setUpdatedBy(dto.getUserId());
                taluk.setUpdatedAt(LocalDateTime.now());

                desTalukRepository.save(taluk);

                return "Updated Successfully";
            }
        }

        // SAVE NEW (AUTO INCREMENT ID FROM DB)
        DesTaluk taluk = new DesTaluk();

        taluk.setDesTalukNameEn(dto.getDesTalukNameEn());
        taluk.setDesTalukNameMal(dto.getDesTalukNameMal());
        taluk.setDistId(dto.getDistId());
        taluk.setActive(dto.getIsActive());
        taluk.setAddedBy(dto.getUserId());
       // taluk.setUpdatedBy(dto.getUpdatedBy());
        taluk.setCreatedAt(LocalDateTime.now());
        taluk.setUpdatedAt(LocalDateTime.now());

        desTalukRepository.save(taluk);

        return "Saved Successfully";
    }
    //getAll method of DesTalukMaster
    public List<DesTalukResponse> getAllDesTalukMaster() {

        List<DesTaluk> destaluk = desTalukRepository.findAll();

        return destaluk.stream()
                .map(taluk -> DesTalukResponse.builder()
                        .desTalukId(taluk.getDesTalukId())
                        .desTalukNameEn(taluk.getDesTalukNameEn())
                        .desTalukNameMal(taluk.getDesTalukNameMal())
                        .distId(taluk.getDistId())
                        .isActive(taluk.isActive())
                        .addedBy(taluk.getAddedBy())
                        .build())
                .toList();
    }


    // saveOrUpdate of RevTalukMaster
    public String saveOrUpdateRev(RevTalukDTO dto) {

        // USER ID VALIDATION
        if (dto.getUserId() == null) {
            return "User Id is required";
        }

        // UPDATE IF ID EXISTS
        if (dto.getRevTalukId() != null) {

            Optional<RevTaluk> existing =
                    revenueTalukRepository.findById(dto.getRevTalukId());

            if (existing.isPresent()) {

                RevTaluk taluk = existing.get();

                taluk.setRevTalukNameEn(dto.getRevTalukNameEn());
                taluk.setRevTalukNameMal(dto.getRevTalukNameMal());
                taluk.setDistId(dto.getDistId());
                taluk.setIsActive(dto.getIsActive());
                taluk.setLsgCode(dto.getLsgCode());
                taluk.setCensusCode2001(dto.getCensusCode2001());
                taluk.setCensusCode2011(dto.getCensusCode2011());
                taluk.setTalukCodeApi(dto.getTalukCodeApi());
                taluk.setUpdatedBy(dto.getUserId());
                taluk.setUpdatedAt(LocalDateTime.now());

                revenueTalukRepository.save(taluk);

                return "Updated Successfully";
            }
        }

        // SAVE NEW RECORD
        RevTaluk taluk = new RevTaluk();

        taluk.setRevTalukNameEn(dto.getRevTalukNameEn());
        taluk.setRevTalukNameMal(dto.getRevTalukNameMal());
        taluk.setDistId(dto.getDistId());
        taluk.setIsActive(dto.getIsActive());
        taluk.setLsgCode(dto.getLsgCode());
        taluk.setCensusCode2001(dto.getCensusCode2001());
        taluk.setCensusCode2011(dto.getCensusCode2011());
        taluk.setTalukCodeApi(dto.getTalukCodeApi());
        taluk.setCreatedBy(dto.getUserId());
       // taluk.setUpdatedBy(dto.getUserId());
        taluk.setCreatedAt(LocalDateTime.now());
        taluk.setUpdatedAt(LocalDateTime.now());

        revenueTalukRepository.save(taluk);

        return "Saved Successfully";
    }


    // getAll method of RevTalukMaster
    public List<RevTalukResponse> getAllRevTalukMaster() {

        List<RevTaluk> talukList = revenueTalukRepository.findAll();

        return talukList.stream()
                .map(taluk -> RevTalukResponse.builder()
                        .revTalukId(taluk.getRevTalukId())
                        .revTalukNameEn(taluk.getRevTalukNameEn())
                        .revTalukNameMal(taluk.getRevTalukNameMal())
                        .distId(taluk.getDistId())
                        .isActive(taluk.getIsActive())
                        .lsgCode(taluk.getLsgCode())
                        .censusCode2001(taluk.getCensusCode2001())
                        .censusCode2011(taluk.getCensusCode2011())
                        .talukCodeApi(taluk.getTalukCodeApi())
                        .createdBy(taluk.getCreatedBy())
                        .updatedBy(taluk.getUpdatedBy())
                        .build())
                .toList();
    }
    // saveOrUpdate of TblMasterVillage
    public String saveOrUpdateVillage(TblMasterVillageRequest dto) {

        // USER ID VALIDATION
        if (dto.getUserId() == null) {
            return "User Id is required";
        }

        // REQUIRED FIELD VALIDATION
        if (dto.getVillageNameEn() == null || dto.getVillageNameEn().trim().isEmpty()) {
            return "Village Name English is required";
        }

        if (dto.getVillageNameMal() == null || dto.getVillageNameMal().trim().isEmpty()) {
            return "Village Name Malayalam is required";
        }

        if (dto.getRevTalukId() == null) {
            return "Revenue Taluk Id is required";
        }

        // ✅ REVENUE TALUK ID EXISTENCE VALIDATION
        if (!revenueTalukRepository.existsById(Long.valueOf(dto.getRevTalukId()))) {
            return "Invalid Revenue Taluk Id";
        }

        // UPDATE IF ID EXISTS
        if (dto.getVillageId() != null) {

            Optional<TblMasterVillage> existing =
                    tblMasterVillageRepository.findById(dto.getVillageId());

            if (existing.isPresent()) {

                TblMasterVillage village = existing.get();

                village.setVillageNameEn(dto.getVillageNameEn());
                village.setVillageNameMal(dto.getVillageNameMal());
                village.setRevTalukId(dto.getRevTalukId());
                village.setVillageCodeApi(dto.getVillageCodeApi());
                village.setLsgCode(dto.getLsgCode());
                village.setCensusCode2001(dto.getCensusCode2001());
                village.setCensusCode2011(dto.getCensusCode2011());

                // update can be true / false
                village.setIsActive(dto.getIsActive());

                village.setUpdatedBy(dto.getUserId());
                village.setUpdatedAt(LocalDateTime.now());

                tblMasterVillageRepository.save(village);

                return "Updated Successfully";
            }
        }

        // SAVE NEW RECORD
        TblMasterVillage village = new TblMasterVillage();

        village.setVillageNameEn(dto.getVillageNameEn());
        village.setVillageNameMal(dto.getVillageNameMal());
        village.setRevTalukId(dto.getRevTalukId());
        village.setVillageCodeApi(dto.getVillageCodeApi());
        village.setLsgCode(dto.getLsgCode());
        village.setCensusCode2001(dto.getCensusCode2001());
        village.setCensusCode2011(dto.getCensusCode2011());

        // always true while save
        village.setIsActive(true);

        village.setAddedBy(dto.getUserId());
        // village.setUpdatedBy(dto.getUserId());
        village.setCreatedAt(LocalDateTime.now());
        village.setUpdatedAt(LocalDateTime.now());

        tblMasterVillageRepository.save(village);

        return "Saved Successfully";
    }

    // getAll method of TblMasterVillage
//    public List<TblMasterVillageRequest> getAllVillage() {
//
//        List<TblMasterVillage> list =
//                tblMasterVillageRepository.findAll();
//
//        return list.stream()
//                .map(data -> TblMasterVillageRequest.builder()
//                        .villageId(data.getVillageId())
//                        .villageNameEn(data.getVillageNameEn())
//                        .villageNameMal(data.getVillageNameMal())
//                        .revTalukId(data.getRevTalukId())
//                        .isActive(data.getIsActive())
//                        .villageCodeApi(data.getVillageCodeApi())
//                        .lsgCode(data.getLsgCode())
//                        .censusCode2001(data.getCensusCode2001())
//                        .censusCode2011(data.getCensusCode2011())
//                        .userId(data.getAddedBy())
//                        .build())
//                .toList();
//    }
    public List<TblMasterVillageRequest> getAllVillage(int page, String search) {

        Pageable pageable = PageRequest.of(page, 100, Sort.by("villageId").ascending());

        List<TblMasterVillage> list;

        if (search != null && !search.trim().isEmpty()) {
            list = tblMasterVillageRepository
                    .findByVillageNameEnContainingIgnoreCaseOrVillageCodeApiContainingIgnoreCase(
                            search, search, pageable)
                    .getContent();
        } else {
            list = tblMasterVillageRepository.findAll(pageable).getContent();
        }

        return list.stream()
                .map(data -> TblMasterVillageRequest.builder()
                        .villageId(data.getVillageId())
                        .villageNameEn(data.getVillageNameEn())
                        .villageNameMal(data.getVillageNameMal())
                        .revTalukId(data.getRevTalukId())
                        .isActive(data.getIsActive())
                        .villageCodeApi(data.getVillageCodeApi())
                        .lsgCode(data.getLsgCode())
                        .censusCode2001(data.getCensusCode2001())
                        .censusCode2011(data.getCensusCode2011())
                        .userId(data.getAddedBy())
                        .build())
                .toList();
    }

    // saveOrUpdate of TblMasterVillageBlock
    public String saveOrUpdateVillageBlock(TblMasterVillageBlockRequest dto) {

        // USER ID VALIDATION
        if (dto.getUserId() == null) {
            return "User Id is required";
        }

        // REQUIRED FIELD VALIDATION
        if (dto.getBlockCode() == null || dto.getBlockCode().trim().isEmpty()) {
            return "Block Code is required";
        }

        if (dto.getVillageId() == null) {
            return "Village Id is required";
        }

        // CHECK VILLAGE EXISTS (tbl_master_village PK)
        boolean villageExists =
                tblMasterVillageRepository.existsById(dto.getVillageId());

        if (!villageExists) {
            return "Invalid Village Id";
        }

        // UPDATE IF ID EXISTS
        if (dto.getVillageBlockId() != null) {

            Optional<TblMasterVillageBlock> existing =
                    tblMasterVillageBlockRepository.findById(dto.getVillageBlockId());

            if (existing.isPresent()) {

                TblMasterVillageBlock block = existing.get();

                block.setBlockCode(dto.getBlockCode());
                block.setVillageId(dto.getVillageId());
                block.setUpdatedBy(dto.getUserId());
                block.setUpdatedAt(LocalDateTime.now());

                // update active status from request
                block.setIsActive(dto.getIsActive());

                tblMasterVillageBlockRepository.save(block);

                return "Updated Successfully";
            }
        }

        // SAVE NEW RECORD
        TblMasterVillageBlock block = new TblMasterVillageBlock();

        block.setBlockCode(dto.getBlockCode());
        block.setVillageId(dto.getVillageId());
        block.setAddedBy(dto.getUserId());
       // block.setUpdatedBy(dto.getUserId());
        block.setCreatedAt(LocalDateTime.now());
        block.setUpdatedAt(LocalDateTime.now());

        // active true while save
        block.setIsActive(true);

        tblMasterVillageBlockRepository.save(block);

        return "Saved Successfully";
    }

    //getallVillageBlock
//    public List<TblMasterVillageBlockRequest> getAllVillageBlocks() {
//
//        List<TblMasterVillageBlock> list =
//                tblMasterVillageBlockRepository.findAll();
//
//        return list.stream()
//                .map(data -> TblMasterVillageBlockRequest.builder()
//                        .villageBlockId(data.getVillageBlockId())
//                        .blockCode(data.getBlockCode())
//                        .villageId(data.getVillageId())
//                        .isActive(data.getIsActive())
//                        .userId(data.getAddedBy()) // optional
//                        .build())
//                .toList();
//    }
//
    public List<TblMasterVillageBlockRequest> getAllVillageBlocks(int page, String search) {

        Pageable pageable = PageRequest.of(page, 100, Sort.by("villageBlockId").ascending());

        List<TblMasterVillageBlock> list;

        if (search != null && !search.trim().isEmpty()) {
            list = tblMasterVillageBlockRepository
                    .findByBlockCodeContainingIgnoreCaseOrVillageId(
                            search,
                            isNumeric(search) ? Long.parseLong(search) : -1L,
                            pageable)
                    .getContent();
        } else {
            list = tblMasterVillageBlockRepository.findAll(pageable).getContent();
        }

        return list.stream()
                .map(data -> TblMasterVillageBlockRequest.builder()
                        .villageBlockId(data.getVillageBlockId())
                        .blockCode(data.getBlockCode())
                        .villageId(data.getVillageId())
                        .isActive(data.getIsActive())
                        .userId(data.getAddedBy())
                        .build())
                .toList();
    }

    //saveOrUpdate TblMasterZone
    public String saveOrUpdateZone(MasterZoneRequest dto) {

        // =========================
        // USER VALIDATION
        // =========================
        if (dto.getUserId() == null) {
            return "User Id is required";
        }

        // =========================
        // REQUIRED FIELDS
        // =========================
        if (dto.getZoneNameEn() == null || dto.getZoneNameEn().trim().isEmpty()) {
            return "Zone Name English is required";
        }

        if (dto.getDesTalukId() == null) {
            return "Des Taluk Id is required";
        }

        if (dto.getDistId() == null) {
            return "District Id is required";
        }

        if (dto.getBtrTypeId() == null) {
            return "BTR Type Id is required";
        }

        // =========================
        // FOREIGN KEY VALIDATIONS
        // =========================

        // 🔹 Check DesTaluk
        boolean talukExists =
                desTalukRepository.existsById(dto.getDesTalukId());

        if (!talukExists) {
            return "Invalid Des Taluk Id";
        }

        // 🔹 Check District
        boolean districtExists =
                districtMasterRepository.existsById(Long.valueOf(dto.getDistId()));

        if (!districtExists) {
            return "Invalid District Id";
        }

        // 🔹 Check BTR Type
        Optional<TblBtrType> btrType =
                tblBtrTypeRepository.findById(dto.getBtrTypeId());

        if (btrType.isEmpty()) {
            return "Invalid BTR Type Id";
        }

        // =========================
        // UPDATE CASE
        // =========================
        if (dto.getZoneId() != null) {

            Optional<TblMasterZone> existing =
                    tblMasterZoneRepository.findById(dto.getZoneId());

            if (existing.isPresent()) {

                TblMasterZone zone = existing.get();

                zone.setZoneCode(dto.getZoneCode());
                zone.setZoneNameEn(dto.getZoneNameEn());
                zone.setZoneNameMal(dto.getZoneNameMal());
                zone.setDesTalukId(dto.getDesTalukId());
                zone.setDesDistId(dto.getDesDistId());
                zone.setDistId(dto.getDistId());
                zone.setZoneUser(dto.getZoneUser());

                // update active from request
                zone.setIsActive(dto.getIsActive());

                zone.setBtrType(btrType.get());
                zone.setUpdatedBy(dto.getUserId());
                zone.setUpdatedAt(LocalDateTime.now());

                tblMasterZoneRepository.save(zone);

                return "Updated Successfully";
            }
        }

        // =========================
        // SAVE NEW
        // =========================
        TblMasterZone zone = new TblMasterZone();

        zone.setZoneCode(dto.getZoneCode());
        zone.setZoneNameEn(dto.getZoneNameEn());
        zone.setZoneNameMal(dto.getZoneNameMal());
        zone.setDesTalukId(dto.getDesTalukId());
        zone.setDesDistId(dto.getDesDistId());
        zone.setDistId(dto.getDistId());
        zone.setZoneUser(dto.getZoneUser());

        // always true while saving
        zone.setIsActive(true);

        zone.setBtrType(btrType.get());
        zone.setAddedBy(dto.getUserId());
       // zone.setUpdatedBy(dto.getUserId());
        zone.setCreatedAt(LocalDateTime.now());
        zone.setUpdatedAt(LocalDateTime.now());

        tblMasterZoneRepository.save(zone);

        return "Saved Successfully";
    }

    //getAll TblMasterZone
//    public List<MasterZoneRequest> getAllZones() {
//
//        List<TblMasterZone> list = tblMasterZoneRepository.findAll();
//
//        return list.stream()
//                .map(data -> MasterZoneRequest.builder()
//                        .zoneId(data.getZoneId())
//                        .zoneCode(data.getZoneCode())
//                        .zoneNameEn(data.getZoneNameEn())
//                        .zoneNameMal(data.getZoneNameMal())
//                        .desTalukId(data.getDesTalukId())
//                        .desDistId(data.getDesDistId())
//                        .distId(data.getDistId())
//                        .btrTypeId(
//                                data.getBtrType() != null
//                                        ? data.getBtrType().getBtrTypeId()
//                                        : null
//                        )
//                        .zoneUser(data.getZoneUser())
//                        .isActive(data.getIsActive())
//                        .userId(data.getAddedBy())
//                        .build())
//                .toList();
//    }
    public List<MasterZoneRequest> getAllZones(int page, String search) {

        Pageable pageable = PageRequest.of(page, 100, Sort.by("zoneId").ascending());

        List<TblMasterZone> list;

        if (search != null && !search.trim().isEmpty()) {
            list = tblMasterZoneRepository
                    .findByZoneNameEnContainingIgnoreCaseOrZoneCodeContainingIgnoreCase(
                            search, search, pageable)
                    .getContent();
        } else {
            list = tblMasterZoneRepository.findAll(pageable).getContent();
        }

        return list.stream()
                .map(data -> MasterZoneRequest.builder()
                        .zoneId(data.getZoneId())
                        .zoneCode(data.getZoneCode())
                        .zoneNameEn(data.getZoneNameEn())
                        .zoneNameMal(data.getZoneNameMal())
                        .desTalukId(data.getDesTalukId())
                        .desDistId(data.getDesDistId())
                        .distId(data.getDistId())
                        .btrTypeId(
                                data.getBtrType() != null
                                        ? data.getBtrType().getBtrTypeId()
                                        : null
                        )
                        .zoneUser(data.getZoneUser())
                        .isActive(data.getIsActive())
                        .userId(data.getAddedBy())
                        .build())
                .toList();
    }

    //saveOrUpdate TblLocalBody
    public String saveOrUpdateLocalBody(TblLocalBodyRequest dto) {

        // =========================
        // USER VALIDATION
        // =========================
        if (dto.getUserId() == null) {
            return "User Id is required";
        }

        // =========================
        // REQUIRED FIELDS
        // =========================
        if (dto.getLocalbodyNameEn() == null || dto.getLocalbodyNameEn().trim().isEmpty()) {
            return "Local Body Name English is required";
        }

        if (dto.getDistId() == null) {
            return "District Id is required";
        }
        if (dto.getLocalbodyType() == null) {
            return "Local Body Type is required";
        }

        boolean typeExists =
                localBodyTypeRepository.existsById(Long.valueOf(dto.getLocalbodyType()));

        if (!typeExists) {
            return "Invalid Local Body Type";
        }

        // =========================
        // UPDATE CASE
        // =========================
        if (dto.getLocalbodyId() != null) {

            Optional<TblLocalBody> existing =
                    localBodyRepository.findById(dto.getLocalbodyId());

            if (existing.isPresent()) {

                TblLocalBody entity = existing.get();

                entity.setLocalbodyCode(dto.getLocalbodyCode());
                entity.setDistId(dto.getDistId());
                entity.setLocalbodyNameEn(dto.getLocalbodyNameEn());
                entity.setLocalbodyNameMal(dto.getLocalbodyNameMal());
                entity.setLocalbodyType(dto.getLocalbodyType());
                entity.setCodeApi(dto.getCodeApi());
                entity.setLsgCode(dto.getLsgCode());

                // update active from request
               // entity.setIsActive(dto.getIsActive());
                entity.setIsActive(
                        dto.getIsActive() != null ? dto.getIsActive() : entity.getIsActive()
                );
                entity.setUpdatedBy(dto.getUserId());
                entity.setUpdatedAt(LocalDateTime.now());

                localBodyRepository.save(entity);

                return "Updated Successfully";
            }
        }

        // =========================
        // SAVE NEW
        // =========================
        TblLocalBody entity = new TblLocalBody();

       // entity.setLocalbodyId(dto.getLocalbodyId()); // must be provided OR change to auto-gen
        entity.setLocalbodyCode(dto.getLocalbodyCode());
        entity.setDistId(dto.getDistId());
        entity.setLocalbodyNameEn(dto.getLocalbodyNameEn());
        entity.setLocalbodyNameMal(dto.getLocalbodyNameMal());
        entity.setLocalbodyType(dto.getLocalbodyType());
        entity.setCodeApi(dto.getCodeApi());
        entity.setLsgCode(dto.getLsgCode());

        // always true while save
        entity.setIsActive(true);

        entity.setAddedBy(dto.getUserId());
        //entity.setUpdatedBy(dto.getUserId());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        localBodyRepository.save(entity);

        return "Saved Successfully";
    }

    //getAll of TblMasterLocalBody
//    public List<TblLocalBodyRequest> getAllLocalBodies() {
//
//        List<TblLocalBody> list = localBodyRepository.findAll();
//
//        return list.stream()
//                .map(data -> TblLocalBodyRequest.builder()
//                        .localbodyId(data.getLocalbodyId())
//                        .localbodyCode(data.getLocalbodyCode())
//                        .distId(data.getDistId())
//                        .localbodyNameEn(data.getLocalbodyNameEn())
//                        .localbodyNameMal(data.getLocalbodyNameMal())
//                        .localbodyType(data.getLocalbodyType())
//                        .codeApi(data.getCodeApi())
//                        .lsgCode(data.getLsgCode())
//                        .isActive(data.getIsActive())
//                        .userId(data.getAddedBy()) // or updatedBy if needed
//                        .build())
//                .toList();
//    }
    public List<TblLocalBodyRequest> getAllLocalBodies(int page, String search) {

        Pageable pageable = PageRequest.of(page, 100, Sort.by("localbodyId").ascending());

        List<TblLocalBody> list;

        if (search != null && !search.trim().isEmpty()) {
            list = localBodyRepository
                    .findByLocalbodyNameEnContainingIgnoreCaseOrLocalbodyCodeContainingIgnoreCase(
                            search, search, pageable)
                    .getContent();
        } else {
            list = localBodyRepository.findAll(pageable).getContent();
        }

        return list.stream()
                .map(data -> TblLocalBodyRequest.builder()
                        .localbodyId(data.getLocalbodyId())
                        .localbodyCode(data.getLocalbodyCode())
                        .distId(data.getDistId())
                        .localbodyNameEn(data.getLocalbodyNameEn())
                        .localbodyNameMal(data.getLocalbodyNameMal())
                        .localbodyType(data.getLocalbodyType())
                        .codeApi(data.getCodeApi())
                        .lsgCode(data.getLsgCode())
                        .isActive(data.getIsActive())
                        .userId(data.getAddedBy())
                        .build())
                .toList();
    }
    //saveOrUpdate MasterBlock
    public String saveOrUpdateBlock(MasterBlockRequest dto) {

        // =========================
        // USER VALIDATION
        // =========================
        if (dto.getUserId() == null) {
            return "User Id is required";
        }

        // =========================
        // REQUIRED FIELDS
        // =========================
        if (dto.getBlockName() == null || dto.getBlockName().trim().isEmpty()) {
            return "Block Name is required";
        }

        if (dto.getDistrict() == null) {
            return "District is required";
        }

        // =========================
        // UPDATE CASE
        // =========================
        if (dto.getBlockId() != null) {

            Optional<MasterBlock> existing =
                    masterBlockRepository.findById(dto.getBlockId());

            if (existing.isPresent()) {

                MasterBlock block = existing.get();

                block.setBlockCode(dto.getBlockCode());
                block.setBlockName(dto.getBlockName());
                block.setDistrict(dto.getDistrict());
                block.setLsgCode(dto.getLsgCode());

                // update value from request
                block.setValid(dto.isValid());

                block.setUpdatedBy(dto.getUserId());
                block.setUpdatedAt(LocalDateTime.now());

                masterBlockRepository.save(block);

                return "Updated Successfully";
            }
        }

        // =========================
        // SAVE NEW
        // =========================
        MasterBlock block = new MasterBlock();

        block.setBlockCode(dto.getBlockCode());
        block.setBlockName(dto.getBlockName());
        block.setDistrict(dto.getDistrict());
        block.setLsgCode(dto.getLsgCode());

        // default true while saving
        block.setValid(true);

        block.setAddedBy(dto.getUserId());
      //  block.setUpdatedBy(dto.getUserId());
        block.setCreatedAt(LocalDateTime.now());
        block.setUpdatedAt(LocalDateTime.now());

        masterBlockRepository.save(block);

        return "Saved Successfully";
    }

    //getAll masterBlock
//    public List<MasterBlockRequest> getAllBlocks() {
//
//        List<MasterBlock> list = masterBlockRepository.findAll();
//
//        return list.stream()
//                .map(data -> MasterBlockRequest.builder()
//                        .blockId(data.getBlockId())
//                        .blockCode(data.getBlockCode())
//                        .blockName(data.getBlockName())
//                        .district(data.getDistrict())
//                        .lsgCode(data.getLsgCode())
//                        .isValid(data.isValid())
//                        .userId(data.getAddedBy()) // optional
//                        .build())
//                .toList();
//    }
    public List<MasterBlockRequest> getAllBlocks(int page,String search) {

        Pageable pageable = PageRequest.of(page, 100, Sort.by("blockId").ascending());

        List<MasterBlock> list;

        if (search != null && !search.trim().isEmpty()) {
            list = masterBlockRepository
                    .findByBlockNameContainingIgnoreCaseOrBlockCodeContainingIgnoreCase(
                            search, search, pageable)
                    .getContent();
        } else {
            list = masterBlockRepository.findAll(pageable).getContent();
        }

        return list.stream()
                .map(data -> MasterBlockRequest.builder()
                        .blockId(data.getBlockId())
                        .blockCode(data.getBlockCode())
                        .blockName(data.getBlockName())
                        .district(data.getDistrict())
                        .lsgCode(data.getLsgCode())
                        .isValid(data.isValid())
                        .userId(data.getAddedBy())
                        .build())
                .toList();
    }
  public List<Form1ZoneListResponse> getZonesByUserType(String type, Integer idValue) {

    List<TblMasterZone> zones;
    if ("Taluk".equalsIgnoreCase(type)) {

      zones = tblMasterZoneRepository.findByDesTalukId(idValue);

    } else if ("District".equalsIgnoreCase(type)) {

      zones = tblMasterZoneRepository.findByDistId(idValue);

    } else if ("Directorate".equalsIgnoreCase(type)) {

      zones = tblMasterZoneRepository.findAll();

    } else {

      throw new IllegalArgumentException(
              "Invalid type. Use 'Taluk', 'District', or 'Directorate'.");
    }

    if (zones == null || zones.isEmpty()) {
      throw new IllegalArgumentException("No zones found for the given ID.");
    }

    return zones.stream()
            .map(zone -> {

              Optional<DesTaluk> taluk =
                      desTalukRepository.findById(zone.getDesTalukId());

              String talukName =
                      taluk.map(DesTaluk::getDesTalukNameEn)
                              .orElse("Unknown Taluk");

              Optional<DistrictMaster> district =
                      districtMasterRepository.findById(
                              Long.valueOf(zone.getDistId()));

              String districtName =
                      district.map(DistrictMaster::getDistNameEn)
                              .orElse("Unknown District");

              return new Form1ZoneListResponse(
                      zone.getZoneId(),
                      zone.getZoneCode(),
                      zone.getZoneNameEn(),
                      zone.getZoneNameMal(),
                      zone.getBtrType().getBtrType(),
                      zone.getDesTalukId(),
                      zone.getDistrictMaster().getDistId(),
                      talukName,
                      districtName
              );
            })
            .sorted(
                    Comparator.comparing(Form1ZoneListResponse::getDesDistId)
                            .thenComparing(Form1ZoneListResponse::getDesTalukId)
            )
            .collect(Collectors.toList());
  }
//    List<Long> zoneIds =
//            zones.stream()
//                    .map(zone -> Long.valueOf(zone.getZoneId()))
//                    .collect(Collectors.toList());
//
//    ResponseEntity<Page<Form1EditLogResponse>> response =
//            formEntryClient.getEditStatusByZoneIds(
//                    zoneIds,
//                    0,
//                    100);
//
//    Page<Form1EditLogResponse> formResponse =
//            (Page<Form1EditLogResponse>) response.getBody();
//
//    Map<Long, Form1EditLogResponse> statusMap =
//            formResponse.getContent()
//                    .stream()
//                    .collect(
//                            Collectors.toMap(
//                                    Form1EditLogResponse::getZoneId,
//                                    data -> data,
//                                    (a, b) -> a));
//
//    return zones.stream()
//            .map(
//                    zone -> {
//
//                      Optional<DesTaluk> taluk =
//                              desTalukRepository.findById(
//                                      zone.getDesTalukId());
//
//                      String talukName =
//                              taluk.map(DesTaluk::getDesTalukNameEn)
//                                      .orElse("Unknown Taluk");
//
//                      Optional<DistrictMaster> district =
//                              districtMasterRepository.findById(
//                                      Long.valueOf(zone.getDistId()));
//
//                      String districtName =
//                              district.map(DistrictMaster::getDist_name_en)
//                                      .orElse("Unknown District");
//
//                      Form1EditLogResponse formStatus =
//                              statusMap.get(
//                                      Long.valueOf(zone.getZoneId()));
//
//                      return new Form1ZoneListResponse(
//                              zone.getZoneId(),
//                              zone.getZoneCode(),
//                              zone.getZoneNameEn(),
//                              zone.getZoneNameMal(),
//                              zone.getBtrType().getBtrType(),
//                              zone.getDesTalukId(),
//                              zone.getDistrictMaster().getDist_id(),
//                              talukName,
//                              districtName);
//                    })
//            .sorted(
//                    Comparator.comparing(
//                                    Form1ZoneListResponse::getDesDistId)
//                            .thenComparing(
//                                    Form1ZoneListResponse::getDesTalukId))
//            .collect(Collectors.toList());
//  }

}