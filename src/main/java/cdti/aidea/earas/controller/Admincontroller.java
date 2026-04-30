package cdti.aidea.earas.controller;

import cdti.aidea.earas.contract.*;
import cdti.aidea.earas.contract.RequestsDTOs.*;
import cdti.aidea.earas.contract.Response.*;
import cdti.aidea.earas.common.exception.Response;
import cdti.aidea.earas.contract.RequestsDTOs.ClusterApprovalActionDTO;
import cdti.aidea.earas.contract.RequestsDTOs.ClusterLimitRequest;
import cdti.aidea.earas.contract.RequestsDTOs.KeyplotsLimitLogRequest;
import cdti.aidea.earas.contract.RequestsDTOs.ZoneUserAssignDto;
import cdti.aidea.earas.contract.Response.ClusterApprovalTableDTO;
import cdti.aidea.earas.contract.Response.KeyplotsLimitLogResponse;
import cdti.aidea.earas.contract.Response.ZoneListResponse;
//import cdti.aidea.earas.contract.UserAccessDTOs.ZoneDetailsWithUserAccessResponse;
import cdti.aidea.earas.model.Btr_models.ClusterLimitLog;
import cdti.aidea.earas.model.Btr_models.KeyplotsLimitLog;
import cdti.aidea.earas.model.Btr_models.Masters.*;
import cdti.aidea.earas.repository.Btr_repo.TblZoneRevenueVillageMappingRepository;
import cdti.aidea.earas.repository.Btr_repo.TblZoneVillageBlockMappingRepository;
import cdti.aidea.earas.repository.Btr_repo.ZoneRevenueTalukMappingRepository;
import cdti.aidea.earas.service.AdminManage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/admin-manage")
@RequiredArgsConstructor
@Slf4j
public class Admincontroller {

  private final AdminManage adminManage;
  private final TblZoneVillageBlockMappingRepository tblZoneVillageBlockMappingRepository;
  private final TblZoneRevenueVillageMappingRepository tblZoneRevenueVillageMappingRepository;
  private final ZoneRevenueTalukMappingRepository zoneRevenueTalukMappingRepository;

  @GetMapping("/keyplot-limits")
  public List<KeyplotsLimitLogResponse> getAllKeyplotLimits() {
    return adminManage.getAllKeyplots();
  }

  @PostMapping("/save-keyplot-limits")
  public ResponseEntity<KeyplotsLimitLog> createKeyplotsLimit(
      @RequestBody KeyplotsLimitLogRequest request) {
    KeyplotsLimitLog saved = adminManage.saveOrUpdateKeyplotsLimit(request);
    return ResponseEntity.ok(saved);
  }

  @PostMapping("/save-cluster-limits")
  public ResponseEntity<ClusterLimitLog> createOrUpdateClusterLimit(
      @RequestBody ClusterLimitRequest request) {
    ClusterLimitLog saved = adminManage.saveOrUpdateClusterLimit(request);
    return ResponseEntity.ok(saved);
  }

  @GetMapping("/cluster-limits")
  public List<ClusterLimitRequest> getAllClusterLimits() {
    return adminManage.getAllClusterLimits();
  }

