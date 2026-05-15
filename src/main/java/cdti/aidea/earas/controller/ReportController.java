package cdti.aidea.earas.controller;
import cdti.aidea.earas.contract.Response.ClusterReportResponse;
import cdti.aidea.earas.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;

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
    }

