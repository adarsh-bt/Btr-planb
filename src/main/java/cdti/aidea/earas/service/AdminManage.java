package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.RequestsDTOs.ClusterApprovalActionDTO;
import cdti.aidea.earas.contract.RequestsDTOs.ClusterLimitRequest;
import cdti.aidea.earas.contract.RequestsDTOs.KeyPlotDetailsRequest;
import cdti.aidea.earas.contract.RequestsDTOs.KeyplotsLimitLogRequest;
import cdti.aidea.earas.contract.Response.*;
import cdti.aidea.earas.model.Btr_models.*;
import cdti.aidea.earas.model.Btr_models.Masters.DesTaluk;
import cdti.aidea.earas.model.Btr_models.Masters.DistrictMaster;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import cdti.aidea.earas.repository.Btr_repo.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
public class AdminManage {

  private final KeyplotsLimitLogRepository repository;

  private final ClusterLimitLogRepository clusterLimitLogRepository;

  private final TblMasterZoneRepository tblMasterZoneRepository;
  private final DesTalukRepository desTalukRepository;
  private final DistrictMasterRepository districtMasterRepository;
  private final ClusterApprovalLogRepository clusterApprovalLogRepository;
  private final ClusterMasterRepository clusterMasterRepository;

  private final UserZoneAssignmentRepositoty userZoneAssignmentRepositoty;

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
                Optional<DesTaluk> taluk = desTalukRepository.findById(zone.getDesTalukId());
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
                        zone.getDesTalukId(),
                        zone.getDesDistId(),
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
    System.out.println("type "+type+" : "+idValue);
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
    }else{
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
            .findById(zone.getDesTalukId())
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
        throw new RuntimeException("A record already exists for the current agri year: " + agriStart +" To "+agriEnd);
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
                      zone.getDesDistId(),
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

}