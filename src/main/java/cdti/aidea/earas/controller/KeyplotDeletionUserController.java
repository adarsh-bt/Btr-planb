package cdti.aidea.earas.controller;


import cdti.aidea.earas.contract.RequestsDTOs.KeyplotDeletionRequestResponseDTO;
import cdti.aidea.earas.contract.RequestsDTOs.KeyplotDeletionUserRequestDTO;
import cdti.aidea.earas.service.KeyplotDeletionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/keyplot-deletion-requests")
@RequiredArgsConstructor
public class KeyplotDeletionUserController {

    private final KeyplotDeletionService deletionService;
    @PostMapping
    public ResponseEntity<KeyplotDeletionRequestResponseDTO> submitRequest(
            @Valid @RequestBody KeyplotDeletionUserRequestDTO requestDto) {
        KeyplotDeletionRequestResponseDTO response = deletionService.submitDeletionRequest(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/keyplot_deletion_impact_preview/{requestId}")
    public ResponseEntity<Map<String, Object>> getDeletionImpactPreview(
            @PathVariable UUID requestId
    ) {
        Map<String, Object> preview = deletionService.previewDeletionImpact(requestId);
        return ResponseEntity.ok(preview);
    }
//    @GetMapping("/{requestId}")
//    public ResponseEntity<KeyplotDeletionRequestResponseDTO> getRequestById(@PathVariable UUID requestId) {
//        return ResponseEntity.ok(deletionService.getRequestStatus(requestId));
//    }
//
//    @GetMapping("/track/{requestNumber}")
//    public ResponseEntity<KeyplotDeletionRequestResponseDTO> getRequestByNumber(@PathVariable String requestNumber) {
//        return ResponseEntity.ok(deletionService.getRequestStatusByNumber(requestNumber));
//    }
}