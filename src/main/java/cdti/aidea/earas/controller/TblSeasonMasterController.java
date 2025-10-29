package cdti.aidea.earas.controller;
import java.util.List;

import cdti.aidea.earas.contract.Response.TblSeasonMasterDTO;
import cdti.aidea.earas.service.TblSeasonMasterService;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/season-master")
@RequiredArgsConstructor
public class TblSeasonMasterController {
    private final TblSeasonMasterService seasonMasterService;

    @GetMapping("/getAll")
    public List<TblSeasonMasterDTO> getAllSeasons() {
        return seasonMasterService.getAllSeasons();
    }
    // ✅ Add a new season
    @PostMapping("/add")
    public TblSeasonMasterDTO addOrUpdateSeason(@RequestBody TblSeasonMasterDTO dto) {
        return seasonMasterService.addSeason(dto);
    }
}
