package cdti.aidea.earas.controller;

import cdti.aidea.earas.contract.Response.TblBtrDataDTO;
import cdti.aidea.earas.service.TblBtrDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/btr-data")
@RequiredArgsConstructor
public class TblBtrDataController {
  private final TblBtrDataService service;

  //    @PostMapping("/save")
//    public ResponseEntity<TblBtrData> saveData(@RequestBody TblBtrDataDTO dto) {
//        TblBtrData saved = service.saveData(dto);
//        return ResponseEntity.ok(saved);
//    }
  // ✅ Save multiple records
  @PostMapping("/saveAll")
  public ResponseEntity<List<Map<String, Object>>> saveAllData(@RequestBody List<TblBtrDataDTO> dtoList) {
    List<Map<String, Object>> savedList = service.saveAllData(dtoList);
    return ResponseEntity.ok(savedList);
  }


//    @GetMapping("/getAll")
//    public ResponseEntity<List<TblBtrDataDTO>> getAllData() {
//        return ResponseEntity.ok(service.getAllData());
//    }
  // ✅ Get by ZoneId (manual fetch without relation)
//    @GetMapping("/getByZone/{zoneId}")
//    public ResponseEntity<List<TblBtrDataDTO>> getDataByZone(@PathVariable Integer zoneId) {
//        return ResponseEntity.ok(service.getDataByZoneId(zoneId));
//    }
//    // ✅ NEW: Get grouped clusters by ZoneId (manual fetch with wet/dry grouping)
//    @GetMapping("/getClustersByZone/{zoneId}")
//    public ResponseEntity<List<Map<String, Object>>> getClustersByZone(@PathVariable Integer zoneId) {
//        return ResponseEntity.ok(service.getDataClustersByZone(zoneId));
//    }
}