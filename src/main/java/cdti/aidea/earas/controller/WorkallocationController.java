package cdti.aidea.earas.controller;

import cdti.aidea.earas.contract.RequestsDTOs.TblWorkAllocationDTO;
import cdti.aidea.earas.service.WorkallocationService;
import cdti.aidea.earas.service.Zone_Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/btr-api")
@RequiredArgsConstructor
public class WorkallocationController {

    private final WorkallocationService workallocationService;

    @GetMapping("/work-allocation-view/{zoneId}/{agriYear}")
    public ResponseEntity<List<TblWorkAllocationDTO>> getByZone(@PathVariable Integer zoneId,@PathVariable("agriYear") String agriYear) {
        List<TblWorkAllocationDTO> allocations = workallocationService.getWorkAllocationsByZoneId(zoneId,agriYear);
        return ResponseEntity.ok(allocations);
    }

    @PostMapping("/work-allocation-save")
    public ResponseEntity<?> saveWorkAllocationDraft(@Valid @RequestBody List<TblWorkAllocationDTO> payload) {
        try {
            workallocationService.saveOrSubmitWorkAllocations(payload, false);
            return ResponseEntity.ok("Draft saved successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error saving draft: " + e.getMessage());
        }
    }

    // ✅ ADD THIS NEW ENDPOINT FOR SUBMIT
    @PostMapping("/work-allocation-submit")
    public ResponseEntity<?> submitWorkAllocation(@RequestBody List<TblWorkAllocationDTO> payload) {
        try {
            workallocationService.saveOrSubmitWorkAllocations(payload, true);
            return ResponseEntity.ok("Submitted successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error submitting: " + e.getMessage());
        }
    }
}
