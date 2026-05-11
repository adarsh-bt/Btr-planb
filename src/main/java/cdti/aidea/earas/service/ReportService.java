package cdti.aidea.earas.service;
import cdti.aidea.earas.contract.Response.ClusterReportResponse;
import cdti.aidea.earas.contract.Response.SubDetailsClusterStatusResponse;
import cdti.aidea.earas.model.Btr_models.ClusterMaster;
import cdti.aidea.earas.repository.Btr_repo.ClusterMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ClusterMasterRepository clusterMasterRepository;

    //State and allDistricts
    public ClusterReportResponse getDashboardData(
            String landType,
            YearMonth startMonth,
            YearMonth endMonth
    ) {
        if (startMonth == null) {
            throw new RuntimeException("start month is required");
        }

//        LocalDate startDate = null;
//        LocalDate endDate = null;
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        if (landType != null) {
            landType = landType.toUpperCase();
            // landType = landType.trim();
        }

        //   if (startMonth != null) {

        startDate = startMonth.atDay(1).atStartOfDay();

        if (endMonth == null) {

            endDate = startMonth.atEndOfMonth().atTime(23, 59, 59);

        } else {

            endDate = endMonth.atEndOfMonth().atTime(23, 59, 59);
        }

        List<ClusterMaster> clusters =
                clusterMasterRepository.getDashboardData(
                        landType,
                        startDate,
                        endDate
                );

        ClusterReportResponse response =
                new ClusterReportResponse();

        Map<String, SubDetailsClusterStatusResponse> districtMap =
                new HashMap<>();

        long completed = 0;
        long ongoing = 0;
        long notStarted = 0;
        long underView = 0;

        for (ClusterMaster cluster : clusters) {

            String status = cluster.getStatus();

            String districtName = "UNKNOWN";
            Long districtId = null;

            if (cluster.getZone() != null &&
                    cluster.getZone().getDistrictMaster() != null) {

                districtName =
                        cluster.getZone()
                                .getDistrictMaster()
                                .getDistNameEn();

                districtId =
                        cluster.getZone()
                                .getDistrictMaster()
                                .getDistId()
                                .longValue();
            }

            districtMap.putIfAbsent(
                    districtName,
                    new SubDetailsClusterStatusResponse(
                            districtId,
                            0L,
                            0L,
                            0L,
                            0L
                    )
            );

            SubDetailsClusterStatusResponse districtStats =
                    districtMap.get(districtName);

            if (status == null) {
                continue;
            }

            switch (status.trim().toLowerCase()) {

                case "completed":

                    completed++;

                    districtStats.setCompleted(
                            (districtStats.getCompleted() == null
                                    ? 0L
                                    : districtStats.getCompleted()) + 1
                    );

                    break;

                case "ongoing":

                    ongoing++;

                    districtStats.setOngoing(
                            (districtStats.getOngoing() == null
                                    ? 0L
                                    : districtStats.getOngoing()) + 1
                    );

                    break;

                case "not started":

                    notStarted++;

                    districtStats.setNotStarted(
                            (districtStats.getNotStarted() == null
                                    ? 0L
                                    : districtStats.getNotStarted()) + 1
                    );

                    break;

                case "under view":

                    underView++;

                    districtStats.setUnderView(
                            (districtStats.getUnderView() == null
                                    ? 0L
                                    : districtStats.getUnderView()) + 1
                    );

                    break;
            }
        }

        response.setTotalCluster((long) clusters.size());

        response.setCompleted(completed);

        response.setOngoing(ongoing);

        response.setNotStarted(notStarted);

        response.setUnderView(underView);

        response.setAllSubDetails(districtMap);

        return response;

    }
    //based on DistrictId and corresponding taluk list
    public ClusterReportResponse getTalukWiseDashboardData(

            Integer districtId,
            String landType,
            YearMonth startMonth,
            YearMonth endMonth
    ) {
if(startMonth==null){
    throw new RuntimeException("startMonth is required");
}

//        LocalDate startDate = null;
//        LocalDate endDate = null;
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (landType != null) {
            landType = landType.toUpperCase();
        }

      //  if (startMonth != null) {

            startDate = startMonth.atDay(1).atStartOfDay();

            if (endMonth == null) {

                endDate = startMonth.atEndOfMonth().atTime(23, 59, 59);

            } else {

                endDate = endMonth.atEndOfMonth().atTime(23, 59, 59);
            }


        List<ClusterMaster> clusters =
                clusterMasterRepository.getTalukWiseDashboardData(
                        districtId,
                        landType,
                        startDate,
                        endDate
                );

        ClusterReportResponse response =
                new ClusterReportResponse();

        Map<String, SubDetailsClusterStatusResponse> talukMap =
                new HashMap<>();

        long completed = 0;
        long ongoing = 0;
        long notStarted = 0;
        long underView = 0;

        for (ClusterMaster cluster : clusters) {

            String status = cluster.getStatus();

            String talukName = "UNKNOWN";

            if (cluster.getZone() != null &&
                    cluster.getZone().getDesTalukMaster() != null) {

                talukName =
                        cluster.getZone()
                                .getDesTalukMaster()
                                .getDesTalukNameEn();
            }

            talukMap.putIfAbsent(
                    talukName,
                    new SubDetailsClusterStatusResponse(
                            cluster.getZone()   //including talukId as of need
                                    .getDesTalukMaster()
                                    .getDesTalukId()
                                    .longValue(),
                            0L,
                            0L,
                            0L,
                            0L
                    )
            );

            SubDetailsClusterStatusResponse talukStats =
                    talukMap.get(talukName);

            if (status == null) {
                continue;
            }

            switch (status.trim().toLowerCase()) {

                case "completed":

                    completed++;

                    talukStats.setCompleted(
                            (talukStats.getCompleted() == null
                                    ? 0L
                                    : talukStats.getCompleted()) + 1
                    );

                    break;

                case "ongoing":

                    ongoing++;

                    talukStats.setOngoing(
                            (talukStats.getOngoing() == null
                                    ? 0L
                                    : talukStats.getOngoing()) + 1
                    );

                    break;

                case "not started":

                    notStarted++;

                    talukStats.setNotStarted(
                            (talukStats.getNotStarted() == null
                                    ? 0L
                                    : talukStats.getNotStarted()) + 1
                    );

                    break;

                case "under view":

                    underView++;

                    talukStats.setUnderView(
                            (talukStats.getUnderView() == null
                                    ? 0L
                                    : talukStats.getUnderView()) + 1
                    );

                    break;
            }
        }

        response.setTotalCluster((long) clusters.size());

        response.setCompleted(completed);

        response.setOngoing(ongoing);

        response.setNotStarted(notStarted);

        response.setUnderView(underView);

        response.setAllSubDetails(talukMap);

        return response;
    }
}