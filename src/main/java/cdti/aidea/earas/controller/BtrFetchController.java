package cdti.aidea.earas.controller;

import cdti.aidea.earas.contract.Response.BtrDataResponse;
import cdti.aidea.earas.service.BtrFetchService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fetch-btr")
// @CrossOrigin(origins = "*", maxAge = 3600)
public class BtrFetchController {

  @Autowired private BtrFetchService btrFetchService;

  /**
   * Main endpoint: Get all BTR data for a specific zone with optional filtering Example: GET
   * /api/v1/btr/zone/1/data Example: GET /api/v1/btr/zone/1/data?filter=agricultural
   */
  @GetMapping("/zone/{zoneId}/data")
  public ResponseEntity<List<BtrDataResponse>> getAllBtrDataByZone(
      @PathVariable Integer zoneId, @RequestParam(required = false) String filter) {
    try {
      if (zoneId == null || zoneId <= 0) {
        return ResponseEntity.badRequest().build();
      }

      List<BtrDataResponse> response = btrFetchService.getAllBtrDataByZone(zoneId, filter);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/data/{id}")
  public ResponseEntity<BtrDataResponse> getBtrDataById(@PathVariable Long id) {
    try {
      System.out.println("=== DEBUG: getBtrDataById called with id: " + id + " ===");

      if (id == null || id <= 0) {
        System.out.println("DEBUG: Invalid ID: " + id);
        return ResponseEntity.badRequest().build();
      }

      Optional<BtrDataResponse> response = btrFetchService.getBtrDataById(id);

      if (response.isPresent()) {
        System.out.println("DEBUG: Found record with ID: " + response.get().getId());
        return ResponseEntity.ok(response.get());
      } else {
        System.out.println("DEBUG: No record found with ID: " + id);
        return ResponseEntity.notFound().build();
      }
    } catch (Exception e) {
      System.err.println("ERROR in getBtrDataById: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /** Get multiple BTR records by IDs Example: GET /api/v1/btr/data/bulk?ids=1,2,3,4,5 */
  @GetMapping("/data/bulk")
  public ResponseEntity<List<BtrDataResponse>> getBtrDataByIds(@RequestParam List<Long> ids) {
    try {
      if (ids == null || ids.isEmpty()) {
        return ResponseEntity.badRequest().build();
      }

      // Limit to prevent too large requests
      if (ids.size() > 100) {
        return ResponseEntity.badRequest().body(null);
      }

      List<BtrDataResponse> response = btrFetchService.getBtrDataByIds(ids);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /** Check if BTR record exists by ID Example: GET /api/v1/btr/data/123/exists */
  @GetMapping("/data/{id}/exists")
  public ResponseEntity<Boolean> checkBtrDataExists(@PathVariable Long id) {
    try {
      if (id == null || id <= 0) {
        return ResponseEntity.badRequest().build();
      }

      boolean exists = btrFetchService.getBtrDataById(id).isPresent();
      return ResponseEntity.ok(exists);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /** Get BTR record with zone validation Example: GET /api/v1/btr/zone/1/data/123 */
  @GetMapping("/zone/{zoneId}/data/{id}")
  public ResponseEntity<BtrDataResponse> getBtrDataByIdAndZone(
      @PathVariable Integer zoneId, @PathVariable Long id) {
    try {
      if (zoneId == null || zoneId <= 0 || id == null || id <= 0) {
        return ResponseEntity.badRequest().build();
      }

      Optional<BtrDataResponse> response = btrFetchService.getBtrDataById(id);

      if (response.isPresent()) {
        BtrDataResponse btrData = response.get();

        // Validate that the record belongs to the specified zone
        if (btrData.getZoneId() != null && btrData.getZoneId().equals(zoneId)) {
          return ResponseEntity.ok(btrData);
        } else {
          return ResponseEntity.notFound().build(); // Record doesn't belong to this zone
        }
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /** Get summary statistics for a zone Example: GET /api/v1/btr/zone/1/summary */
  @GetMapping("/zone/{zoneId}/summary")
  public ResponseEntity<String> getZoneSummary(@PathVariable Integer zoneId) {
    try {
      if (zoneId == null || zoneId <= 0) {
        return ResponseEntity.badRequest().build();
      }

      String summary = btrFetchService.getZoneSummary(zoneId);
      return ResponseEntity.ok(summary);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get unique land types for a zone with their counts Example: GET /api/v1/btr/zone/1/land-types
   */
  @GetMapping("/zone/{zoneId}/land-types")
  public ResponseEntity<Map<String, Long>> getLandTypesByZone(@PathVariable Integer zoneId) {
    try {
      if (zoneId == null || zoneId <= 0) {
        return ResponseEntity.badRequest().build();
      }

      Map<String, Long> landTypes = btrFetchService.getLandTypesByZone(zoneId);
      return ResponseEntity.ok(landTypes);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /** Get unique local bodies for a zone Example: GET /api/v1/btr/zone/1/local-bodies */
  @GetMapping("/zone/{zoneId}/local-bodies")
  public ResponseEntity<Map<String, String>> getLocalBodiesByZone(@PathVariable Integer zoneId) {
    try {
      if (zoneId == null || zoneId <= 0) {
        return ResponseEntity.badRequest().build();
      }

      Map<String, String> localBodies = btrFetchService.getLocalBodiesByZone(zoneId);
      return ResponseEntity.ok(localBodies);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /** Health check endpoint Example: GET /api/v1/btr/health */
  @GetMapping("/health")
  public ResponseEntity<String> healthCheck() {
    return ResponseEntity.ok("BTR Fetch Service is running successfully!");
  }

  /** Test endpoint to check zone mapping Example: GET /api/v1/btr/zone/1/test */
  @GetMapping("/zone/{zoneId}/test")
  public ResponseEntity<String> testZoneMapping(@PathVariable Integer zoneId) {
    try {
      if (zoneId == null || zoneId <= 0) {
        return ResponseEntity.badRequest().build();
      }

      // This will show you how many records are found for the zone
      List<BtrDataResponse> data = btrFetchService.getAllBtrDataByZone(zoneId, null);
      String message =
          String.format("Zone %d test successful! Found %d BTR records.", zoneId, data.size());
      return ResponseEntity.ok(message);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error testing zone " + zoneId + ": " + e.getMessage());
    }
  }

  /** Get records count for a zone (quick stats) Example: GET /api/v1/btr/zone/1/count */
  @GetMapping("/zone/{zoneId}/count")
  public ResponseEntity<Integer> getBtrCountByZone(@PathVariable Integer zoneId) {
    try {
      if (zoneId == null || zoneId <= 0) {
        return ResponseEntity.badRequest().build();
      }

      List<BtrDataResponse> data = btrFetchService.getAllBtrDataByZone(zoneId, null);
      return ResponseEntity.ok(data.size());
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
