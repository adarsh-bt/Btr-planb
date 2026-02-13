package cdti.aidea.earas.controller;

import cdti.aidea.earas.common.exception.Response;
import cdti.aidea.earas.contract.RequestsDTOs.ClusterApprovalActionDTO;
import cdti.aidea.earas.contract.RequestsDTOs.ClusterLimitRequest;
import cdti.aidea.earas.contract.RequestsDTOs.KeyplotsLimitLogRequest;
import cdti.aidea.earas.contract.RequestsDTOs.ZoneUserAssignDto;
import cdti.aidea.earas.contract.Response.AdminZoneResponse;
import cdti.aidea.earas.contract.Response.ClusterApprovalTableDTO;
import cdti.aidea.earas.contract.Response.KeyplotsLimitLogResponse;
import cdti.aidea.earas.contract.Response.ZoneListResponse;
//import cdti.aidea.earas.contract.UserAccessDTOs.ZoneDetailsWithUserAccessResponse;
import cdti.aidea.earas.model.Btr_models.ClusterLimitLog;
import cdti.aidea.earas.model.Btr_models.KeyplotsLimitLog;
import cdti.aidea.earas.service.AdminManage;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/admin-manage")
@RequiredArgsConstructor
@Slf4j
public class Admincontroller {

  private final AdminManage adminManage;

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
    @GetMapping("/zones")
    public ResponseEntity<List<AdminZoneResponse>> getAllZones() {
        return ResponseEntity.ok(adminManage.getAllZonesWithSeasonDates());
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
  // To get zone assigned details before interconnection
//    @GetMapping("/active")
//    public ResponseEntity<List<ZoneUserAssignDto>>getActiveZones() {
//        return ResponseEntity.ok(
//                adminManage.getActiveZonesWithAssignment()
//        );
//    }


  // To get zone assigned details after interconnection

//    @GetMapping("/active")
//    public ResponseEntity<List<ZoneUserAssignDto>> getActiveZones() {
//
//        return ResponseEntity.ok(
//                adminManage.getAllZonesWithUsers()
//        );
//    }

    //interconnection with pagination done to aassign zone userdetails
    @GetMapping("/active")
    public ResponseEntity<Response> getActiveZones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        List<ZoneUserAssignDto> allZones =
                adminManage.getAllZonesWithUsers();

        int start = page * size;
        int end = Math.min(start + size, allZones.size());

        List<ZoneUserAssignDto> paginatedList =
                (start >= allZones.size()) ?
                        List.of() :
                        allZones.subList(start, end);

        return ResponseEntity.ok(
                Response.builder()
                        .payload(paginatedList)
                        .message("Zones fetched successfully")
                        .build()
        );
    }



}
