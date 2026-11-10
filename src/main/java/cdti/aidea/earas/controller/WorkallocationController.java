package cdti.aidea.earas.controller;

import cdti.aidea.earas.contract.FormEntryDto.Response;
import cdti.aidea.earas.contract.RequestsDTOs.RevokeRequest;
import cdti.aidea.earas.contract.RequestsDTOs.TblWorkAllocationDTO;
import cdti.aidea.earas.contract.RequestsDTOs.WorkAllocationVerificationRequest;
import cdti.aidea.earas.contract.Response.DistrictWetLbcodeSummaryResponse;
import cdti.aidea.earas.contract.Response.WetClusterLbcodeResponse;
import cdti.aidea.earas.contract.Response.WetClusterZoneSummaryResponse;
import cdti.aidea.earas.contract.Response.WetTalukLbcodeSummaryResponse;
import cdti.aidea.earas.service.WorkallocationService;
import cdti.aidea.earas.service.Zone_Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/btr-api")
@RequiredArgsConstructor
public class WorkallocationController {

    private final WorkallocationService workallocationService;

    @GetMapping("/zone-details-workallocation/{ZoneId}")
    public ResponseEntity<Response> getKeyPlots(@PathVariable("ZoneId") Integer ZoneId) {

        return new ResponseEntity<>(
                Response.builder()
                        .payload(workallocationService.ZoneDetailsWorkAllocation(ZoneId))
                        .message("Zone details fetched successfully.")
                        .build(),
                HttpStatus.OK);
    }

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

    @PostMapping("/verification")
    public ResponseEntity<?> verifyWorkAllocation(
            @RequestBody WorkAllocationVerificationRequest request) {

        workallocationService.verify(request);

        return ResponseEntity.ok("Verification updated successfully");
    }

    @PostMapping("/work-allocation-revoke")
    public ResponseEntity<?> revokeWorkAllocation(@RequestBody RevokeRequest request) {
        try {
            workallocationService.revokeWorkAllocation(request.getApprovalId(), request.getRemarks(),request.getRevokedBy());
            return ResponseEntity.ok("Work allocation revoked successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error revoking: " + e.getMessage());
        }
    }

    //to connect with form based on total wet cluster
    @GetMapping("/wet-cluster-lbcodes/zoneId/agriYear")
    public ResponseEntity<List<WetClusterLbcodeResponse>> getWetClusterLbcodes(
            @RequestParam Long zoneId,
            @RequestParam String agriYear) {

        return ResponseEntity.ok(
                workallocationService.getWetClusterLbcodes(
                        zoneId, agriYear));
    }

    //connect with form to get zone wise wetlbcode->talukid
    @GetMapping("/wet-cluster-lbcodes/taluk")
    public ResponseEntity<List<WetClusterZoneSummaryResponse>>
    getWetClusterLbcodesByTaluk(
            @RequestParam("talukId") Long talukId,
            @RequestParam("agriYear") String agriYear) {
        List<WetClusterZoneSummaryResponse> response =
                workallocationService.getWetClusterLbcodesByTaluk(
                        talukId, agriYear);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wet-cluster-lbcodes/district")
    public ResponseEntity<List<WetTalukLbcodeSummaryResponse>>
    getWetClusterLbcodesByDistrict(
            @RequestParam Long districtId,
            @RequestParam String agriYear) {

        return ResponseEntity.ok(
                workallocationService.getWetClusterLbcodesByDistrict(
                        districtId, agriYear));
    }
    @GetMapping("/wet-cluster-lbcodes/districts")
    public ResponseEntity<List<DistrictWetLbcodeSummaryResponse>>
    getDistrictWiseWetLbcodeCount(
            @RequestParam String agriYear) {

        return ResponseEntity.ok(
                workallocationService.getDistrictWiseWetLbcodeCount(agriYear));
    }
}
