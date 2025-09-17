package cdti.aidea.earas.controller;

import cdti.aidea.earas.contract.FormEntryDto.CceCropDetailsResponse;
import cdti.aidea.earas.service.CceCropService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/btr-cce")
public class CceCropController {

    private final CceCropService cceCropService;
    public CceCropController(CceCropService cceCropService) {
        this.cceCropService = cceCropService;
    }
    @GetMapping("/fetch")
    public List<CceCropDetailsResponse> fetchCrops() {
        return cceCropService.getCceCrops();
    }
}