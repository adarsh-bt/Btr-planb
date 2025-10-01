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

  @PostMapping("/save")
  public ResponseEntity<?> saveBatch(@RequestBody List<CropAssignmentTrailSaveDto> saveDto) {
    System.out.println("SSS " + saveDto);
    List<Long> savedIds = cropAssignmentTrailService.saveCropAssignmentTrail(saveDto);

    // Build response
    return new ResponseEntity<>(
        Response.builder().payload(savedIds).message("Successfully saved crop assignments").build(),
        HttpStatus.CREATED);
  }
}
