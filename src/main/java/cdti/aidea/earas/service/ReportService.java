package cdti.aidea.earas.service;
import cdti.aidea.earas.contract.ClusterCompletedProgressResponse;
import cdti.aidea.earas.contract.ClusterCompletedProgressSubDetails;
import cdti.aidea.earas.contract.Response.BlockZoneWiseClusterStatusResponse;
import cdti.aidea.earas.contract.Response.ClusterReportResponse;
import cdti.aidea.earas.contract.Response.SubDetailsClusterStatusResponse;
import cdti.aidea.earas.contract.WorkAllocationProgressResponse;
import cdti.aidea.earas.model.Btr_models.ClusterMaster;
import cdti.aidea.earas.model.Btr_models.Masters.*;
import cdti.aidea.earas.model.Btr_models.TblWorkAllocation;
import cdti.aidea.earas.repository.Btr_repo.*;
import cdti.aidea.earas.utils.AgriYearUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.RequestEntity.put;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ClusterMasterRepository clusterMasterRepository;
    private final ZoneLocalbodyBlockMappingRepository zoneLocalbodyBlockMappingRepository;
    private final MasterBlockRepository masterBlockRepository;
    private final LocalBodyRepository tblLocalBodyRepository;
    private final DistrictMasterRepository districtMasterRepository;
    private final TblWorkAllocationRepository tblWorkAllocationRepository;
    private final DesTalukRepository  desTalukRepository;
    private final MasterBlockRepository  tblMasterBlockRepository;
    private final LocalBodyRepository localBodyRepository;



    //State and allDistricts
