package cdti.aidea.earas.controller;

import cdti.aidea.earas.contract.LocalbodyDto;
import cdti.aidea.earas.contract.RevenueTalukDto;
import cdti.aidea.earas.contract.RevenueVillageDto;
import cdti.aidea.earas.service.ZoneMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/localbodies")
@RequiredArgsConstructor
public class ZoneMappingController {

    private final ZoneMappingService zoneMappingService;

    // Modified Endpoint: Returns a JSON object
    @GetMapping("/district/{zoneId}")
    public ResponseEntity<Map<String, Integer>> getDistrictIdByZone(@PathVariable Integer zoneId) {
        return zoneMappingService.getDistrictIdByZone(zoneId)
                .map(distId -> ResponseEntity.ok(Map.of("distId", distId))) // Wrap the ID in a Map
                .orElse(ResponseEntity.notFound().build());
    }

    // Get Local Bodies by Zone
    @GetMapping("/by-zone/{zoneId}")
    public ResponseEntity<List<LocalbodyDto>> getByZone(@PathVariable Integer zoneId,
                                                        @RequestParam(defaultValue = "en") String lang) {
        List<LocalbodyDto> result = zoneMappingService.getLocalbodiesByZone(zoneId, lang);
        return ResponseEntity.ok(result);
    }

    // Get Revenue Taluks by Zone
    @GetMapping("/revenue-taluks/{zoneId}")
    public ResponseEntity<List<RevenueTalukDto>> getRevenueTaluksByZone(@PathVariable Integer zoneId,
                                                                        @RequestParam(defaultValue = "en") String lang) {
        List<RevenueTalukDto> result = zoneMappingService.getRevenueTaluksByZone(zoneId, lang);
        return ResponseEntity.ok(result);
    }

    // Get Revenue Villages by Zone (with blockCode)
    @GetMapping("/revenue-villages/{zoneId}")
    public ResponseEntity<List<RevenueVillageDto>> getRevenueVillagesByZone(@PathVariable Integer zoneId,
                                                                            @RequestParam(defaultValue = "en") String lang) {
        List<RevenueVillageDto> result = zoneMappingService.getRevenueVillagesByZone(zoneId, lang);
        return ResponseEntity.ok(result);
    }
}
