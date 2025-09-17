package cdti.aidea.earas.controller;



import cdti.aidea.earas.common.exception.Response;

import cdti.aidea.earas.contract.RequestsDTOs.ZoneAssignedRequset;
import cdti.aidea.earas.contract.Response.ZoneListResponse;
import cdti.aidea.earas.model.Btr_models.UserZoneAssignment;

import cdti.aidea.earas.service.BtrExportService;
import cdti.aidea.earas.service.Zone_Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@Validated
@RestController
@RequestMapping("/btr-api")
@RequiredArgsConstructor
public class UserZoneController {

    private final Zone_Service zoneService;
    private final BtrExportService btrExportService;

//    if array

//    "roles": ["District Level Approver", "User"]
//    @RequestHeader("X-Roles") String rolesHeader
//...
//    List<String> roles = Arrays.asList(rolesHeader.split(","));
//if (!roles.contains("District Level Approver")) {
//        // Forbidden
//    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportBtrData(@RequestParam UUID userId) {
        ByteArrayResource excelFile = btrExportService.exportUserBtrDataToExcel(userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=btr_data.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(excelFile.contentLength())
                .body(excelFile);
    }
    @GetMapping("/zones/{type}/{id}")
    public ResponseEntity<Response> getById(@PathVariable("type") String type,
                                            @PathVariable("id") String id){
//                                            @RequestHeader("X-Username") String username,    for apigateway
//                                            @RequestHeader("X-Roles") String role) {

        try {
            Integer idValue = Integer.parseInt(id); // Parse the ID

            // Call the unified service method
            List<ZoneListResponse> zoneList = zoneService.UserZonesByType(type, idValue);

            return new ResponseEntity<>(Response.builder().payload(zoneList).message("User details fetched successfully.").build(), HttpStatus.OK);

        } catch (NumberFormatException e) {
            return new ResponseEntity<>(Response.builder().message("Invalid ID. Please provide a valid integer value.").build(), HttpStatus.BAD_REQUEST);
        }
    }



    @PostMapping("/assigned-zone-save")
    public ResponseEntity<Response> ZoneAssignedService(@RequestBody @Valid ZoneAssignedRequset zoneAssignedRequest) {
        try {
            UserZoneAssignment zoneListResponse = zoneService.ZoneAssignedService(zoneAssignedRequest);
            ZoneListResponse userRegistrationResponse =
                    ZoneListResponse.builder().build();
            return new ResponseEntity<>(
                    Response.builder().payload(userRegistrationResponse).message("Successfully saved").build(),
                    HttpStatus.CREATED);
        } catch (Exception ex) {

            return new ResponseEntity<>(Response.builder()
                    .message("An error occurred while processing your request "+ex.getMessage())

                    .build(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/btr-data/{UserId}")
    public ResponseEntity<Response> getByLandData(
            @PathVariable("UserId") UUID userId,
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "filter", required = false) String filter
    ) {
        System.out.println("UserId: " + userId);

        return new ResponseEntity<>(Response.builder()
                .payload(zoneService.UserAssignedLand(userId, page, size, filter))
                .message("User details fetched successfully.")
                .build(),
                HttpStatus.OK);
    }


    @GetMapping("/zone-details/{UserId}")
    public ResponseEntity<Response> getKeyPlots(
            @PathVariable("UserId") UUID userId

    ) {

        return new ResponseEntity<>(Response.builder()
                .payload(zoneService.ZoneDetails(userId))
                .message("Zone details fetched successfully.")
                .build(),
                HttpStatus.OK);
    }


}
