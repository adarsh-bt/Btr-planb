package cdti.aidea.earas.controller;

import cdti.aidea.earas.contract.Response.TblBtrDataDTO;
import cdti.aidea.earas.service.OutOfClusterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/OutOfCluster")
@RequiredArgsConstructor
public class OutOfClusterController {
    private final OutOfClusterService outOfClusterService;
    @PostMapping("/saveBtr")
    public ResponseEntity<Map<String, Object>> saveBtr(
            @RequestBody TblBtrDataDTO dto) {

        Map<String, Object> response = outOfClusterService.saveBtr(dto);

        if ("Validation Failed".equals(response.get("status"))) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }
}
