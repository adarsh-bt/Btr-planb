package cdti.aidea.earas.controller;

import cdti.aidea.earas.contract.Response.TblBtrDataDTO;
import cdti.aidea.earas.contract.Response.TblBtrDataResponseDTO;
import cdti.aidea.earas.model.Btr_models.TblBtrData;
import cdti.aidea.earas.service.TblBtrDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/btr-data")
@RequiredArgsConstructor
public class TblBtrDataController {
    private final TblBtrDataService service;

    // ✅ Fixed: Return DTO instead of Entity
        @GetMapping("/all")
    public ResponseEntity<List<TblBtrDataResponseDTO>> getAllBtrData() {
        List<TblBtrDataResponseDTO> btrDataList = service.getAllBtrData();
        return ResponseEntity.ok(btrDataList);
    }

    // ✅ Fixed: Return DTO instead of Entity
    @GetMapping("/{id}")
    public ResponseEntity<TblBtrDataResponseDTO> getBtrDataById(@PathVariable Long id) {
        Optional<TblBtrDataResponseDTO> btrData = service.getBtrDataById(id);

        if (btrData.isPresent()) {
            return ResponseEntity.ok(btrData.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

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


}