  @GetMapping("/zones/{type}/{id}")
  public ResponseEntity<List<ZoneListResponse>> getById(
      @PathVariable("type") String type, @PathVariable("id") String id) {
    try {
      Integer idValue = Integer.parseInt(id); // Parse the ID

      // Call the unified service method
      List<ZoneListResponse> zoneList = adminManage.AdminViewZonesByType(type, idValue);
      return new ResponseEntity<>(zoneList, HttpStatus.OK);
    } catch (NumberFormatException e) {
      // You can still return an error response if the ID is invalid
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (IllegalArgumentException e) {
      // Optional: log or return a specific error message
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/zones_cluster/{type}/{id}")
  public ResponseEntity<List<ClusterApprovalTableDTO>> ZonelistClusters(
          @PathVariable("type") String type, @PathVariable("id") String id) {
    try {
      Integer idValue = Integer.parseInt(id); // Parse the ID
System.out.println("id  "+idValue+"  : "+type);
      // Call the unified service method
      List<ClusterApprovalTableDTO> zoneList = adminManage.zoneListForClusters(type, idValue);
      return new ResponseEntity<>(zoneList, HttpStatus.OK);
    } catch (NumberFormatException e) {
      // You can still return an error response if the ID is invalid
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (IllegalArgumentException e) {
      // Optional: log or return a specific error message
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/approve-reject")
  public ResponseEntity<?> approveOrReject(
          @Valid @RequestBody ClusterApprovalActionDTO request) {
    return ResponseEntity.ok(
            adminManage.clusterApprovals(request));
  }

  @GetMapping("/GetZones")
  public ZonePageResponse getZones(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "20") int size,
          @RequestParam(required = false) String search) {
    return adminManage.getAllZones(page, size, search);
  }

  @GetMapping("/zone-mapping-details/{zoneId}")
  public ResponseEntity<ZoneMappingResponseDTO> getZoneDetails(
          @PathVariable Integer zoneId) {

    ZoneMappingResponseDTO result = adminManage.getZoneDetails(zoneId);
    return ResponseEntity.ok(result);
  }


  @GetMapping("/taluks")
  public List<TalukDTO> getTaluks(@RequestParam Integer zoneId) {

    return adminManage.getTaluksByZone(zoneId);
  }


  @GetMapping("/villages")
  public List<VillageDTO> getVillages(@RequestParam Integer talukId) {

    return adminManage.getVillages(talukId);
  }


  @GetMapping("/blocks")
  public List<BlockDTO> getBlocks(@RequestParam Integer villageId) {

    return adminManage.getBlocksByVillage(villageId);
  }

  @PutMapping("/remove-village/{id}")
  @Transactional
  public ResponseEntity<?> removeVillage(@PathVariable Long id,
                                         @RequestParam UUID userid) {

    TblZoneRevenueVillageMapping mapping =
            tblZoneRevenueVillageMappingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Village mapping not found"));

    // 🔹 Soft delete village
    mapping.setIsValid(false);
    mapping.setUpdatedBy(userid);
    mapping.setUpdatedAt(LocalDateTime.now());

    tblZoneRevenueVillageMappingRepository.save(mapping);

    // 🔹 🚀 FAST: bulk update (no loop)
    tblZoneVillageBlockMappingRepository
            .softDeleteBlocksByVillage(
                    mapping.getZone(),
                    mapping.getRevenueVillage(),
                    userid
            );

    return ResponseEntity.ok("Village + blocks removed");
  }

  @PutMapping("/remove-block/{id}")
  public ResponseEntity<?> removeBlock(@PathVariable Long id,@RequestParam UUID userid) {

    TblZoneVillageBlockMapping mapping =
            tblZoneVillageBlockMappingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Village mapping not found"));

    mapping.setIsValid(false);
    mapping.setUpdatedBy(userid);
    mapping.setUpdatedAt(LocalDateTime.now());
    tblZoneVillageBlockMappingRepository.save(mapping);

    return ResponseEntity.ok("Block removed");
  }

  @Transactional
  @PutMapping("/remove-taluk/{id}")
  public ResponseEntity<?> removeTaluk(@PathVariable Long id,
                                       @RequestParam UUID userid) {

    ZoneRevenueTalukMapping taluk =
            zoneRevenueTalukMappingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Taluk not found"));

    taluk.setIsValid(false);
    taluk.setUpdatedBy(userid);
    taluk.setUpdatedAt(LocalDateTime.now());
    zoneRevenueTalukMappingRepository.save(taluk);

    List<TblZoneRevenueVillageMapping> villages =
            tblZoneRevenueVillageMappingRepository
                    .findVillagesByZoneAndTaluk(
                            taluk.getZone(),
                            taluk.getRevenueTaluk()
                    );

    for (TblZoneRevenueVillageMapping village : villages) {

      village.setIsValid(false);
      village.setUpdatedBy(userid);
      village.setUpdatedAt(LocalDateTime.now());
      List<TblZoneVillageBlockMapping> blocks =
              tblZoneVillageBlockMappingRepository
                      .findByZoneAndVillageIdAndIsValidTrue(
                              taluk.getZone(),
                              village.getRevenueVillage()
                      );

      for (TblZoneVillageBlockMapping block : blocks) {
        block.setIsValid(false);
        block.setUpdatedBy(userid);
        block.setUpdatedAt(LocalDateTime.now());
      }
      tblZoneVillageBlockMappingRepository.saveAll(blocks);
    }
    tblZoneRevenueVillageMappingRepository.saveAll(villages);
    return ResponseEntity.ok("Taluk + villages + blocks removed");
  }

  @PostMapping("/add-zone-mapping")
  public ResponseEntity<?> addZoneMapping(@RequestBody AddZoneMappingRequest request) {

    adminManage.saveZoneMapping(request);

    return ResponseEntity.ok("Mapping saved successfully");
  }

  @PostMapping("/save-or-update")
  public ResponseEntity<TblMasterZone> saveOrUpdate(@RequestBody ZoneCreateRequest request) {
    TblMasterZone response = adminManage.saveOrUpdate(request);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/zone-localbodies/{zoneId}")
  public ResponseEntity<?> getLocalBodies(@PathVariable Integer zoneId) {

    List<LocalbodyDTO> data = adminManage.getZoneLocalBodies(zoneId);

    return ResponseEntity.ok(data);
  }

  @GetMapping("/available-localbodies/{zoneId}")
  public ResponseEntity<?> getAvailableLocalBodies(@PathVariable Integer zoneId) {

    return ResponseEntity.ok(
            adminManage.getAvailableLocalBodies(zoneId)
    );
  }

  @PostMapping("/add-localbody")
  public ResponseEntity<?> addLocalBody(
          @RequestBody LocalbodyDTO request) {
    adminManage.saveLocalBodyMapping(request);
    return ResponseEntity.ok("LocalBody mapped successfully");
  }

  @PutMapping("/remove-localbody/{id}")
  public ResponseEntity<?> removeLocalBody(@PathVariable Integer id,
                                           @RequestParam UUID userId) {
    adminManage.removeLocalBodyMapping(id, userId);
    return ResponseEntity.ok("LocalBody removed successfully");
  }

  @PostMapping("/edit-allow-cluster")
  public ResponseEntity<String> create(@RequestBody ClusterEditRequestDTO dto){
    adminManage.createClusterEditRequest(dto);
    return ResponseEntity.ok("Cluster edit request processed successfully");
  }

  @GetMapping("/zones/{zoneId}/block-mappings")
  public ResponseEntity<?> getBlockMappings(@PathVariable Integer zoneId) {

    List<ZoneBlockMappingResponseDto> data =
            adminManage.getBlockMappings(zoneId);

    return ResponseEntity.ok(data);
  }

  @PostMapping("/zones/block-mapping")
  public ResponseEntity<?> saveOrUpdateBlockMapping(
          @RequestBody ZoneBlockMappingRequestDto request) {

    adminManage.saveOrUpdateBlockMapping(request);

    return ResponseEntity.ok("Mapping saved/updated successfully");
  }

  @GetMapping("/local-blocks")
  public ResponseEntity<?> getBlocksByDistrict(
          @RequestParam Integer zoneId) {

    List<MasterBlock> blocks =
            adminManage.getBlocksByDistrict(zoneId);

    return ResponseEntity.ok(blocks);
  }

  @GetMapping("/local-bodies")
  public ResponseEntity<?> getLocalBodies(
          @RequestParam Short typeId,
          @RequestParam int zoneId) {

    List<TblLocalBody> data =
            adminManage.getLocalBodies(typeId, zoneId);

    return ResponseEntity.ok(data);
  }

  @DeleteMapping("/zones/block-mapping/{id}")
  public ResponseEntity<?> deleteBlockMapping(@PathVariable Long id) {

    adminManage.softDeleteBlockMapping(id);

    return ResponseEntity.ok("Mapping deleted successfully");
  }

//    @PostMapping("/edit-allow-cluster")
//    public ResponseEntity<String> create(@RequestBody ClusterEditRequestDTO dto) {
//        adminManage.createClusterEditRequest(dto);
//        return ResponseEntity.ok("Cluster edit request processed successfully");
//    }

    //saveorupdate new district
    @PostMapping("/saveDistrict")
    public ResponseEntity<String> saveDistrict(@RequestBody DistrictRequestDTO dto) {
        return ResponseEntity.ok(adminManage.saveOrUpdateDistrict(dto));
    }

    //saveOrUpdate in DesTaluk
    @PostMapping("/saveOrUpdate")
    public String saveOrUpdate(@RequestBody DesTalukDTO dto) {
        return adminManage.saveOrUpdate(dto);
    }

    //DesTalukMaster getAll
    @GetMapping("/getAllTaluk")
    public ResponseEntity<List<DesTalukResponse>> getAllDesTalukMaster() {

        return ResponseEntity.ok(
                adminManage.getAllDesTalukMaster()
        );
    }

    //revenue taluk master saveorupdate controller
    @PostMapping("/saveOrUpdateRev")
    public ResponseEntity<String> saveOrUpdate(
            @RequestBody RevTalukDTO dto) {

        return ResponseEntity.ok(
                adminManage.saveOrUpdateRev(dto)
        );
    }

    //getAll details of revTalukMaster
    @GetMapping("/getAllRev")
    public ResponseEntity<List<RevTalukResponse>> getAllRevTalukMaster() {

        return ResponseEntity.ok(
                adminManage.getAllRevTalukMaster()
        );
    }
    //saveOrUpdate in masterVillage
    @PostMapping("/saveOrUpdateMasterVillage")
    public ResponseEntity<String> saveOrUpdateVillage(
            @RequestBody TblMasterVillageRequest dto) {

        return ResponseEntity.ok(
                adminManage.saveOrUpdateVillage(dto)
        );
    }
    //getAll method of TblMasterVillage
    @GetMapping("/getAllMasterVillage")
    public ResponseEntity<List<TblMasterVillageRequest>> getAllVillage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String search) {

        return ResponseEntity.ok(
                adminManage.getAllVillage(page, search)
        );
    }
    //post of tblMasterVillageBlock
    @PostMapping("/saveOrUpdateVillageBlock")
    public ResponseEntity<String> saveOrUpdateVillageBlock(
            @RequestBody TblMasterVillageBlockRequest dto) {

        return ResponseEntity.ok(
                adminManage.saveOrUpdateVillageBlock(dto)
        );
    }
    //getall villageBlock
    @GetMapping("/getAllVillageBlock")
    public ResponseEntity<List<TblMasterVillageBlockRequest>> getAllVillageBlocks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String search
    ) {

        return ResponseEntity.ok(
                adminManage.getAllVillageBlocks(page, search)
        );
    }

    //PostMethod of tblMasterZone
    @PostMapping("/saveOrUpdateMasterZone")
    public ResponseEntity<String> saveOrUpdateZone(
            @RequestBody MasterZoneRequest dto) {

        return ResponseEntity.ok(
                adminManage.saveOrUpdateZone(dto)
        );
    }
//getAll method of TblMasterZone
@GetMapping("/getAllMasterZone")
public ResponseEntity<List<MasterZoneRequest>> getAllZones(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(required = false) String search) {

    return ResponseEntity.ok(
            adminManage.getAllZones(page,search)
    );
}
//saveOrUpdate TblLocalBody
@PostMapping("/saveOrUpdateLocalBody")
public ResponseEntity<String> saveOrUpdateLocalBody(
        @RequestBody TblLocalBodyRequest dto) {

    return ResponseEntity.ok(
            adminManage.saveOrUpdateLocalBody(dto)
    );
}
//getAll TblMaster LocalBody
//@GetMapping("/getAllLocalBody")
//public ResponseEntity<List<TblLocalBodyRequest>> getAllLocalBodies() {
//    return ResponseEntity.ok(
//            adminManage.getAllLocalBodies()
//    );
//}
@GetMapping("/getAllLocalBody")
public ResponseEntity<List<TblLocalBodyRequest>> getAllLocalBodies(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(required = false) String search) {

    return ResponseEntity.ok(
            adminManage.getAllLocalBodies(page, search)
    );
}
//saveOrUpdate tblMasterBlock
@PostMapping("/saveOrUpdateBlock")
public ResponseEntity<String> saveOrUpdateBlock(
        @RequestBody MasterBlockRequest dto) {

    return ResponseEntity.ok(
            adminManage.saveOrUpdateBlock(dto)
    );
}
    //getAll masterBlock
//    @GetMapping("/getAllMasterBlock")
//    public ResponseEntity<List<MasterBlockRequest>> getAllBlocks() {
//        return ResponseEntity.ok(
//                adminManage.getAllBlocks()
//        );
//    }
    @GetMapping("/getAllMasterBlock")
    public ResponseEntity<List<MasterBlockRequest>> getAllBlocks(
            @RequestParam(defaultValue ="0")int page,
            @RequestParam(required = false) String search) {

        return ResponseEntity.ok(
                adminManage.getAllBlocks(page,search)
        );
    }
}