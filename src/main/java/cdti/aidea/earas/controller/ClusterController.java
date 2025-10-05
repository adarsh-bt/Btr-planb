package cdti.aidea.earas.controller;

import cdti.aidea.earas.contract.FormEntryDto.AvailableCcePlotFetchRequest;
import cdti.aidea.earas.contract.FormEntryDto.CropReplaceClusterRequest;
import cdti.aidea.earas.contract.FormEntryDto.CropReplaceClusterResponse;
import cdti.aidea.earas.contract.FormEntryDto.KeyPlotClusterDTO;
import cdti.aidea.earas.contract.RequestsDTOs.ClusterIdRequest;
import cdti.aidea.earas.contract.RequestsDTOs.DeleteClusterPlotRequest;
import cdti.aidea.earas.contract.RequestsDTOs.SaveClusterRequestDTO;
import cdti.aidea.earas.contract.Response.*;
import cdti.aidea.earas.service.ClusterService;
import cdti.aidea.earas.service.KeyPlots_Service;
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

  @GetMapping("/user-cluster-summary/{userId}")
  public ResponseEntity<UserClusterSummaryResponse> getUserClusterSummary(
      @PathVariable Integer userId) {
    System.out.println("is  " + userId);
    try {
      UserClusterSummaryResponse response = clusterService.getUserClusterSummary(userId);
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
  //    @GetMapping("/{kpId}/resbdnos-by-village-block")
  //    public ResponseEntity<ResbdnoListReponse> getResbdnosByVillageBlock(
  //            @PathVariable UUID kpId,
  //            @RequestParam Integer villageId,
  //            @RequestParam String blockCode,
  //            @RequestParam(required = false) Integer resvno) {
  //        ResbdnoListReponse response = clusterService.getResbdnoAreaList(kpId, villageId,
  // blockCode, resvno);
  //        return ResponseEntity.ok(response);
  //    }

  //
  //    @GetMapping("/{kpId}/resbdnos")
  //    public ResponseEntity<ResbdnoListReponse> getResbdnoListByKeyPlot(
  //            @PathVariable UUID kpId,
  //            @RequestParam(name = "resvno", required = false) Integer resvno) {
  //
  //        ResbdnoListReponse response = clusterService.getResbdnoList(kpId, resvno);
  //        return ResponseEntity.ok(response);
  //    }

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
          request.getKeyplotId(),
          request.getClusterNo(),
          request.getSidePlots());
      return ResponseEntity.ok(
          Collections.singletonMap("message", "Cluster form saved successfully."));

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error saving cluster form: " + e.getMessage());
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

  //    Reject the Cluster
  //    @PostMapping("/reject-cluster/{keyPlotId}")
  //    public ResponseEntity<?> rejectAndReplaceCluster(
  //            @PathVariable Long keyPlotId,
  //            @RequestBody KeyPlotRejectRequest request) {
  //        System.out.println("reject "+keyPlotId+"  "+request.getReason());
  //        System.out.println("reject Cluster"+keyPlotId+"  "+request.getReason_for_cluster());
  //        try {
  //            Map<String, Object> newPlot = keyPlots_Service.rejectAndReplaceKeyplot(keyPlotId,
  // request);
  //            return ResponseEntity.ok(newPlot);
  //        } catch (EntityNotFoundException e) {
  //            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
  //        } catch (RuntimeException e) {
  //            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // Or
  // INTERNAL_SERVER_ERROR based on the type of error
  //        } catch (Exception e) {
  //            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
  //                    .body("Error rejecting and replacing keyplot: " + e.getMessage());
  //        }
  //    }

  //    @PostMapping("/save-cluster")
  //    public ResponseEntity<?> saveClusterForm(
  //            @RequestParam("keyplotId") UUID keyplotId,
  //            @RequestParam("clusterNo") Integer clusterNo,
  //            @RequestBody List<SidePlotDTO> sidePlots) {
  //
  //        try {
  //            clusterService.saveClusterData(keyplotId, clusterNo, sidePlots);
  //            return ResponseEntity.ok("Cluster form saved successfully.");
  //        } catch (Exception e) {
  //            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
  //                    .body("Error saving cluster form: " + e.getMessage());
  //        }
  //    }
}
