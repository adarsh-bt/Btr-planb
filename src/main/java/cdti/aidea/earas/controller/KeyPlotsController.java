package cdti.aidea.earas.controller;

import cdti.aidea.earas.common.exception.Response;
import cdti.aidea.earas.contract.RequestsDTOs.KeyPlotDetailsRequest;
import cdti.aidea.earas.contract.RequestsDTOs.KeyPlotRejectRequest;
import cdti.aidea.earas.contract.Response.KeyPlotDetailsResponse;
import cdti.aidea.earas.contract.Response.KeyPlotOwnerDetailsResponse;
import cdti.aidea.earas.model.Btr_models.KeyPlots;
import cdti.aidea.earas.service.KeyPlots_Service;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;


@Validated
@RestController
@RequestMapping("/key-plots")
@RequiredArgsConstructor
@Slf4j

public class KeyPlotsController {

  private final KeyPlots_Service keyPlots_Service;

  @GetMapping("/get-all/{zoneId}")
  public ResponseEntity<Response> getAllKeyPlotsWithDetails(@PathVariable("zoneId") Integer zoneId) {
    try {
      List<KeyPlotDetailsResponse> keyPlots = keyPlots_Service.getAllKeyPlotsWithDetails(zoneId);

      return ResponseEntity.ok(
          Response.builder()
              .payload(keyPlots)
              .message("All key plots fetched successfully.")
              .build());

    } catch (Exception e) {
      log.error("Error fetching all key plots: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Response.builder().message("Error fetching key plots: " + e.getMessage()).build());
    }
  }

//    @GetMapping("/fetch-existing-keyplots/{userId}")
//    public ResponseEntity<Response> getExistingKeyPlots(@PathVariable("userId") UUID userId) {
//        return new ResponseEntity<>(Response.builder()
//                .payload(keyPlots_Service.getExistingKeyPlots(userId))
//                .message("Existing key plots fetched successfully.")
//                .build(),
//                HttpStatus.OK);
//    }


//    @GetMapping("/generate-keyplots/{userId}")
//    public ResponseEntity<Response> generateKeyPlots(@PathVariable("userId") UUID userId) {
//        return new ResponseEntity<>(Response.builder()
//                .payload(keyPlots_Service.KeyplotsFormationOldbtr(userId))
//                .message("Key plots generated successfully.")
//                .build(),
//                HttpStatus.OK);
//    }

//  @GetMapping("/fetch-by-id/{kpId}")
//  public ResponseEntity<Response> getById(@PathVariable("kpId") UUID kpId) {
//    return new ResponseEntity<>(
//        Response.builder()
//            .payload(keyPlots_Service.getByKpId(kpId))
//            .message("Key plot details fetched successfully.")
//            .build(),
//        HttpStatus.OK);
//  }
  // @PostMapping("/fetch-existing-keyplots")
  // public ResponseEntity<Response> getExistingKeyPlots(@Valid @RequestBody KeyplotsFetchUserIdReq
  // request) {
  //    UUID userId = request.getUserId();
  //    Long zoneId = request.getZone_id();
  //    System.out.println("iossss");
  //    return new ResponseEntity<>(Response.builder()
  //            .payload(keyPlots_Service.getExistingKeyPlots(userId,zoneId))
  //            .message("Existing key plots fetched successfully.")
  //            .build(),
  //            HttpStatus.OK);
  // }

  @Operation(summary = "Save key plot details")
  @PostMapping("/save")
  public ResponseEntity<Response> saveOrUpdateKeyPlotDetails(
      @RequestBody @Valid KeyPlotDetailsRequest request) {

    KeyPlots plotDetails = keyPlots_Service.keyplotsOwnersaveOrUpdate(request);

    KeyPlotOwnerDetailsResponse response =
        KeyPlotOwnerDetailsResponse.builder()
            .id(plotDetails.getId())
            .owner_name(plotDetails.getOwner_name()) // Set owner name
            .address(plotDetails.getAddress()) // Set address
            .phone_number(plotDetails.getPhone_number()) // Set mobile number
            .build();

    String message = request.getKpId() == null ? "Successfully saved" : "Successfully updated";

    return new ResponseEntity<>(
        Response.builder().payload(response).message(message).build(), HttpStatus.CREATED);
  }

  //    @PostMapping("/reject-and-replace/{keyPlotId}")
  //    public ResponseEntity<?> rejectAndReplaceKeyplot(
  //            @PathVariable Long keyPlotId,
  //            @RequestBody KeyPlotRejectRequest request) {
  //        System.out.println("reject "+keyPlotId+"  "+request.getReason());
  //        try {
  //            Map<String, Object> newPlot = keyPlots_Service.rejectAndReplaceKeyplot(keyPlotId,
  // request);
  //            return ResponseEntity.ok(newPlot);
  //        } catch (EntityNotFoundException e) {
  //            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
  //        } catch (RuntimeException e) {
  //            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // Or
  // INTERNAL_SERVER_ERROR based on the type of error
  //        } catch (Exception e) {
  //            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
  //                    .body("Error rejecting and replacing keyplot: " + e.getMessage());
  //        }
  //    }

  @GetMapping("/get-keyplot/{plotId}")
  public ResponseEntity<Response> getKeyPlotById(@PathVariable("plotId") UUID plotId) {
    try {
      KeyPlotDetailsResponse details = keyPlots_Service.getKeyPlotDetails(plotId);

      return ResponseEntity.ok(
          Response.builder()
              .payload(details)
              .message("KeyPlot details fetched successfully.")
              .build());

    } catch (NoSuchElementException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Response.builder().message(e.getMessage()).build());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Response.builder().message("An unexpected error occurred").build());
    }
  }

//    @GetMapping("/fetch-by-keyplotsdetails/{kpId}")
//    public ResponseEntity<Response> getByPlotsDetails(@PathVariable("kpId") UUID kpId) {
//        return new ResponseEntity<>(
//                Response.builder()
//                        .payload(keyPlots_Service.getByKpId(kpId))
//                        .message("Key plot details fetched successfully.")
//                        .build(),
//                HttpStatus.OK);
//    }
//    @PostMapping("/reject-keyplot/{keyPlotId}")
//    public ResponseEntity<Map<String, Object>> rejectAndReplaceKeyplot(
//            @PathVariable UUID keyPlotId,
//            @RequestBody KeyPlotRejectRequest request) {
//
//        Map<String, Object> response = keyPlots_Service.rejectAndReplaceKeyplot(keyPlotId, request);
//        return ResponseEntity.ok(response);
//    }

}
