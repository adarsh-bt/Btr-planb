package cdti.aidea.earas.controller;

import cdti.aidea.earas.contract.Response.KeyplotCountResponse;
import cdti.aidea.earas.service.KeyplotCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/keyplots")
@RequiredArgsConstructor
public class KeyplotCountController {
    private final KeyplotCountService keyplotCountService;

    @GetMapping("/limit-status/{zoneId}")
    public ResponseEntity<KeyplotCountResponse> getKeyplotsLimitUsage(
            @PathVariable Integer zoneId) {
        KeyplotCountResponse response =
                keyplotCountService.getKeyplotsLimitStatus(zoneId);


        return ResponseEntity.ok(response);
    }
}
