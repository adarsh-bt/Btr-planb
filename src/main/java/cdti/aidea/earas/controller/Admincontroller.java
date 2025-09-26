package cdti.aidea.earas.controller;

import cdti.aidea.earas.contract.RequestsDTOs.ClusterLimitRequest;
import cdti.aidea.earas.contract.RequestsDTOs.KeyplotsLimitLogRequest;
import cdti.aidea.earas.contract.Response.KeyplotsLimitLogResponse;
import cdti.aidea.earas.contract.Response.ZoneListResponse;
import cdti.aidea.earas.model.Btr_models.ClusterLimitLog;
import cdti.aidea.earas.model.Btr_models.KeyplotsLimitLog;
import cdti.aidea.earas.repository.Btr_repo.KeyplotsLimitLogRepository;
import cdti.aidea.earas.service.AdminManage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Validated
@RestController
@RequestMapping("/admin-manage")
@RequiredArgsConstructor
@Slf4j
public class Admincontroller {

  private final AdminManage adminManage;


  @GetMapping("/keyplot-limits")
  public List<KeyplotsLimitLogResponse> getAllKeyplotLimits() {
    return adminManage.getAllKeyplots();
  }


  @PostMapping("/save-keyplot-limits")
  public ResponseEntity<KeyplotsLimitLog> createKeyplotsLimit(@RequestBody KeyplotsLimitLogRequest request) {
    KeyplotsLimitLog saved = adminManage.saveOrUpdateKeyplotsLimit(request);
    return ResponseEntity.ok(saved);
  }

  @PostMapping("/save-cluster-limits")
  public ResponseEntity<ClusterLimitLog> createOrUpdateClusterLimit(@RequestBody ClusterLimitRequest request) {
    ClusterLimitLog saved = adminManage.saveOrUpdateClusterLimit(request);
    return ResponseEntity.ok(saved);
  }

  @GetMapping("/zones/{type}/{id}")
  public ResponseEntity<List<ZoneListResponse>> getById(@PathVariable("type") String type,
                                                        @PathVariable("id") String id) {
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

}