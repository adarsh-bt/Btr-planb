package cdti.aidea.earas.controller;

import cdti.aidea.earas.common.exception.Response;
import cdti.aidea.earas.contract.RequestsDTOs.CropAssignmentTrailSaveDto;
import cdti.aidea.earas.service.ClusterService;
import cdti.aidea.earas.service.CropAssignmentTrailService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/crop-assignment-trail")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Crop Assignment Trail", description = "APIs for managing crop assignment trails")
public class CropAssignmentTrailController {

  private final CropAssignmentTrailService cropAssignmentTrailService;
  private final ClusterService clusterService;

  @PostMapping("/save")
  public ResponseEntity<?> saveBatch(@RequestBody List<CropAssignmentTrailSaveDto> saveDto) {
    System.out.println("SSS "+saveDto);
    List<Long> savedIds = cropAssignmentTrailService.saveCropAssignmentTrail(saveDto);

    // Build response
    return new ResponseEntity<>(
        Response.builder().payload(savedIds).message("Successfully saved crop assignments").build(),
        HttpStatus.CREATED);
  }

  @GetMapping("/{clusterId}/cce-crops")
  public ResponseEntity<Map<String, Object>> getCceCropsByClusterId(@PathVariable Long clusterId) {
    try {
      Map<String, Object> result = clusterService.getGroupedFormDataByClusterId(clusterId);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      Map<String, Object> errorResponse = Map.of(
              "error", e.getMessage(),
              "clusterId", clusterId,
              "status", "failed"
      );
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }
}