public ClusterReportResponse getDashboardData(

        String landType,
        YearMonth startMonth,
        YearMonth endMonth
) {
System.out.println("start year "+startMonth+"  "+endMonth);
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

            case "on going":

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

            case "under review":

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

            case "on going":

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

            case "under review":

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

//                        MasterBlock block =
//                                masterBlockRepository
//                                        .findById(
//                                                mapping.getBlockDetails()
//                                        )
//                                        .orElse(null);
//
//                        if (block != null) {
//
//                            blockId = block.getBlockId();
//
//                            blockName = block.getBlockName();
//
                        if (mapping.getBlockPanchayatMunicipalArea() == 1) {

                            MasterBlock block =
                                    masterBlockRepository
                                            .findById(
                                                    mapping.getBlockDetails()
                                            )
                                            .orElse(null);

                            if (block != null) {

                                blockId =
                                        block.getBlockId();

                                blockName =
                                        block.getBlockName();
                            }

                        } else if (mapping.getBlockPanchayatMunicipalArea() == 2) {

                            TblLocalBody localBody =
                                    tblLocalBodyRepository
                                            .findById(
                                                    mapping.getBlockDetails()
                                            )
                                            .orElse(null);

                            if (localBody != null) {

                                blockId =
                                        localBody.getLocalbodyId();

                                blockName =
                                        localBody.getLocalbodyNameEn();
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

                    case "on going":

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

                    case "under review":

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
    //Total cluster completed status needs to connect with form1
    public ClusterCompletedProgressResponse getCompletedClusters(
            String landType,
            String agriYear) {

        if (agriYear == null || agriYear.isBlank()) {
            throw new RuntimeException("Agricultural year is required");
        }

        if (landType != null) {
            landType = landType.trim().toUpperCase();
        }

        LocalDate agriStart =
                AgriYearUtil.getAgriYearStart(agriYear);

        LocalDate agriEnd =
                AgriYearUtil.getAgriYearEnd(agriYear);

        LocalDateTime startDate =
                agriStart.atStartOfDay();

        LocalDateTime endDate =
                agriEnd.atTime(23, 59, 59);

        List<ClusterMaster> clusters =
                clusterMasterRepository.getDashboardData(
                        startDate,
                        endDate);

        Map<String, ClusterCompletedProgressSubDetails> districtMap =
                new LinkedHashMap<>();

        long totalCompleted = 0;

        boolean includeWet =
                landType == null
                        || "WET".equalsIgnoreCase(landType);

        boolean includeDry =
                landType == null
                        || "DRY".equalsIgnoreCase(landType);

        for (ClusterMaster cluster : clusters) {

            if (!"completed".equalsIgnoreCase(cluster.getStatus())) {
                continue;
            }

            String clusterLandType = null;

            if (cluster.getKeyPlot() != null) {
                clusterLandType =
                        cluster.getKeyPlot().getLandType();
            }

            if (clusterLandType == null) {
                continue;
            }

            String districtName = "UNKNOWN";
            Long districtId = null;

            if (cluster.getZone() != null &&
                    cluster.getZone().getDistrictMaster() != null) {

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
                    ClusterCompletedProgressSubDetails.builder()
                            .id(districtId)
                            .wetCompleted(0L)
                            .dryCompleted(0L)
                            .build());

            ClusterCompletedProgressSubDetails district =
                    districtMap.get(districtName);

            if ("WET".equalsIgnoreCase(clusterLandType) && includeWet) {

                totalCompleted += 3;

                district.setWetCompleted(
                        district.getWetCompleted() + 3);
            }

            if ("DRY".equalsIgnoreCase(clusterLandType) && includeDry) {

                totalCompleted += 3;

                district.setDryCompleted(
                        district.getDryCompleted() + 3);
            }
        }

        return ClusterCompletedProgressResponse.builder()
                .totalClusterCompleted(totalCompleted)
                .allSubDetails(districtMap)
                .build();
    }

    //Total cluster completed status needs to connect with form1 passing dId taluk wise
    public ClusterCompletedProgressResponse getTalukWiseCompletedClusters(

            Integer districtId,
            String landType,
            String agriYear) {

        if (agriYear == null || agriYear.isBlank()) {
            throw new RuntimeException("Agricultural year is required");
        }

        if (landType != null) {
            landType = landType.trim().toUpperCase();
        }

        LocalDate agriStart =
                AgriYearUtil.getAgriYearStart(agriYear);

        LocalDate agriEnd =
                AgriYearUtil.getAgriYearEnd(agriYear);

        LocalDateTime startDate =
                agriStart.atStartOfDay();

        LocalDateTime endDate =
                agriEnd.atTime(23, 59, 59);

        List<ClusterMaster> clusters =
                clusterMasterRepository.getTalukWiseDashboardDataByAgriYear(
                        districtId,
                        startDate,
                        endDate);

        Map<String, ClusterCompletedProgressSubDetails> talukMap =
                new LinkedHashMap<>();

        long totalCompleted = 0;

        boolean includeWet =
                landType == null
                        || "WET".equalsIgnoreCase(landType);

        boolean includeDry =
                landType == null
                        || "DRY".equalsIgnoreCase(landType);

        for (ClusterMaster cluster : clusters) {

            if (!"completed".equalsIgnoreCase(cluster.getStatus())) {
                continue;
            }

            if (cluster.getKeyPlot() == null) {
                continue;
            }

            String clusterLandType = cluster.getKeyPlot().getLandType();

            if (clusterLandType == null) {
                continue;
            }

            String talukName = "UNKNOWN";
            Long talukId = null;

            if (cluster.getZone() != null &&
                    cluster.getZone().getDesTalukMaster() != null) {

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
                    ClusterCompletedProgressSubDetails.builder()
                            .id(talukId)
                            .wetCompleted(0L)
                            .dryCompleted(0L)
                            .build());

            ClusterCompletedProgressSubDetails taluk =
                    talukMap.get(talukName);

            if ("WET".equalsIgnoreCase(clusterLandType) && includeWet) {

                totalCompleted += 3;

                taluk.setWetCompleted(
                        taluk.getWetCompleted() + 3);
            }

            if ("DRY".equalsIgnoreCase(clusterLandType) && includeDry) {

                totalCompleted += 3;

                taluk.setDryCompleted(
                        taluk.getDryCompleted() + 3);
            }
        }

        return ClusterCompletedProgressResponse.builder()
                .totalClusterCompleted(totalCompleted)
                .allSubDetails(talukMap)
                .build();
    }
    //zone wise details while passing taluk-id needs to connect with form
    public ClusterCompletedProgressResponse getZoneWiseCompletedClusters(

            Integer talukId,
            String landType,
            String agriYear,
            String search) {

        if (agriYear == null || agriYear.isBlank()) {
            throw new RuntimeException("Agricultural year is required");
        }

        if (landType != null) {
            landType = landType.trim().toUpperCase();
        }

        LocalDate agriStart =
                AgriYearUtil.getAgriYearStart(agriYear);

        LocalDate agriEnd =
                AgriYearUtil.getAgriYearEnd(agriYear);

        LocalDateTime startDate =
                agriStart.atStartOfDay();

        LocalDateTime endDate =
                agriEnd.atTime(23, 59, 59);

        List<ClusterMaster> clusters =
                clusterMasterRepository.getZoneWiseDashboardDataByAgriYear(
                        talukId,
                        startDate,
                        endDate);

        Map<String, ClusterCompletedProgressSubDetails> zoneMap =
                new LinkedHashMap<>();

        long totalCompleted = 0;

        boolean includeWet =
                landType == null
                        || "WET".equalsIgnoreCase(landType);

        boolean includeDry =
                landType == null
                        || "DRY".equalsIgnoreCase(landType);

        for (ClusterMaster cluster : clusters) {

            if (!"completed".equalsIgnoreCase(cluster.getStatus())) {
                continue;
            }

            if (cluster.getKeyPlot() == null) {
                continue;
            }

            String clusterLandType =
                    cluster.getKeyPlot().getLandType();

            if (clusterLandType == null) {
                continue;
            }

            String zoneName = "UNKNOWN";
            Long zoneId = null;

            if (cluster.getZone() != null) {

                zoneName =
                        cluster.getZone().getZoneNameEn();

                zoneId =
                        cluster.getZone().getZoneId().longValue();
            }

            // Search
            if (search != null &&
                    !search.trim().isEmpty() &&
                    !zoneName.toLowerCase().contains(search.toLowerCase())) {
                continue;
            }

            zoneMap.putIfAbsent(
                    zoneName,
                    ClusterCompletedProgressSubDetails.builder()
                            .id(zoneId)
                            .wetCompleted(0L)
                            .dryCompleted(0L)
                            .build());

            ClusterCompletedProgressSubDetails zone =
                    zoneMap.get(zoneName);

            if ("WET".equalsIgnoreCase(clusterLandType) && includeWet) {

                totalCompleted += 3;

                zone.setWetCompleted(
                        zone.getWetCompleted() + 3);
            }

            if ("DRY".equalsIgnoreCase(clusterLandType) && includeDry) {

                totalCompleted += 3;

                zone.setDryCompleted(
                        zone.getDryCompleted() + 3);
            }
        }

        return ClusterCompletedProgressResponse.builder()
                .totalClusterCompleted(totalCompleted)
                .allSubDetails(zoneMap)
                .build();
    }

    //work allocation report district wise all kerala
    public WorkAllocationProgressResponse getWorkAllocationProgress(
            String agriYear) {

        LocalDate agriStart = AgriYearUtil.getAgriYearStart(agriYear);

        LocalDate agriEnd = AgriYearUtil.getAgriYearEnd(agriYear);

        // ==========================================
        // GET ALL ACTIVE WORK ALLOCATION RECORDS
        // FOR THE GIVEN AGRI YEAR
        // ==========================================

        List<TblWorkAllocation> allocations = tblWorkAllocationRepository
                .findByAgriStartAndAgriEndAndIsActiveTrue(
                        agriStart,
                        agriEnd);

        // ==========================================
        // GET ALL ACTIVE DISTRICTS
        // ==========================================

        List<DistrictMaster> districts = districtMasterRepository.findByActiveTrue();

        // ==========================================
        // KERALA TOTALS
        // ==========================================

        BigDecimal totalPlotsWet = BigDecimal.ZERO;
        BigDecimal totalPlotsDry = BigDecimal.ZERO;
        BigDecimal totalPlots = BigDecimal.ZERO;

        BigDecimal totalAreaWet = BigDecimal.ZERO;
        BigDecimal totalAreaDry = BigDecimal.ZERO;
        BigDecimal totalArea = BigDecimal.ZERO;

        // ==========================================
        // DISTRICT DETAILS
        // ==========================================

        List<WorkAllocationProgressResponse.LocationDetails> locationDetails = new ArrayList<>();

        for (DistrictMaster district : districts) {

            // Get allocations belonging to this district
            List<TblWorkAllocation> districtAllocations = allocations.stream()
                    .filter(a -> a.getZone() != null
                            && a.getZone().getDistId() != null
                            && a.getZone().getDistId()
                            .equals(district.getDist_id()))
                    .toList();

            // ======================================
            // VILLAGE RECORDS
            // ======================================

            BigDecimal villageWet = sum(districtAllocations,
                    TblWorkAllocation::getVillageWetArea);

            BigDecimal villageDry = sum(districtAllocations,
                    TblWorkAllocation::getVillageDryArea);

            BigDecimal villageTotal = sum(districtAllocations,
                    TblWorkAllocation::getVillageTotalArea);

            WorkAllocationProgressResponse.VillageRecords villageRecords = WorkAllocationProgressResponse.VillageRecords
                    .builder()
                    .wet(villageWet)
                    .dry(villageDry)
                    .total(villageTotal)
                    .build();

            // ======================================
            // EXCLUDED AREA
            // ======================================

            BigDecimal forestArea = sum(districtAllocations,
                    TblWorkAllocation::getForestAreaA);

            BigDecimal plantationArea = sum(districtAllocations,
                    TblWorkAllocation::getAreaUnderPlant);

            BigDecimal waterBodies = sum(districtAllocations,
                    TblWorkAllocation::getForestExcludeUnclutivate);

            BigDecimal others = sum(districtAllocations,
                    TblWorkAllocation::getKayalExcludeArea);

            WorkAllocationProgressResponse.ExcludedArea excludedArea = WorkAllocationProgressResponse.ExcludedArea
                    .builder()
                    .forestArea(forestArea)
                    .plantationArea(plantationArea)
                    .areaOfWaterBodies(waterBodies)
                    .others(others)
                    .build();

            // ======================================
            // NUMBER OF PLOTS
            // ======================================

            BigDecimal plotsWet = sum(districtAllocations,
                    TblWorkAllocation::getNoOfPlotsWet);

            BigDecimal plotsDry = sum(districtAllocations,
                    TblWorkAllocation::getNoOfPlotsDry);

            BigDecimal plotsTotal = sum(districtAllocations,
                    TblWorkAllocation::getNoOfPlotsTotal);

            WorkAllocationProgressResponse.EstimationPlots estimationPlots = WorkAllocationProgressResponse.EstimationPlots
                    .builder()
                    .wet(plotsWet)
                    .dry(plotsDry)
                    .total(plotsTotal)
                    .build();

            // ======================================
            // AREA AVAILABLE FOR ESTIMATION
            // ======================================

            BigDecimal areaWet = sum(districtAllocations,
                    TblWorkAllocation::getTotalAreaWet);

            BigDecimal areaDry = sum(districtAllocations,
                    TblWorkAllocation::getTotalAreaDry);

            BigDecimal areaTotal = sum(districtAllocations,
                    TblWorkAllocation::getTotalAreaForEstimation);

            WorkAllocationProgressResponse.AreaAvailable areaAvailable = WorkAllocationProgressResponse.AreaAvailable
                    .builder()
                    .wet(areaWet)
                    .dry(areaDry)
                    .total(areaTotal)
                    .build();

            // ======================================
            // DISTRICT ESTIMATION
            // ======================================

            WorkAllocationProgressResponse.DistrictEstimation districtEstimation = WorkAllocationProgressResponse.DistrictEstimation
                    .builder()
                    .noOfPlots(estimationPlots)
                    .areaInCents(areaAvailable)
                    .build();

            // ======================================
            // ADD DISTRICT
            // ======================================

            locationDetails.add(
                    WorkAllocationProgressResponse.LocationDetails
                            .builder()
                            .id(district.getDist_id())
                            .name(district.getDist_name_en())
                            .villageRecords(villageRecords)
                            .excludedArea(excludedArea)
                            .areaAvailableForEstimation(
                                    districtEstimation)
                            .build());

            // ======================================
            // ADD TO KERALA TOTAL
            // ======================================

            totalPlotsWet = totalPlotsWet.add(plotsWet);

            totalPlotsDry = totalPlotsDry.add(plotsDry);

            totalPlots = totalPlots.add(plotsTotal);

            totalAreaWet = totalAreaWet.add(areaWet);

            totalAreaDry = totalAreaDry.add(areaDry);

            totalArea = totalArea.add(areaTotal);
        }

        // ==========================================
        // KERALA PLOT TOTAL
        // ==========================================

        WorkAllocationProgressResponse.EstimationPlots keralaPlots = WorkAllocationProgressResponse.EstimationPlots
                .builder()
                .wet(totalPlotsWet)
                .dry(totalPlotsDry)
                .total(totalPlots)
                .build();

        // ==========================================
        // KERALA AREA TOTAL
        // ==========================================

        WorkAllocationProgressResponse.AreaAvailable keralaArea = WorkAllocationProgressResponse.AreaAvailable
                .builder()
                .wet(totalAreaWet)
                .dry(totalAreaDry)
                .total(totalArea)
                .build();

        // ==========================================
        // FINAL RESPONSE
        // ==========================================

        return WorkAllocationProgressResponse.builder()
                .noOfPlots(keralaPlots)
                .areaAvailableForEstimation(keralaArea)
                .locations(locationDetails)
                .build();
    }
    // TALUK WISE WORK ALLOCATION PROGRESS PASSING DISTRICTID
    // location = TALUKS
    // ==========================================================

    public WorkAllocationProgressResponse getWorkAllocationProgressByDistrict(
            Integer districtId,
            String agriYear) {

        LocalDate agriStart = AgriYearUtil.getAgriYearStart(agriYear);

        LocalDate agriEnd = AgriYearUtil.getAgriYearEnd(agriYear);

        // ======================================================
        // CHECK DISTRICT
        // ======================================================

        DistrictMaster district = districtMasterRepository.findById(Long.valueOf(districtId))
                .orElseThrow(() -> new RuntimeException(
                        "District not found for ID: "
                                + districtId));

        // ======================================================
        // GET WORK ALLOCATION RECORDS
        // FOR THIS DISTRICT + AGRI YEAR
        // ======================================================

        List<TblWorkAllocation> allocations = tblWorkAllocationRepository
                .findByAgriStartAndAgriEndAndIsActiveTrue(
                        agriStart,
                        agriEnd)
                .stream()
                .filter(a -> a.getZone() != null
                        && a.getZone().getDistId() != null
                        && a.getZone().getDistId()
                        .equals(districtId))
                .toList();

        // ======================================================
        // DISTRICT TOTALS
        // ======================================================

        BigDecimal totalPlotsWet = sum(allocations,
                TblWorkAllocation::getNoOfPlotsWet);

        BigDecimal totalPlotsDry = sum(allocations,
                TblWorkAllocation::getNoOfPlotsDry);

        BigDecimal totalPlots = sum(allocations,
                TblWorkAllocation::getNoOfPlotsTotal);

        BigDecimal totalAreaWet = sum(allocations,
                TblWorkAllocation::getTotalAreaWet);

        BigDecimal totalAreaDry = sum(allocations,
                TblWorkAllocation::getTotalAreaDry);

        BigDecimal totalArea = sum(allocations,
                TblWorkAllocation::getTotalAreaForEstimation);

        // ======================================================
        // GROUP BY TALUK
        // ======================================================

        Map<Integer, List<TblWorkAllocation>> talukWise = allocations.stream()
                .filter(a -> a.getZone() != null
                        && a.getZone().getDesTalukId() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getZone().getDesTalukId()));

        List<WorkAllocationProgressResponse.LocationDetails> locations = new ArrayList<>();

        // ======================================================
        // EACH TALUK
        // ======================================================

        for (Map.Entry<Integer, List<TblWorkAllocation>> entry : talukWise.entrySet()) {

            Integer talukId = entry.getKey();

            List<TblWorkAllocation> talukAllocations = entry.getValue();

            // ==================================================
            // VILLAGE RECORDS
            // ==================================================

            BigDecimal villageWet = sum(
                    talukAllocations,
                    TblWorkAllocation::getVillageWetArea);

            BigDecimal villageDry = sum(
                    talukAllocations,
                    TblWorkAllocation::getVillageDryArea);

            BigDecimal villageTotal = sum(
                    talukAllocations,
                    TblWorkAllocation::getVillageTotalArea);

            WorkAllocationProgressResponse.VillageRecords villageRecords = WorkAllocationProgressResponse.VillageRecords
                    .builder()
                    .wet(villageWet)
                    .dry(villageDry)
                    .total(villageTotal)
                    .build();

            // ==================================================
            // EXCLUDED AREA
            // ==================================================

            BigDecimal forestArea = sum(
                    talukAllocations,
                    TblWorkAllocation::getForestAreaA);

            BigDecimal plantationArea = sum(
                    talukAllocations,
                    TblWorkAllocation::getAreaUnderPlant);

            BigDecimal waterBodies = sum(
                    talukAllocations,
                    TblWorkAllocation::getForestExcludeUnclutivate);

            BigDecimal others = sum(
                    talukAllocations,
                    TblWorkAllocation::getKayalExcludeArea);

            WorkAllocationProgressResponse.ExcludedArea excludedArea = WorkAllocationProgressResponse.ExcludedArea
                    .builder()
                    .forestArea(forestArea)
                    .plantationArea(plantationArea)
                    .areaOfWaterBodies(waterBodies)
                    .others(others)
                    .build();

            // ==================================================
            // TALUK NO OF PLOTS
            // ==================================================

            BigDecimal plotsWet = sum(
                    talukAllocations,
                    TblWorkAllocation::getNoOfPlotsWet);

            BigDecimal plotsDry = sum(
                    talukAllocations,
                    TblWorkAllocation::getNoOfPlotsDry);

            BigDecimal plotsTotal = sum(
                    talukAllocations,
                    TblWorkAllocation::getNoOfPlotsTotal);

            WorkAllocationProgressResponse.EstimationPlots estimationPlots = WorkAllocationProgressResponse.EstimationPlots
                    .builder()
                    .wet(plotsWet)
                    .dry(plotsDry)
                    .total(plotsTotal)
                    .build();

            // ==================================================
            // TALUK AREA AVAILABLE
            // ==================================================

            BigDecimal areaWet = sum(
                    talukAllocations,
                    TblWorkAllocation::getTotalAreaWet);

            BigDecimal areaDry = sum(
                    talukAllocations,
                    TblWorkAllocation::getTotalAreaDry);

            BigDecimal areaTotal = sum(
                    talukAllocations,
                    TblWorkAllocation::getTotalAreaForEstimation);

            WorkAllocationProgressResponse.AreaAvailable areaAvailable = WorkAllocationProgressResponse.AreaAvailable
                    .builder()
                    .wet(areaWet)
                    .dry(areaDry)
                    .total(areaTotal)
                    .build();

            // ==================================================
            // TALUK ESTIMATION
            // ==================================================

            WorkAllocationProgressResponse.DistrictEstimation estimation = WorkAllocationProgressResponse.DistrictEstimation
                    .builder()
                    .noOfPlots(estimationPlots)
                    .areaInCents(areaAvailable)
                    .build();

            // ==================================================
            // TALUK NAME
            // ==================================================

            String talukName = talukAllocations.get(0)
                    .getZone()
                    .getDesTalukMaster() != null
                    ? talukAllocations.get(0)
                    .getZone()
                    .getDesTalukMaster()
                    .getDesTalukNameEn()
                    : "Unknown";

            // ==================================================
            // ADD TALUK
            // ==================================================

            locations.add(
                    WorkAllocationProgressResponse.LocationDetails
                            .builder()
                            .id(talukId)
                            .name(talukName)
                            .villageRecords(villageRecords)
                            .excludedArea(excludedArea)
                            .areaAvailableForEstimation(estimation)
                            .build());
        }

        // ======================================================
        // DISTRICT TOTAL PLOTS
        // ======================================================

        WorkAllocationProgressResponse.EstimationPlots districtPlots = WorkAllocationProgressResponse.EstimationPlots
                .builder()
                .wet(totalPlotsWet)
                .dry(totalPlotsDry)
                .total(totalPlots)
                .build();

        // ======================================================
        // DISTRICT TOTAL AREA
        // ======================================================

        WorkAllocationProgressResponse.AreaAvailable districtArea = WorkAllocationProgressResponse.AreaAvailable
                .builder()
                .wet(totalAreaWet)
                .dry(totalAreaDry)
                .total(totalArea)
                .build();

        // ======================================================
        // FINAL DISTRICT RESPONSE
        // ======================================================

        return WorkAllocationProgressResponse.builder()
                .noOfPlots(districtPlots)
                .areaAvailableForEstimation(districtArea)
                .locations(locations)
                .build();
    }

    // work allocation report progress zone and block details while passing talukid
    // work allocation taluk blovck and panchayath:
    @Transactional(readOnly = true)

    public WorkAllocationProgressResponse getWorkAllocationProgressByTaluk(
            Integer talukId,
            String agriYear) {

        LocalDate agriStart = AgriYearUtil.getAgriYearStart(agriYear);

        LocalDate agriEnd = AgriYearUtil.getAgriYearEnd(agriYear);

        // ==========================================================
        // CHECK TALUK
        // ==========================================================

        DesTaluk taluk = desTalukRepository.findById(talukId)
                .orElseThrow(() -> new RuntimeException(
                        "Taluk not found for ID: "
                                + talukId));

        // ==========================================================
        // GET WORK ALLOCATION RECORDS FOR AGRI YEAR
        // ==========================================================

        List<TblWorkAllocation> allocations = tblWorkAllocationRepository
                .findByAgriStartAndAgriEndAndIsActiveTrue(
                        agriStart,
                        agriEnd)
                .stream()
                .filter(a -> a.getZone() != null
                        && a.getZone().getDesTalukId() != null
                        && a.getZone()
                        .getDesTalukId()
                        .equals(talukId))
                .toList();

        // ==========================================================
        // TALUK TOTALS
        // ==========================================================

        BigDecimal totalPlotsWet = sum(
                allocations,
                TblWorkAllocation::getNoOfPlotsWet);

        BigDecimal totalPlotsDry = sum(
                allocations,
                TblWorkAllocation::getNoOfPlotsDry);

        BigDecimal totalPlots = sum(
                allocations,
                TblWorkAllocation::getNoOfPlotsTotal);

        BigDecimal totalAreaWet = sum(
                allocations,
                TblWorkAllocation::getTotalAreaWet);

        BigDecimal totalAreaDry = sum(
                allocations,
                TblWorkAllocation::getTotalAreaDry);

        BigDecimal totalArea = sum(
                allocations,
                TblWorkAllocation::getTotalAreaForEstimation);

        // ==========================================================
        // GROUP BY ZONE AND LBCODE (PANCHAYATH)
        // ==========================================================

        Map<String, List<TblWorkAllocation>> zoneWise = allocations.stream()
                .filter(a -> a.getZone() != null
                        && a.getZone().getZoneId() != null)
                .collect(
                        Collectors.groupingBy(
                                a -> a.getZone().getZoneId() + "_"
                                        + (a.getLbcode() != null
                                        ? a.getLbcode().trim()
                                        : ""),
                                LinkedHashMap::new,
                                Collectors.toList()));

        // ==========================================================
        // LOCATION / ZONE DETAILS
        // ==========================================================

        List<WorkAllocationProgressResponse.LocationDetails> locations = new ArrayList<>();

        for (Map.Entry<String, List<TblWorkAllocation>> entry : zoneWise.entrySet()) {

            List<TblWorkAllocation> zoneAllocations = entry.getValue();

            TblMasterZone zone = zoneAllocations
                    .get(0)
                    .getZone();

            Integer zoneId = zone.getZoneId();

            // ======================================================
            // VILLAGE RECORDS
            // ======================================================

            BigDecimal villageWet = sum(
                    zoneAllocations,
                    TblWorkAllocation::getVillageWetArea);

            BigDecimal villageDry = sum(
                    zoneAllocations,
                    TblWorkAllocation::getVillageDryArea);

            BigDecimal villageTotal = sum(
                    zoneAllocations,
                    TblWorkAllocation::getVillageTotalArea);

            WorkAllocationProgressResponse.VillageRecords villageRecords = WorkAllocationProgressResponse.VillageRecords
                    .builder()
                    .wet(villageWet)
                    .dry(villageDry)
                    .total(villageTotal)
                    .build();

            // ======================================================
            // EXCLUDED AREA
            // ======================================================

            BigDecimal forestArea = sum(
                    zoneAllocations,
                    TblWorkAllocation::getForestAreaA);

            BigDecimal plantationArea = sum(
                    zoneAllocations,
                    TblWorkAllocation::getAreaUnderPlant);

            BigDecimal waterBodies = sum(
                    zoneAllocations,
                    TblWorkAllocation::getForestExcludeUnclutivate);

            BigDecimal others = sum(
                    zoneAllocations,
                    TblWorkAllocation::getKayalExcludeArea);

            WorkAllocationProgressResponse.ExcludedArea excludedArea = WorkAllocationProgressResponse.ExcludedArea
                    .builder()
                    .forestArea(forestArea)
                    .plantationArea(plantationArea)
                    .areaOfWaterBodies(waterBodies)
                    .others(others)
                    .build();

            // ======================================================
            // NUMBER OF PLOTS
            // ======================================================

            BigDecimal plotsWet = sum(
                    zoneAllocations,
                    TblWorkAllocation::getNoOfPlotsWet);

            BigDecimal plotsDry = sum(
                    zoneAllocations,
                    TblWorkAllocation::getNoOfPlotsDry);

            BigDecimal plotsTotal = sum(
                    zoneAllocations,
                    TblWorkAllocation::getNoOfPlotsTotal);

            WorkAllocationProgressResponse.EstimationPlots estimationPlots = WorkAllocationProgressResponse.EstimationPlots
                    .builder()
                    .wet(plotsWet)
                    .dry(plotsDry)
                    .total(plotsTotal)
                    .build();

            // ======================================================
            // AREA AVAILABLE FOR ESTIMATION
            // ======================================================

            BigDecimal areaWet = sum(
                    zoneAllocations,
                    TblWorkAllocation::getTotalAreaWet);

            BigDecimal areaDry = sum(
                    zoneAllocations,
                    TblWorkAllocation::getTotalAreaDry);

            BigDecimal areaTotal = sum(
                    zoneAllocations,
                    TblWorkAllocation::getTotalAreaForEstimation);

            WorkAllocationProgressResponse.AreaAvailable areaAvailable = WorkAllocationProgressResponse.AreaAvailable
                    .builder()
                    .wet(areaWet)
                    .dry(areaDry)
                    .total(areaTotal)
                    .build();

            // ======================================================
            // ESTIMATION
            // ======================================================

            WorkAllocationProgressResponse.DistrictEstimation estimation = WorkAllocationProgressResponse.DistrictEstimation
                    .builder()
                    .noOfPlots(estimationPlots)
                    .areaInCents(areaAvailable)
                    .build();

            // ======================================================
            // BLOCK / LOCALBODY
            // ======================================================

            Integer blockId = null;
            String blockName = null;

            Optional<ZoneLocalbodyBlockMapping> mapping =

                    zoneLocalbodyBlockMappingRepository
                            .findByZoneAndIsValidTrue(zoneId);

            if (mapping.isPresent()) {

                ZoneLocalbodyBlockMapping zm = mapping.get();

                if (zm.getBlockPanchayatMunicipalArea() != null
                        && zm.getBlockDetails() != null) {

                    if (zm.getBlockPanchayatMunicipalArea() == 1) {

                        MasterBlock block = tblMasterBlockRepository
                                .findById(
                                        zm.getBlockDetails())
                                .orElse(null);

                        if (block != null) {

                            blockId = block.getBlockId();

                            blockName = block.getBlockName();
                        }
                    }

                    else {

                        TblLocalBody localbody = localBodyRepository
                                .findById(
                                        zm.getBlockDetails())
                                .orElse(null);

                        if (localbody != null) {

                            blockId = localbody.getLocalbodyId();

                            blockName = formatLocalbodyBlockName(localbody);
                        }
                    }
                }
            }
            // ======================================================
            // PANCHAYATH FROM LBCODE
            // ======================================================

            Integer panchayathId = null;
            String panchayathName = null;

            String lbcode = zoneAllocations.get(0).getLbcode();

            if (lbcode != null && !lbcode.isBlank()) {

                TblLocalBody localbody = localBodyRepository
                        .findByCodeApiAndIsActiveTrue(lbcode)
                        .orElseGet(() -> localBodyRepository.findByCodeApi(lbcode)
                                .orElse(null));

                if (localbody != null) {

                    panchayathId = localbody.getLocalbodyId();

                    panchayathName = localbody.getLocalbodyNameEn();

                    // For Municipalities (type == 2) and Corporations (type == 3), format block
                    // name with Municipality/Corporation suffix
                    if (localbody.getLocalbodyType() != null && localbody.getLocalbodyType() != 1) {
                        blockId = localbody.getLocalbodyId();
                        blockName = formatLocalbodyBlockName(localbody);
                    }
                }
            }

            if (blockName == null || blockName.isBlank()) {
                blockName = (panchayathName != null && !panchayathName.isBlank()) ? panchayathName
                        : zone.getZoneNameEn();
            }
            if (blockId == null) {
                blockId = panchayathId;
            }

            // ======================================================
            // ADD ZONE / PANCHAYATH
            // ======================================================

            locations.add(
                    WorkAllocationProgressResponse.LocationDetails
                            .builder()
                            .id(zoneId)
                            .name(zone.getZoneNameEn())
                            .blockId(blockId)
                            .blockName(blockName)
                            .panchayathId(panchayathId)
                            .panchayathName(panchayathName)
                            .villageRecords(villageRecords)
                            .excludedArea(excludedArea)
                            .areaAvailableForEstimation(estimation)
                            .build());
        }

        // ==========================================================
        // TALUK TOTAL PLOTS
        // ==========================================================

        WorkAllocationProgressResponse.EstimationPlots talukPlots = WorkAllocationProgressResponse.EstimationPlots
                .builder()
                .wet(totalPlotsWet)
                .dry(totalPlotsDry)
                .total(totalPlots)
                .build();

        // ==========================================================
        // TALUK TOTAL AREA
        // ==========================================================

        WorkAllocationProgressResponse.AreaAvailable talukArea = WorkAllocationProgressResponse.AreaAvailable
                .builder()
                .wet(totalAreaWet)
                .dry(totalAreaDry)
                .total(totalArea)
                .build();

        // ==========================================================
        // FINAL RESPONSE
        // ==========================================================

        return WorkAllocationProgressResponse.builder()
                .noOfPlots(talukPlots)
                .areaAvailableForEstimation(talukArea)
                .locations(locations)
                .build();
    }
    // ==========================================
    // COMMON SUM METHOD
    // ==========================================

    private BigDecimal sum(
            List<TblWorkAllocation> allocations,

            Function<TblWorkAllocation, BigDecimal> getter) {

        return allocations.stream()
                .map(getter)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String formatLocalbodyBlockName(TblLocalBody localbody) {
        if (localbody == null || localbody.getLocalbodyNameEn() == null) {
            return "";
        }
        String name = localbody.getLocalbodyNameEn().trim();
        if (localbody.getLocalbodyType() != null) {
            short type = localbody.getLocalbodyType();
            if (type == 2) {
                if (!name.toLowerCase().contains("municipality")) {
                    name = name + " Municipality";
                }
            } else if (type == 3) {
                if (!name.toLowerCase().contains("corporation")) {
                    name = name + " Corporation";
                }
            }
        }
        return name;
    }
}

