package cdti.aidea.earas.controller;

import cdti.aidea.earas.common.exception.Response;
import cdti.aidea.earas.contract.RequestsDTOs.CropAssignmentTrailSaveDto;
import cdti.aidea.earas.service.CropAssignmentTrailService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/crop-assignment-trail")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Crop Assignment Trail", description = "APIs for managing crop assignment trails")
public class CropAssignmentTrailController {

  private final CropAssignmentTrailService cropAssignmentTrailService;

  //  @PostMapping("/save")
  //  @Operation(
  //      summary = "Save crop assignment trail",
  //      description = "Save a new crop assignment trail entry and sync with external service")
  //  public ResponseEntity<Map<String, Object>> saveCropAssignmentTrail(
  //      @Valid @RequestBody CropAssignmentTrailSaveDto saveDto) {
  //
  //    log.info("Request to save crop assignment trail for crop ID: {}", saveDto.getCropId());
  //
  //    try {
  //      Long savedId = cropAssignmentTrailService.saveCropAssignmentTrail(saveDto);
  //
  //      Map<String, Object> response =
  //          Map.of(
  //              "success",
  //              true,
  //              "message",
  //              "Crop assignment trail saved and synced successfully",
  //              "id",
  //              savedId);
  //
  //      return ResponseEntity.status(HttpStatus.CREATED).body(response);
  //
  //    } catch (IllegalStateException e) {
  //      log.warn("Invalid crop assignment trail save request: {}", e.getMessage());
  //      Map<String, Object> errorResponse = Map.of("success", false, "message", e.getMessage());
  //      return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
  //
  //    } catch (RuntimeException e) {
  //      log.error("Error saving crop assignment trail: {}", e.getMessage());
  //
  //      // Check if it's a Feign/external service error
  //      if (e.getMessage().contains("External service") || e.getMessage().contains("Feign")) {
  //        Map<String, Object> errorResponse =
  //            Map.of(
  //                "success",
  //                false,
  //                "message",
  //                "Data saved locally but external service synchronization failed: "
  //                    + e.getMessage());
  //        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(errorResponse);
  //      }
  //
  //      Map<String, Object> errorResponse =
  //          Map.of(
  //              "success",
  //              false,
  //              "message",
  //              "Failed to save crop assignment trail: " + e.getMessage());
  //      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  //    }
  //  }
  @PostMapping("/save")
  public ResponseEntity<?> saveBatch(@RequestBody List<CropAssignmentTrailSaveDto> saveDto) {
    List<Long> savedIds = cropAssignmentTrailService.saveCropAssignmentTrail(saveDto);

    // Build response
    return new ResponseEntity<>(
        Response.builder().payload(savedIds).message("Successfully saved crop assignments").build(),
        HttpStatus.CREATED);
  }
}
