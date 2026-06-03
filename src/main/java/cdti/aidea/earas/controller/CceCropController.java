package cdti.aidea.earas.controller;

import cdti.aidea.earas.common.exception.Response;
import cdti.aidea.earas.contract.FormEntryDto.CceCropDetailsResponse;
import cdti.aidea.earas.contract.FormEntryDto.FetchDistrictResponse;
import cdti.aidea.earas.service.CceCropService;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/btr-cce")
public class CceCropController {

  private final CceCropService cceCropService;

  @GetMapping("/fetch")
  public List<CceCropDetailsResponse> fetchCrops() {
    return cceCropService.getCceCrops();
  }

  @GetMapping("/fetch-districts-by-cluster-id/{clusterId}")
  public ResponseEntity<Response> getRandomCceByZone(@PathVariable Long clusterId) {
    FetchDistrictResponse responseData = cceCropService.getDistrictByClusterId(clusterId);

    return new ResponseEntity<>(
            Response.builder()
                    .payload(responseData)
                    .message(
                            "Districts for cluster Id "
                                    + clusterId
                                    + " fetched successfully.")
                    .build(),
            HttpStatus.OK);
  }

}
