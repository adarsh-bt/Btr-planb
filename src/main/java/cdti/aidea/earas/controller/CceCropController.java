package cdti.aidea.earas.controller;

import cdti.aidea.earas.common.exception.Response;
import cdti.aidea.earas.contract.FormEntryDto.*;
import cdti.aidea.earas.contract.Response.FormClusterDetailsResponse;
import cdti.aidea.earas.service.CceCropService;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/btr-cce")
public class CceCropController {

  private final CceCropService cceCropService;

  @GetMapping("/fetch")
  public List<CceCropDetailsResponse> fetchCrops() {
    return cceCropService.getCceCrops();
  }

  @PostMapping("/fetch-districts-by-cluster-ids")
  public ResponseEntity<Response> getDistrictsByClusterIds(
          @RequestBody ClusterIdsRequest request) {

    List<FetchDistrictResponse> responseData =
            cceCropService.getDistrictsByClusterIds(
                    request.getClusterIds());

    return ResponseEntity.ok(
            Response.builder()
                    .payload(responseData)
                    .message("Districts fetched successfully")
                    .build());
  }

  @GetMapping("/fetch-taluks-by-distId/{distId}")
  public ResponseEntity<?> fetchTalukByDistId(
          @PathVariable Long distId,
          @RequestParam String agriYear) {

    List<FetchTalukResponse> responseData =
            cceCropService.getTalukByDistrictId(distId, agriYear);

    return ResponseEntity.ok(
            Response.builder()
                    .payload(responseData)
                    .message("Taluks fetched successfully")
                    .build());
  }

  @GetMapping("/fetch-blocks-by-talukId/{talukId}")
  public ResponseEntity<?> fetchBlocksByTalukId(
          @PathVariable Long talukId,
          @RequestParam String agriYear) {

    List<FetchBlocksResponse> responseData =
            cceCropService.getBlocksByTalukId(talukId, agriYear);

    return ResponseEntity.ok(
            Response.builder()
                    .payload(responseData)
                    .message("Blocks fetched successfully")
                    .build());
  }
  //cluster details needs to interconnect with form1
//  @GetMapping("/cluster-localbody")
//  public ResponseEntity<List<FormClusterDetailsResponse>>
//  getClusterLocalBodyDetails(
//          @RequestParam List<Long> clusterIds) {
//
//      return ResponseEntity.ok(
//              cceCropService.getClusterLocalBodyDetails(clusterIds)
//      );
//  }
  @GetMapping("/cluster-localbody")
  public ResponseEntity<List<FormClusterDetailsResponse>>
  getClusterLocalBodyDetails(
          @RequestParam List<Long> clusterIds) {

      return ResponseEntity.ok(
              cceCropService.getClusterLocalBodyDetails(clusterIds)
      );
  }
}
