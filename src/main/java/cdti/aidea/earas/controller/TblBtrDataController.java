package cdti.aidea.earas.controller;

import cdti.aidea.earas.contract.Response.PlotDuplicateResponse;
import cdti.aidea.earas.contract.Response.TblBtrDataDTO;
import cdti.aidea.earas.contract.Response.ValidationResponse;
import cdti.aidea.earas.model.Btr_models.TblNonBtr;
import cdti.aidea.earas.repository.Btr_repo.TblNonBtrRepository;
import cdti.aidea.earas.service.TblBtrDataService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/btr-data")
@RequiredArgsConstructor
public class TblBtrDataController {
  private final TblBtrDataService service;
  private final TblNonBtrRepository tblNonBtrRepository;

  @PostMapping("/saveAll")
  public ResponseEntity<Map<String, Object>> saveAllData(@RequestBody List<TblBtrDataDTO> dtoList) {
    System.out.println("API called: /saveAll");
    Map<String, Object> response = service.saveAllData(dtoList);

    if ("Validation Failed".equals(response.get("status"))) {
      return ResponseEntity.badRequest().body(response);
    }

    return ResponseEntity.ok(response);
  }

  @PostMapping("/validate-duplicate")
  public ResponseEntity<?> validateDuplicate(@RequestBody TblBtrDataDTO dto) {
    ValidationResponse response = service.validateDuplicateForCluster(dto);

    if (response != null) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    } else {
      return ResponseEntity.ok(new PlotDuplicateResponse(false, "No duplicate found."));
    }
  }


  @GetMapping("/btypes/active")
  public ResponseEntity<List<TblNonBtr>> getActiveBTypes() {
    List<TblNonBtr> activeBTypes = tblNonBtrRepository.findByIsActiveTrue();
    return ResponseEntity.ok(activeBTypes);
  }
}
