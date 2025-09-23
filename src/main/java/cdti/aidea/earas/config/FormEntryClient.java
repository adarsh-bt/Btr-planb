package cdti.aidea.earas.config;

import cdti.aidea.earas.contract.FormEntryDto.AvailableCcePlotFetchRequest;
import cdti.aidea.earas.contract.FormEntryDto.AvailableCcePlotRejectionRequest;
import cdti.aidea.earas.contract.FormEntryDto.CceAssignmentRequest;
import cdti.aidea.earas.contract.FormEntryDto.Response;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "formEntryClient", url = "http://localhost:9114")
public interface FormEntryClient {

  @GetMapping("/earas-form1-entry/cce-crop-details/fetch-number-of-cce")
  Map<String, Object> getRawCceCropDetails(); // raw JSON map

  @PostMapping("/earas-form1-entry/available-cce-plot-details/save")
  void saveCceAssignment(@RequestBody CceAssignmentRequest assignment);

  @PostMapping("/earas-form1-entry/available-cce-plot-details/fetch-by-zoneId")
  Map<String, Object> getAvailableCcePlotsByZoneId(
      @RequestBody AvailableCcePlotFetchRequest request);

  @GetMapping("/earas-form1-entry/available-cce-plot-details/fetch-cce-crops/{clusterId}")
  Response fetchCceCrops(@PathVariable("clusterId") Long clusterId);

  @PostMapping("/earas-form1-entry/available-cce-plot-details/rejection")
  ResponseEntity<Response> availableCcePlotRejection(
      @RequestBody AvailableCcePlotRejectionRequest request);
}
