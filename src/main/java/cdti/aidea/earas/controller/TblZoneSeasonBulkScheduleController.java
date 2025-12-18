package cdti.aidea.earas.controller;

import cdti.aidea.earas.contract.Response.TblZoneSeasonBulkScheduleADTO;
import cdti.aidea.earas.service.TblZoneSeasonBulkScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/zone-season-schedule")
@RequiredArgsConstructor
public class TblZoneSeasonBulkScheduleController {
    private final TblZoneSeasonBulkScheduleService service;

    @PostMapping("/apply-by-office")
    public ResponseEntity<String> applyByOffice(
            @RequestBody TblZoneSeasonBulkScheduleADTO dto
    ) {
        service.applyScheduleByOffice(dto);
        return ResponseEntity.ok("Schedule applied successfully");
    }

}
