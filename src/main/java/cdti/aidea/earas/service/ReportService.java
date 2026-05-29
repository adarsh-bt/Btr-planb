package cdti.aidea.earas.service;
import cdti.aidea.earas.contract.Response.BlockZoneWiseClusterStatusResponse;
import cdti.aidea.earas.contract.Response.ClusterReportResponse;
import cdti.aidea.earas.contract.Response.SubDetailsClusterStatusResponse;
import cdti.aidea.earas.model.Btr_models.ClusterMaster;
import cdti.aidea.earas.model.Btr_models.Masters.MasterBlock;
import cdti.aidea.earas.model.Btr_models.Masters.ZoneLocalbodyBlockMapping;
import cdti.aidea.earas.repository.Btr_repo.ClusterMasterRepository;
import cdti.aidea.earas.repository.Btr_repo.MasterBlockRepository;
import cdti.aidea.earas.repository.Btr_repo.ZoneLocalbodyBlockMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.RequestEntity.put;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ClusterMasterRepository clusterMasterRepository;
    private final ZoneLocalbodyBlockMappingRepository zoneLocalbodyBlockMappingRepository;
    private final MasterBlockRepository masterBlockRepository;

    //State and allDistricts
public ClusterReportResponse getDashboardData(

        String landType,
        YearMonth startMonth,
        YearMonth endMonth
) {

    if (startMonth == null) {
        throw new RuntimeException("start month is required");
    }

    if (landType != null) {
        landType = landType.toUpperCase().trim();
    }

    LocalDateTime startDate =
            startMonth.atDay(1).atStartOfDay();

    LocalDateTime endDate;

    if (endMonth == null) {

        endDate =
                startMonth
                        .atEndOfMonth()
                        .atTime(23, 59, 59);

    } else {

        endDate =
                endMonth
                        .atEndOfMonth()
                        .atTime(23, 59, 59);
    }

    List<ClusterMaster> clusters =
            clusterMasterRepository.getDashboardData(
                    startDate,
                    endDate
            );

    ClusterReportResponse response =
            new ClusterReportResponse();

    Map<String, SubDetailsClusterStatusResponse> districtMap =
            new LinkedHashMap<>();

    long completed = 0;
    long ongoing = 0;
    long notStarted = 0;
    long underView = 0;

    boolean includeWet =
            landType == null
                    || "WET".equalsIgnoreCase(landType);

    boolean includeDry =
            landType == null
                    || "DRY".equalsIgnoreCase(landType);

    for (ClusterMaster cluster : clusters) {

        String status = cluster.getStatus();

        String clusterLandType = null;

        if (cluster.getKeyPlot() != null) {

            clusterLandType =
                    cluster.getKeyPlot()
                            .getLandType();
        }

        String districtName = "UNKNOWN";

        Long districtId = null;

        if (
                cluster.getZone() != null
                        &&
                        cluster.getZone().getDistrictMaster() != null
        ) {

            districtName =
                    cluster.getZone()
                            .getDistrictMaster()
                            .getDist_name_en();

            districtId =
                    cluster.getZone()
                            .getDistrictMaster()
                            .getDist_id()
                            .longValue();
        }

        districtMap.putIfAbsent(
                districtName,
                new SubDetailsClusterStatusResponse(
                        districtId,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L
                )
        );

        SubDetailsClusterStatusResponse districtStats =
                districtMap.get(districtName);

        if (status == null || clusterLandType == null) {
            continue;
        }

        switch (status.trim().toLowerCase()) {

            case "completed":

                if (
                        clusterLandType.equalsIgnoreCase("WET")
                                && includeWet
                ) {

                    completed++;

                    districtStats.setWetCompleted(
                            districtStats.getWetCompleted() + 1
                    );
                }

                if (
                        clusterLandType.equalsIgnoreCase("DRY")
                                && includeDry
                ) {

                    completed++;

                    districtStats.setDryCompleted(
                            districtStats.getDryCompleted() + 1
                    );
                }

                break;

            case "ongoing":

                if (
                        clusterLandType.equalsIgnoreCase("WET")
                                && includeWet
                ) {

                    ongoing++;

                    districtStats.setWetOngoing(
                            districtStats.getWetOngoing() + 1
                    );
                }

                if (
                        clusterLandType.equalsIgnoreCase("DRY")
                                && includeDry
                ) {

                    ongoing++;

                    districtStats.setDryOngoing(
                            districtStats.getDryOngoing() + 1
                    );
                }

                break;

            case "not started":

                if (
                        clusterLandType.equalsIgnoreCase("WET")
                                && includeWet
                ) {

                    notStarted++;

                    districtStats.setWetNotStarted(
                            districtStats.getWetNotStarted() + 1
                    );
                }

                if (
                        clusterLandType.equalsIgnoreCase("DRY")
                                && includeDry
                ) {

                    notStarted++;

                    districtStats.setDryNotStarted(
                            districtStats.getDryNotStarted() + 1
                    );
                }

                break;

            case "under view":

                if (
                        clusterLandType.equalsIgnoreCase("WET")
                                && includeWet
                ) {

                    underView++;

                    districtStats.setWetUnderView(
                            districtStats.getWetUnderView() + 1
                    );
                }

                if (
                        clusterLandType.equalsIgnoreCase("DRY")
                                && includeDry
                ) {

                    underView++;

                    districtStats.setDryUnderView(
                            districtStats.getDryUnderView() + 1
                    );
                }

                break;
        }
    }

    response.setTotalCluster(
            completed + ongoing + notStarted + underView
    );

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

    if (startMonth == null) {
        throw new RuntimeException("startMonth is required");
    }

    if (landType != null) {
        landType = landType.toUpperCase().trim();
    }

    LocalDateTime startDate =
            startMonth.atDay(1).atStartOfDay();

    LocalDateTime endDate;

    if (endMonth == null) {

        endDate =
                startMonth
                        .atEndOfMonth()
                        .atTime(23, 59, 59);

    } else {

        endDate =
                endMonth
                        .atEndOfMonth()
                        .atTime(23, 59, 59);
    }

    List<ClusterMaster> clusters =
            clusterMasterRepository.getTalukWiseDashboardData(
                    districtId,
                    startDate,
                    endDate
            );

    ClusterReportResponse response =
            new ClusterReportResponse();

    Map<String, SubDetailsClusterStatusResponse> talukMap =
            new LinkedHashMap<>();

    long completed = 0;
    long ongoing = 0;
    long notStarted = 0;
    long underView = 0;

    boolean includeWet =
            landType == null
                    || "WET".equalsIgnoreCase(landType);

    boolean includeDry =
            landType == null
                    || "DRY".equalsIgnoreCase(landType);

    for (ClusterMaster cluster : clusters) {

        String status = cluster.getStatus();

        String clusterLandType = null;

        if (cluster.getKeyPlot() != null) {

            clusterLandType =
                    cluster.getKeyPlot()
                            .getLandType();
        }

        String talukName = "UNKNOWN";

        Long talukId = null;

        if (
                cluster.getZone() != null
                        &&
                        cluster.getZone().getDesTalukMaster() != null
        ) {

            talukName =
                    cluster.getZone()
                            .getDesTalukMaster()
                            .getDesTalukNameEn();

            talukId =
                    (long) cluster.getZone()
                            .getDesTalukMaster()
                            .getDesTalukId();
        }

        talukMap.putIfAbsent(
                talukName,
                new SubDetailsClusterStatusResponse(
                        talukId,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L
                )
        );

        SubDetailsClusterStatusResponse talukStats =
                talukMap.get(talukName);

        if (status == null || clusterLandType == null) {
            continue;
        }

        switch (status.trim().toLowerCase()) {

            case "completed":

                if (
                        clusterLandType.equalsIgnoreCase("WET")
                                && includeWet
                ) {

                    completed++;

                    talukStats.setWetCompleted(
                            talukStats.getWetCompleted() + 1
                    );
                }

                if (
                        clusterLandType.equalsIgnoreCase("DRY")
                                && includeDry
                ) {

                    completed++;

                    talukStats.setDryCompleted(
                            talukStats.getDryCompleted() + 1
                    );
                }

                break;

            case "ongoing":

                if (
                        clusterLandType.equalsIgnoreCase("WET")
                                && includeWet
                ) {

                    ongoing++;

                    talukStats.setWetOngoing(
                            talukStats.getWetOngoing() + 1
                    );
                }

                if (
                        clusterLandType.equalsIgnoreCase("DRY")
                                && includeDry
                ) {

                    ongoing++;

                    talukStats.setDryOngoing(
                            talukStats.getDryOngoing() + 1
                    );
                }

                break;

            case "not started":

                if (
                        clusterLandType.equalsIgnoreCase("WET")
                                && includeWet
                ) {

                    notStarted++;

                    talukStats.setWetNotStarted(
                            talukStats.getWetNotStarted() + 1
                    );
                }

                if (
                        clusterLandType.equalsIgnoreCase("DRY")
                                && includeDry
                ) {

                    notStarted++;

                    talukStats.setDryNotStarted(
                            talukStats.getDryNotStarted() + 1
                    );
                }

                break;

            case "under view":

                if (
                        clusterLandType.equalsIgnoreCase("WET")
                                && includeWet
                ) {

                    underView++;

                    talukStats.setWetUnderView(
                            talukStats.getWetUnderView() + 1
                    );
                }

                if (
                        clusterLandType.equalsIgnoreCase("DRY")
                                && includeDry
                ) {

                    underView++;

                    talukStats.setDryUnderView(
                            talukStats.getDryUnderView() + 1
                    );
                }

                break;
        }
    }

    response.setTotalCluster(
            completed + ongoing + notStarted + underView
    );

    response.setCompleted(completed);

    response.setOngoing(ongoing);

    response.setNotStarted(notStarted);

    response.setUnderView(underView);

    response.setAllSubDetails(talukMap);

    return response;
}

    //based on talukId list zone status details
    //trail2:
