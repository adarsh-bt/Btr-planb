package cdti.aidea.earas.service;
import cdti.aidea.earas.contract.Response.ClusterReportResponse;
import cdti.aidea.earas.contract.Response.DistrictClusterStatusResponse;
import cdti.aidea.earas.model.Btr_models.ClusterMaster;
import cdti.aidea.earas.repository.Btr_repo.ClusterMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ClusterMasterRepository clusterMasterRepository;

    public ClusterReportResponse getDashboardData(
            String landType,
            YearMonth startMonth,
            YearMonth endMonth
   )
//    {
//        if (startMonth != null && endMonth == null) {
//
//            endMonth = startMonth.withDayOfMonth(
//                    startMonth.lengthOfMonth()
//            );
//        }

//        List<ClusterMaster> clusters =
//                clusterMasterRepository.getDashboardData(
//                        landType,
//                        startMonth,
//                        endMonth
//                );
    {

        LocalDate startDate = null;
        LocalDate endDate = null;
        if (landType != null) {
            landType = landType.toUpperCase();
        }

        if (startMonth != null) {

            startDate = startMonth.atDay(1);

            if (endMonth == null) {

                endDate = startMonth.atEndOfMonth();

            } else {

                endDate = endMonth.atEndOfMonth();
            }
        }

        List<ClusterMaster> clusters =
                clusterMasterRepository.getDashboardData(
                        landType,
                        startDate,
                        endDate
                );

        ClusterReportResponse response =
                new ClusterReportResponse();

        Map<String, DistrictClusterStatusResponse> districtMap =
                new HashMap<>();

        long completed = 0;
        long ongoing = 0;
        long notStarted = 0;
        long underView = 0;

        for (ClusterMaster cluster : clusters) {

            String status = cluster.getStatus();

            String districtName = "UNKNOWN";

            if (cluster.getZone() != null &&
                    cluster.getZone().getDistrictMaster() != null) {

                districtName =
                        cluster.getZone()
                                .getDistrictMaster()
                                .getDistNameEn();
            }

            districtMap.putIfAbsent(
                    districtName,
                    new DistrictClusterStatusResponse()
            );

            DistrictClusterStatusResponse districtStats =
                    districtMap.get(districtName);

            if (status == null) {
                continue;
            }

//            switch (status.trim().toLowerCase()) {
//
//                case "completed":
//
//                    completed++;
//
//                    districtStats.setCompleted(
//                            districtStats.getCompleted() + 1
//                    );
//
//                    break;
//
//                case "ongoing":
//
//                    ongoing++;
//
//                    districtStats.setOngoing(
//                            districtStats.getOngoing() + 1
//                    );
//
//                    break;
//
//                case "not started":
//
//                    notStarted++;
//
//                    districtStats.setNotStarted(
//                            districtStats.getNotStarted() + 1
//                    );
//
//                    break;
//
//                case "under view":
//
//                    underView++;
//
//                    districtStats.setUnderView(
//                            districtStats.getUnderView() + 1
//                    );
//
//                    break;
//            }
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

        response.setAllDistricts(districtMap);

        return response;

    }
}