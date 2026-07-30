package cdti.aidea.earas.controller;

import cdti.aidea.earas.contract.FormEntryDto.AvailableCcePlotFetchRequest;
import cdti.aidea.earas.contract.FormEntryDto.CropReplaceClusterRequest;
import cdti.aidea.earas.contract.FormEntryDto.CropReplaceClusterResponse;
import cdti.aidea.earas.contract.FormEntryDto.KeyPlotClusterDTO;
import cdti.aidea.earas.contract.RequestsDTOs.*;
import cdti.aidea.earas.contract.Response.*;
import cdti.aidea.earas.contract.ValidationErrorResponse;
import cdti.aidea.earas.service.ClusterService;
import cdti.aidea.earas.service.KeyPlots_Service;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/cluster-api")
@RequiredArgsConstructor
public class ClusterController {

  private final ClusterService clusterService;
  private final KeyPlots_Service keyPlots_Service;

  @GetMapping("/{clusterId}/form-data")
  public ResponseEntity<List<ClusterFormResponseDTO>> getClusterFormData(
      @PathVariable Long clusterId) {
    List<ClusterFormResponseDTO> result = clusterService.getFormDataByClusterId(clusterId);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/user-cluster-summary/{zoneId}/{agriYear}")
  public ResponseEntity<UserClusterSummaryResponse> getUserClusterSummary(
          @PathVariable Integer zoneId,
          @PathVariable String agriYear) {
    try {
      UserClusterSummaryResponse response = clusterService.getUserClusterSummary(zoneId,agriYear);
      return ResponseEntity.ok(response);
    } catch (NoSuchElementException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new UserClusterSummaryResponse(
                  e.getMessage(), 0, 0, 0, 0, "CCe Not Available", Collections.emptyList()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new UserClusterSummaryResponse(
                  "An internal error occurred",
                  0,
                  0,
                  0,
                  0,
                  "CCe Not Available",
                  Collections.emptyList()));
    }
  }


  @GetMapping("/cluster-form-status/{zoneId}/{agriYear}")
  public ResponseEntity<UserClusterSummaryResponse> getClusterSummary(
          @PathVariable Integer zoneId,
          @PathVariable String agriYear) {

    UserClusterSummaryResponse response =
            clusterService.getClusterSummaryWithExternalStatus(zoneId, agriYear);

    return ResponseEntity.ok(response);
  }



  //    cluster labels for App
  @PostMapping("/cluster-labels")
  public ResponseEntity<Map<String, Object>> getGroupedClusterFormData(
      @Valid @RequestBody ClusterIdRequest request) {
    Long clusterId = request.getClusterId();
    Map<String, Object> result = clusterService.getGroupedFormDataByClusterId(clusterId);
    return ResponseEntity.ok(result);
  }

  @PostMapping("/cluster-labels/delete")
  public ResponseEntity<Map<String, Object>> deleteClusterPlot(
      @Valid @RequestBody DeleteClusterPlotRequest request) {
    Long deletedId = request.getClusterPlotId();
    clusterService.deleteClusterFormDataById(deletedId);

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("status", "success");
    response.put("message", "ClusterFormData deleted successfully.");
    response.put("deleted_id", deletedId);

    return ResponseEntity.ok(response);
  }

  //    @PostMapping("/cce-cluster-replace")
  //    public ResponseEntity<CropReplaceClusterResponse> getCceCrops(@Valid @RequestBody
  // ClusterIdRequest request) {
  //        Map<String,Object> response = new LinkedHashMap<>();
  //    }
  // end app only

  //    @GetMapping("/{UserId}/villages")
  //    public ResponseEntity<List<VillagesListResponse>> getVillagessByKeyPlot(@PathVariable UUID
  // UserId) {
  //        List<VillagesListResponse> response = clusterService.getVillagesListByKeyPlotId(UserId);
  //        return ResponseEntity.ok(response);
  //    }

  @GetMapping("/{lbcode}/villages")
  public ResponseEntity<List<VillagesListResponse>> getVillagessByKeyPlot(
      @PathVariable String lbcode) {
    List<VillagesListResponse> response = clusterService.getVillagesListByLbCode(lbcode);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{kpId}/resvnos")
  public ResponseEntity<List<Integer>> getResvnos(
      @PathVariable UUID kpId, @RequestParam Integer villageId, @RequestParam String blockCode) {

    List<Integer> resvnos = clusterService.getResvnoList(kpId, villageId, blockCode);
    return ResponseEntity.ok(resvnos);
  }

  @GetMapping("/{kpId}/resbdnos-by-village-block")
  public ResponseEntity<ResbdnoListReponse> getResbdnosByVillageBlock(
      @PathVariable UUID kpId,
      @RequestParam Integer villageId,
      @RequestParam String blockCode,
      @RequestParam(required = false) Integer resvnoStart,
      @RequestParam(required = false) Integer resvnoEnd) {

    System.out.println(
            "resvnoStart: "
                    + resvnoStart
                    + ", resvnoEnd: "
                    + resvnoEnd
                    + " "
                    + kpId
                    + " "
                    + villageId
                    + " "
                    + blockCode);

    ResbdnoListReponse response =
        clusterService.getResbdnoAreaList(kpId, villageId, blockCode, resvnoStart, resvnoEnd);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/next-cluster")
  public CropReplaceClusterResponse getNextCluster(
      @Valid @RequestBody CropReplaceClusterRequest request) {
    return clusterService.getNextCluster(request);
  }

  @GetMapping("/{kpId}/plot-details")
  public ResponseEntity<ClusterPlotAreaRes> getPlotDetails(
      @PathVariable UUID kpId, @RequestParam Integer resvno, @RequestParam String resbdno) {

    ClusterPlotAreaRes response = clusterService.getPlotDetails(kpId, resvno, resbdno);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/save-cluster")
  public ResponseEntity<?> saveClusterForm(@RequestBody SaveClusterRequestDTO request) {
    try {
      clusterService.saveClusterData(
          request.getUserId(),
          request.getZoneId(),
          request.getKeyplotId(),
          request.getClusterNo(),
          request.getStatus(),
          request.getRemarks(),
              request.getAgriYear(),
          request.getSidePlots());
      return ResponseEntity.ok(
          Collections.singletonMap("message", "Cluster form saved successfully."));

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(e.getMessage());
    }
  }

  @DeleteMapping("/delete-sideplot/{id}")
  public ResponseEntity<?> deleteClusterFormData(@PathVariable Long id) {
    System.out.println("delete");
    try {
      clusterService.deleteClusterFormDataById(id);
      return ResponseEntity.ok("ClusterFormData entry deleted successfully.");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error deleting ClusterFormData: " + e.getMessage());
    }
  }

  @PostMapping("/clusters/by-zone")
  public ResponseEntity<List<KeyPlotClusterDTO>> getClustersByZoneId(
      @Valid @RequestBody AvailableCcePlotFetchRequest request) {

    // Convert Long zoneId to UUID if needed here or update the service/repository accordingly
    Integer zoneIdLong = Math.toIntExact(request.getZoneId());

    List<KeyPlotClusterDTO> clusters = clusterService.getClustersByZoneId(zoneIdLong);

    if (clusters.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(clusters);
  }

  @PostMapping("/single/save-plot")
  public ResponseEntity<?> savePlotFromMobile(

          @RequestBody PlotSaveMobileAppRequest request) {

    try {
      Map<String, Object> result = clusterService.savePlotFromMobile(request);
      return ResponseEntity.ok(result);
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(Collections.singletonMap("error", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(Collections.singletonMap("error", "Internal error: " + e.getMessage()));
    }
  }

  @GetMapping("/{btrId}/btrplot-usage")
  public ResponseEntity<List<BtrClusterUsageResponse>> getBtrClusterUsage(
          @PathVariable Long btrId) {

    return ResponseEntity.ok(
            clusterService.getBtrClusterUsage(btrId)
    );
  }

  @PatchMapping("/update-sideplot/{id}")
  public ResponseEntity<?> updateClusterPlot(
          @PathVariable Long id,
          @Valid @RequestBody UpdateClusterPlotRequest request) {
    try {
      clusterService.updateClusterPlot(id, request.getEnumeratedArea(), request.getUserId());
      return ResponseEntity.ok(Collections.singletonMap("message", "Cluster plot updated successfully."));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(Collections.singletonMap("error", e.getMessage()));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(Collections.singletonMap("error", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(Collections.singletonMap("error", "An internal error occurred: " + e.getMessage()));
    }
  }

  @PutMapping("/cluster/number-update")
  public ResponseEntity<?> bulkUpdateCluster(@RequestBody List<ClusterUpdateDTO> updates) {
    clusterService.bulkUpdateClusterNumbers(updates);
    return ResponseEntity.ok("Bulk update successful");
  }

  @GetMapping("/cluster/{id}")
  public ClusterTourResponse getCluster(@PathVariable Long id) {
    return clusterService.getClusterDetails(id);
  }

  @PostMapping("/cluster-labels/list")
  public ResponseEntity<List<ClusterLabelResponse>> getClusterLabels(
          @Valid @RequestBody ClusterIdRequest request) {

    return ResponseEntity.ok(
            clusterService.getClusterLabels(request.getClusterId()));
  }

  @GetMapping("/cluster-list")
  public ResponseEntity<List<ClusterIdNumberResponse>> getClusterList(
          @RequestParam Integer zoneId,
          @RequestParam String agriYear) {

    return ResponseEntity.ok(
            clusterService.getClusters(zoneId, agriYear)
    );
  }

}
