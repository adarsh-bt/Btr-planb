package cdti.aidea.earas.controller;

import cdti.aidea.earas.contract.RequestsDTOs.ClusterEditRequestDTO;
import cdti.aidea.earas.model.Btr_models.ClusterEditAllowed;
import cdti.aidea.earas.service.ClusterEditAllowedService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@AllArgsConstructor
@RestController
@RequestMapping("/api/cluster-edit")
public class ClusterEditAllowedController {

    private final ClusterEditAllowedService editAllowedService;
    @PostMapping("/create")
    public ResponseEntity<ClusterEditAllowed>create(@RequestBody ClusterEditRequestDTO dto){
        return ResponseEntity.ok(editAllowedService.createClusterEditRequest(dto));
    }
}
