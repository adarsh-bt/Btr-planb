package cdti.aidea.earas.controller;

import cdti.aidea.earas.contract.Response.KeyplotCountResponse;
import cdti.aidea.earas.service.KeyPlots_Service;
//import cdti.aidea.earas.service.KeyplotCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/keyplots")
@RequiredArgsConstructor
public class KeyplotCountController {
    private final KeyPlots_Service keyPlotsService;

    @GetMapping("/limit-status/{zoneId}")
    public ResponseEntity<KeyplotCountResponse> getKeyplotsLimitUsage(
            @PathVariable Integer zoneId,
            @RequestParam String agriYear) {

        KeyplotCountResponse response =
                keyPlotsService.getKeyplotsLimitStatus(
                        zoneId,
                        agriYear
                );

        return ResponseEntity.ok(response);
    }
}