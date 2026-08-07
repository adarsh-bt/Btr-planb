package cdti.aidea.earas.controller;
import cdti.aidea.earas.contract.ClusterCompletedProgressResponse;
import cdti.aidea.earas.contract.ClusterCompletedProgressSubDetails;
import cdti.aidea.earas.contract.Response.ClusterReportResponse;
import cdti.aidea.earas.contract.WorkAllocationProgressResponse;
import cdti.aidea.earas.model.Btr_models.ClusterMaster;
import cdti.aidea.earas.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/report")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/clusters/AllDistricts")
    public ClusterReportResponse getClusterDashboard(

            @RequestParam(required = false)
            String landType,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM")
            YearMonth startMonth,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM")
            YearMonth endMonth
    ) {

        return reportService.getDashboardData(
                landType,
                startMonth,
                endMonth

        );
    }

    //based on districtId talukwise status list
    @GetMapping("/clusters/district/taluk-wise")
    public ClusterReportResponse getTalukWiseDashboard(

            @RequestParam
            Integer districtId,

            @RequestParam(required = false)
            String landType,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM")
            YearMonth startMonth,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM")
            YearMonth endMonth
    ) {

        return reportService.getTalukWiseDashboardData(
                districtId,
                landType,
                startMonth,
                endMonth
        );
    }
    //based on talukId gets the details of zone status
//    @GetMapping("/clusters/taluk/{talukId}/zones")
//    public ClusterReportResponse getZoneWiseDashboard(
//
//            @PathVariable Integer talukId,
//
//            @RequestParam(required = false)
//            String landType,
//
//            @RequestParam
//            @DateTimeFormat(pattern = "yyyy-MM")
//            YearMonth startMonth,
//
//            @RequestParam(required = false)
//            @DateTimeFormat(pattern = "yyyy-MM")
//            YearMonth endMonth,
//
//            @RequestParam("page")
//            int page,
//
//            @RequestParam("size")
//            int size
//    ) {
//
//        if (page < 0) {
//            throw new RuntimeException("Page number must be greater than or equal to 0");
//        }
//
//        if (size <= 0 || size > 100) {
//            throw new RuntimeException("Page size must be between 1 and 100" );
//        }
//
//        return reportService.getZoneWiseDashboardData(
//                talukId,
//                landType,
//                startMonth,
//                endMonth
//        );
//    }

    //added pagination and search based on zoneName
    @GetMapping("/clusters/taluk/{talukId}/zones")
    public ClusterReportResponse getZoneWiseDashboard(

            @PathVariable Integer talukId,

            @RequestParam(required = false)
            String landType,

            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM")
            YearMonth startMonth,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM")
            YearMonth endMonth,

            @RequestParam("page")
            int page,

            @RequestParam("size")
            int size,

            @RequestParam(required = false)
            String search
    ) {

        if (page < 0) {
            throw new RuntimeException(
                    "Page number must be greater than or equal to 0"
            );
        }

        if (size <= 0 || size > 100) {
            throw new RuntimeException(
                    "Page size must be between 1 and 100"
            );
        }

        return reportService.getZoneWiseDashboardData(
                talukId,
                landType,
                startMonth,
                endMonth,
                search
        );
    }

    //Total cluster completed status needs to connect with form1
    @GetMapping("/completed-clusters")
    public ResponseEntity<ClusterCompletedProgressResponse> getCompletedClusters(

            @RequestParam(required = false) String landType,
            @RequestParam String agriYear

           ) {

        return ResponseEntity.ok(
                reportService.getCompletedClusters(
                        landType,
                        agriYear));
    }

    //Total cluster completed status needs to connect with form1 DId passing taluk wise
    @GetMapping("/dashboard/completed/taluk")
    public ResponseEntity<ClusterCompletedProgressResponse> getTalukCompletedClusters(

            @RequestParam Integer districtId,

            @RequestParam(required = false) String landType,

            @RequestParam String agriYear) {

        return ResponseEntity.ok(
                reportService.getTalukWiseCompletedClusters(
                        districtId,
                        landType,
                        agriYear));
    }

    //total cluster status taluk-id zone wise details connect with form
    @GetMapping("/dashboard/completed/zone")
    public ResponseEntity<ClusterCompletedProgressResponse> getZoneCompletedClusters(

            @RequestParam Integer talukId,

            @RequestParam(required = false) String landType,

            @RequestParam String agriYear,

            @RequestParam(required = false) String search) {

        return ResponseEntity.ok(
                reportService.getZoneWiseCompletedClusters(
                        talukId,
                        landType,
                        agriYear,
                        search));
    }

    //work allocation report district wise all kerala
    @GetMapping("/work-allocation-progress")
    public ResponseEntity<WorkAllocationProgressResponse>
    getWorkAllocationProgress(
            @RequestParam String agriYear) {

        WorkAllocationProgressResponse response =
                reportService
                        .getWorkAllocationProgress(agriYear);

        return ResponseEntity.ok(response);
    }

    //work allocation report taluk wise districtId
    @GetMapping("/work-allocation-progress/{districtId}")
    public ResponseEntity<WorkAllocationProgressResponse>
    getWorkAllocationProgressByDistrict(
            @PathVariable Integer districtId,
            @RequestParam String agriYear) {

        return ResponseEntity.ok(
                reportService.getWorkAllocationProgressByDistrict(
                        districtId,
                        agriYear
                )
        );
    }

    //work allocation progress report zone wise details passing talukid
    @GetMapping("/work-allocation-progress/taluk/{talukId}")
    public ResponseEntity<?> getWorkAllocationProgressByTaluk(
            @PathVariable Integer talukId,
            @RequestParam String agriYear) {

        try {

            WorkAllocationProgressResponse response =
                    reportService.getWorkAllocationProgressByTaluk(
                            talukId,
                            agriYear
                    );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Error fetching work allocation progress: "
                                    + e.getMessage()
                    );
        }
    }
    }

