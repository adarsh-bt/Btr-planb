package cdti.aidea.earas.controller;

import cdti.aidea.earas.contract.Response.VillageResponseDTO;
import cdti.aidea.earas.service.VillageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/village")
public class VillageController {

    @Autowired
    private VillageService villageService;

    @GetMapping("/by-zone/{zoneId}/localbody/{localBodyId}/villages")
    public ResponseEntity<List<VillageResponseDTO>> getVillagesByZoneAndLocalBody(
            @PathVariable Integer zoneId,
            @PathVariable Integer localBodyId) {

        List<VillageResponseDTO> villages = villageService.getVillagesByZoneAndLocalBody(zoneId, localBodyId);
        return ResponseEntity.ok(villages);
    }
}
