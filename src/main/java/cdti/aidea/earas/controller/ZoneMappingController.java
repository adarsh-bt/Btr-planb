package cdti.aidea.earas.controller;

import cdti.aidea.earas.contract.LocalbodyDto;
import cdti.aidea.earas.contract.Response.ZoneBtrTypeResponse;
import cdti.aidea.earas.contract.RevenueTalukDto;
import cdti.aidea.earas.contract.RevenueVillageDto;
import cdti.aidea.earas.service.ZoneMappingService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/localbodies")
@RequiredArgsConstructor
public class ZoneMappingController {

  @Autowired
  private final ZoneMappingService zoneMappingService;

  // Modified Endpoint: Returns a JSON object
  @GetMapping("/district/{zoneId}")
  public ResponseEntity<Map<String, Integer>> getDistrictIdByZone(@PathVariable Integer zoneId) {
    return zoneMappingService
        .getDistrictIdByZone(zoneId)
        .map(distId -> ResponseEntity.ok(Map.of("distId", distId))) // Wrap the ID in a Map
        .orElse(ResponseEntity.notFound().build());
  }

  // Get Local Bodies by Zone
  @GetMapping("/by-zone/{zoneId}")
  public ResponseEntity<List<LocalbodyDto>> getByZone(
      @PathVariable Integer zoneId, @RequestParam(defaultValue = "en") String lang) {
    List<LocalbodyDto> result = zoneMappingService.getLocalbodiesByZone(zoneId, lang);
    return ResponseEntity.ok(result);
  }

  // Get Revenue Taluks by Zone
  @GetMapping("/revenue-taluks/{zoneId}")
  public ResponseEntity<List<RevenueTalukDto>> getRevenueTaluksByZone(
      @PathVariable Integer zoneId, @RequestParam(defaultValue = "en") String lang) {
    List<RevenueTalukDto> result = zoneMappingService.getRevenueTaluksByZone(zoneId, lang);
    return ResponseEntity.ok(result);
  }

  // Get Revenue Villages by Zone (with blockCode)
  @GetMapping("/revenue-villages/{zoneId}")
  public ResponseEntity<List<RevenueVillageDto>> getRevenueVillagesByZone(
      @PathVariable Integer zoneId, @RequestParam(defaultValue = "en") String lang) {
    List<RevenueVillageDto> result = zoneMappingService.getRevenueVillagesByZone(zoneId, lang);
    return ResponseEntity.ok(result);
  }


  @GetMapping("/{zoneId}/btr-type")
  public ResponseEntity<?> getZoneBtrType(@PathVariable Integer zoneId) {
    try {
      ZoneBtrTypeResponse response = zoneMappingService.getZoneBtrType(zoneId);

      if (response == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "Zone not found",
                        "message", "No active zone found with id: " + zoneId
                ));
      }

      return ResponseEntity.ok(response);

    } catch (NumberFormatException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(Map.of(
                      "error", "Invalid zone ID format",
                      "message", "Zone ID must be a valid number"
              ));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(Map.of(
                      "error", "Internal server error",
                      "message", "An unexpected error occurred"
              ));
    }
  }
}
