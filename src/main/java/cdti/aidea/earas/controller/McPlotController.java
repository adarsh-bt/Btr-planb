package cdti.aidea.earas.controller;
import cdti.aidea.earas.contract.Response.McPlotDTO;
import cdti.aidea.earas.model.Btr_models.TblBtrData;
import cdti.aidea.earas.service.McPlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mcplot")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class McPlotController {
    private final McPlotService mcPlotService;

    @PostMapping("/save")
    public ResponseEntity<TblBtrData> saveMcPlot(@RequestBody McPlotDTO dto) {
        TblBtrData savedData = mcPlotService.saveMcPlotData(dto);
        return ResponseEntity.ok(savedData);
    }
}
