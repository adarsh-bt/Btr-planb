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
}
