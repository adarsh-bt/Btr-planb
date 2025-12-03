package cdti.aidea.earas.controller;

import cdti.aidea.earas.contract.RequestsDTOs.TblWorkAllocationDTO;
import cdti.aidea.earas.service.WorkallocationService;
import cdti.aidea.earas.service.Zone_Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/btr-api")
@RequiredArgsConstructor
public class WorkallocationController {

    private final WorkallocationService workallocationService;



    @GetMapping("/work-allocation-view/{zoneId}")
    public ResponseEntity<List<TblWorkAllocationDTO>> getByZone(@PathVariable Integer zoneId) {
        List<TblWorkAllocationDTO> allocations = workallocationService.getWorkAllocationsByZoneId(zoneId);
        return ResponseEntity.ok(allocations);
    }
}