//public ClusterReportResponse getZoneWiseDashboardData(
//
//        Integer talukId,
//        String landType,
//        YearMonth startMonth,
//        YearMonth endMonth
//) {
//
//    if (startMonth == null) {
//        throw new RuntimeException("Start month is required");
//    }
//
//    if (landType != null) {
//        landType = landType.toUpperCase().trim();
//    }
//
//    LocalDateTime startDate =
//            startMonth.atDay(1).atStartOfDay();
//
//    LocalDateTime endDate;
//
//    if (endMonth == null) {
//
//        endDate =
//                startMonth
//                        .atEndOfMonth()
//                        .atTime(23, 59, 59);
//
//    } else {
//
//        endDate =
//                endMonth
//                        .atEndOfMonth()
//                        .atTime(23, 59, 59);
//    }
//
//    List<ClusterMaster> clusters =
//            clusterMasterRepository.getZoneWiseDashboardData(
//                    talukId,
//                    startDate,
//                    endDate
//            );
//
//    ClusterReportResponse response =
//            new ClusterReportResponse();
//
//    Map<String, BlockZoneWiseClusterStatusResponse> zoneMap =
//            new LinkedHashMap<>();
//
//    long completed = 0;
//    long ongoing = 0;
//    long notStarted = 0;
//    long underView = 0;
//
//    boolean includeWet =
//            landType == null
//                    || "WET".equalsIgnoreCase(landType);
//
//    boolean includeDry =
//            landType == null
//                    || "DRY".equalsIgnoreCase(landType);
//
//    for (ClusterMaster cluster : clusters) {
//
//        String status = cluster.getStatus();
//
//        String zoneName = "UNKNOWN";
//
//        Long zoneId = null;
//
//        Integer blockId = null;
//
//        String blockName = null;
//
//        String clusterLandType = null;
//
//        if (cluster.getKeyPlot() != null) {
//
//            clusterLandType =
//                    cluster.getKeyPlot()
//                            .getLandType();
//        }
//
//        if (cluster.getZone() != null) {
//
//            zoneName =
//                    cluster.getZone()
//                            .getZoneNameEn();
//
//            zoneId =
//                    cluster.getZone()
//                            .getZoneId()
//                            .longValue();
//
//            List<ZoneLocalbodyBlockMapping> mappings =
//                    zoneLocalbodyBlockMappingRepository
//                            .findByZone(
//                                    cluster.getZone().getZoneId()
//                            );
//
//            if (mappings != null && !mappings.isEmpty()) {
//
//                ZoneLocalbodyBlockMapping mapping =
//                        mappings.get(0);
//
//                if (mapping.getBlockDetails() != null) {
//
//                    MasterBlock block =
//                            masterBlockRepository
//                                    .findById(
//                                            mapping.getBlockDetails()
//                                    )
//                                    .orElse(null);
//
//                    if (block != null) {
//
//                        blockId = block.getBlockId();
//
//                        blockName = block.getBlockName();
//                    }
//                }
//            }
//        }
//
//        zoneMap.putIfAbsent(
//                zoneName,
//                new BlockZoneWiseClusterStatusResponse(
//                        zoneId,
//                        blockId,
//                        blockName,
//                        0L,
//                        0L,
//                        0L,
//                        0L,
//                        0L,
//                        0L,
//                        0L,
//                        0L
//                )
//        );
//
//        BlockZoneWiseClusterStatusResponse zoneStats =
//                zoneMap.get(zoneName);
//
//        if (status == null || clusterLandType == null) {
//            continue;
//        }
//
//        switch (status.trim().toLowerCase()) {
//
//            case "completed":
//
//                if (
//                        clusterLandType.equalsIgnoreCase("WET")
//                                && includeWet
//                ) {
//
//                    completed++;
//
//                    zoneStats.setWetCompleted(
//                            zoneStats.getWetCompleted() + 1
//                    );
//                }
//
//                if (
//                        clusterLandType.equalsIgnoreCase("DRY")
//                                && includeDry
//                ) {
//
//                    completed++;
//
//                    zoneStats.setDryCompleted(
//                            zoneStats.getDryCompleted() + 1
//                    );
//                }
//
//                break;
//
//            case "ongoing":
//
//                if (
//                        clusterLandType.equalsIgnoreCase("WET")
//                                && includeWet
//                ) {
//
//                    ongoing++;
//
//                    zoneStats.setWetOngoing(
//                            zoneStats.getWetOngoing() + 1
//                    );
//                }
//
//                if (
//                        clusterLandType.equalsIgnoreCase("DRY")
//                                && includeDry
//                ) {
//
//                    ongoing++;
//
//                    zoneStats.setDryOngoing(
//                            zoneStats.getDryOngoing() + 1
//                    );
//                }
//
//                break;
//
//            case "not started":
//
//                if (
//                        clusterLandType.equalsIgnoreCase("WET")
//                                && includeWet
//                ) {
//
//                    notStarted++;
//
//                    zoneStats.setWetNotStarted(
//                            zoneStats.getWetNotStarted() + 1
//                    );
//                }
//
//                if (
//                        clusterLandType.equalsIgnoreCase("DRY")
//                                && includeDry
//                ) {
//
//                    notStarted++;
//
//                    zoneStats.setDryNotStarted(
//                            zoneStats.getDryNotStarted() + 1
//                    );
//                }
//
//                break;
//
//            case "under view":
//
//                if (
//                        clusterLandType.equalsIgnoreCase("WET")
//                                && includeWet
//                ) {
//
//                    underView++;
//
//                    zoneStats.setWetUnderView(
//                            zoneStats.getWetUnderView() + 1
//                    );
//                }
//
//                if (
//                        clusterLandType.equalsIgnoreCase("DRY")
//                                && includeDry
//                ) {
//
//                    underView++;
//
//                    zoneStats.setDryUnderView(
//                            zoneStats.getDryUnderView() + 1
//                    );
//                }
//
//                break;
//        }
//    }
//
//    response.setTotalCluster(
//            completed + ongoing + notStarted + underView
//    );
//
//    response.setCompleted(completed);
//
//    response.setOngoing(ongoing);
//
//    response.setNotStarted(notStarted);
//
//    response.setUnderView(underView);
//
//    response.setAllSubDetails(zoneMap);
//
//    return response;
//}
    //added pagination and search in db reacords based on zoneName
    public ClusterReportResponse getZoneWiseDashboardData(

            Integer talukId,
            String landType,
            YearMonth startMonth,
            YearMonth endMonth,
            String search
    ) {

        if (startMonth == null) {
            throw new RuntimeException("Start month is required");
        }

        if (landType != null) {
            landType = landType.toUpperCase().trim();
        }

        LocalDateTime startDate =
                startMonth.atDay(1).atStartOfDay();

        LocalDateTime endDate;

        if (endMonth == null) {

            endDate =
                    startMonth
                            .atEndOfMonth()
                            .atTime(23, 59, 59);

        } else {

            endDate =
                    endMonth
                            .atEndOfMonth()
                            .atTime(23, 59, 59);
        }

        List<ClusterMaster> clusters =
                clusterMasterRepository.getZoneWiseDashboardData(
                        talukId,
                        startDate,
                        endDate
                );

        ClusterReportResponse response =
                new ClusterReportResponse();

        Map<String, BlockZoneWiseClusterStatusResponse> zoneMap =
                new LinkedHashMap<>();

        long completed = 0;
        long ongoing = 0;
        long notStarted = 0;
        long underView = 0;

        boolean includeWet =
                landType == null
                        || "WET".equalsIgnoreCase(landType);

        boolean includeDry =
                landType == null
                        || "DRY".equalsIgnoreCase(landType);

        for (ClusterMaster cluster : clusters) {

            String status = cluster.getStatus();

            String zoneName = "UNKNOWN";

            Long zoneId = null;

            Integer blockId = null;

            String blockName = null;

            String clusterLandType = null;

            if (cluster.getKeyPlot() != null) {

                clusterLandType =
                        cluster.getKeyPlot()
                                .getLandType();
            }

            if (cluster.getZone() != null) {

                zoneName =
                        cluster.getZone()
                                .getZoneNameEn();

                zoneId =
                        cluster.getZone()
                                .getZoneId()
                                .longValue();
                // ADD SEARCH FILTER HERE

                if (
                        search != null
                                && !search.trim().isEmpty()
                                && !zoneName.toLowerCase()
                                .contains(search.toLowerCase())
                ) {
                    continue;
                }

                List<ZoneLocalbodyBlockMapping> mappings =
                        zoneLocalbodyBlockMappingRepository
                                .findByZone(
                                        cluster.getZone().getZoneId()
                                );

                if (mappings != null && !mappings.isEmpty()) {

                    ZoneLocalbodyBlockMapping mapping =
                            mappings.get(0);

                    if (mapping.getBlockDetails() != null) {

                        MasterBlock block =
                                masterBlockRepository
                                        .findById(
                                                mapping.getBlockDetails()
                                        )
                                        .orElse(null);

                        if (block != null) {

                            blockId = block.getBlockId();

                            blockName = block.getBlockName();
                        }
                    }
                }
            }

            zoneMap.putIfAbsent(
                    zoneName,
                    new BlockZoneWiseClusterStatusResponse(
                            zoneId,
                            blockId,
                            blockName,
                            0L,
                            0L,
                            0L,
                            0L,
                            0L,
                            0L,
                            0L,
                            0L
                    )
            );

            BlockZoneWiseClusterStatusResponse zoneStats =
                    zoneMap.get(zoneName);

            if (status == null || clusterLandType == null) {
                continue;
            }

            switch (status.trim().toLowerCase()) {

                case "completed":

                    if (
                            clusterLandType.equalsIgnoreCase("WET")
                                    && includeWet
                    ) {

                        completed++;

                        zoneStats.setWetCompleted(
                                zoneStats.getWetCompleted() + 1
                        );
                    }

                    if (
                            clusterLandType.equalsIgnoreCase("DRY")
                                    && includeDry
                    ) {

                        completed++;

                        zoneStats.setDryCompleted(
                                zoneStats.getDryCompleted() + 1
                        );
                    }

                    break;

                case "ongoing":

                    if (
                            clusterLandType.equalsIgnoreCase("WET")
                                    && includeWet
                    ) {

                        ongoing++;

                        zoneStats.setWetOngoing(
                                zoneStats.getWetOngoing() + 1
                        );
                    }

                    if (
                            clusterLandType.equalsIgnoreCase("DRY")
                                    && includeDry
                    ) {

                        ongoing++;

                        zoneStats.setDryOngoing(
                                zoneStats.getDryOngoing() + 1
                        );
                    }

                    break;

                case "not started":

                    if (
                            clusterLandType.equalsIgnoreCase("WET")
                                    && includeWet
                    ) {

                        notStarted++;

                        zoneStats.setWetNotStarted(
                                zoneStats.getWetNotStarted() + 1
                        );
                    }

                    if (
                            clusterLandType.equalsIgnoreCase("DRY")
                                    && includeDry
                    ) {

                        notStarted++;

                        zoneStats.setDryNotStarted(
                                zoneStats.getDryNotStarted() + 1
                        );
                    }

                    break;

                case "under view":

                    if (
                            clusterLandType.equalsIgnoreCase("WET")
                                    && includeWet
                    ) {

                        underView++;

                        zoneStats.setWetUnderView(
                                zoneStats.getWetUnderView() + 1
                        );
                    }

                    if (
                            clusterLandType.equalsIgnoreCase("DRY")
                                    && includeDry
                    ) {

                        underView++;

                        zoneStats.setDryUnderView(
                                zoneStats.getDryUnderView() + 1
                        );
                    }

                    break;
            }
        }

        response.setTotalCluster(
                completed + ongoing + notStarted + underView
        );

        response.setCompleted(completed);

        response.setOngoing(ongoing);

        response.setNotStarted(notStarted);

        response.setUnderView(underView);

        response.setAllSubDetails(zoneMap);

        return response;
    }
}