package cdti.aidea.earas.controller;
import cdti.aidea.earas.contract.Response.TblZoneSeasonScheduleDTO;
import cdti.aidea.earas.service.TblZoneSeasonScheduleService;
import cdti.aidea.earas.service.TblZoneSeasonScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zone-season-schedule")
@RequiredArgsConstructor
public class TblZoneSeasonScheduleController {
    private final TblZoneSeasonScheduleService scheduleService;

    @PostMapping("/create")
    public TblZoneSeasonScheduleDTO createSchedule(@RequestBody TblZoneSeasonScheduleDTO dto) {
        return scheduleService.createSchedule(dto);
    }

    @GetMapping("/getAll")
    public List<TblZoneSeasonScheduleDTO> getAllSchedules() {
        return scheduleService.getAllSchedules();
    }
}
