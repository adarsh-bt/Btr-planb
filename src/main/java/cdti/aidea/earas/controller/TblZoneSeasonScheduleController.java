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
    //    @GetMapping("/getPaginated")
//    public List<TblZoneSeasonScheduleDTO> getPaginatedSchedules(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//        return scheduleService.getPaginatedSchedules(page, size);
//    }
    @GetMapping("/getPaginated")
    public List<TblZoneSeasonScheduleDTO> getPaginatedSchedules(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // ✅ Convert from 1-based to 0-based indexing for service compatibility
        int adjustedPage = (page > 0) ? page - 1 : 0;
        return scheduleService.getPaginatedSchedules(adjustedPage, size);
    }
}