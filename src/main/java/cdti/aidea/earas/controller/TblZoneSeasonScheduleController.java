package cdti.aidea.earas.controller;
import cdti.aidea.earas.contract.RequestsDTOs.ZoneIdFrameIdRequest;
import cdti.aidea.earas.contract.Response.TblZoneSeasonScheduleDTO;
import cdti.aidea.earas.service.TblZoneSeasonScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/zone-season-schedule")
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
    // ✅ Updated endpoint using ZoneIdFrameIdRequest as request body
    @PostMapping("/getByZoneAndFrame")
    public List<TblZoneSeasonScheduleDTO> getSchedulesByZoneAndFrame(
            @RequestBody ZoneIdFrameIdRequest request) {
        return scheduleService.getSchedulesByZoneAndFrame(request);
    }

}
