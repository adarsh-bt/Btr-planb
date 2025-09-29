package cdti.aidea.earas.controller;

import cdti.aidea.earas.contract.RequestsDTOs.ClusterLimitRequest;
import cdti.aidea.earas.contract.RequestsDTOs.KeyplotsLimitLogRequest;
import cdti.aidea.earas.contract.Response.KeyplotsLimitLogResponse;
import cdti.aidea.earas.model.Btr_models.ClusterLimitLog;
import cdti.aidea.earas.model.Btr_models.KeyplotsLimitLog;
import cdti.aidea.earas.service.AdminManage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
