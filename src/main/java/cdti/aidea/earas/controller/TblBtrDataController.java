package cdti.aidea.earas.controller;

import cdti.aidea.earas.contract.Response.TblBtrDataDTO;
import cdti.aidea.earas.service.TblBtrDataService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/btr-data")
@RequiredArgsConstructor
public class TblBtrDataController {
  private final TblBtrDataService service;

  @PostMapping("/saveAll")
  public ResponseEntity<Map<String, Object>> saveAllData(@RequestBody List<TblBtrDataDTO> dtoList) {
    System.out.println("API called: /saveAll");
    Map<String, Object> response = service.saveAllData(dtoList);

    if ("Validation Failed".equals(response.get("status"))) {
      return ResponseEntity.badRequest().body(response);
    }

    return ResponseEntity.ok(response);
  }
}