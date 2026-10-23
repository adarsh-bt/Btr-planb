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
    private final TblWorkAllocationRepository workAllocationRepository;


    //State and allDistricts

    @Transactional(readOnly = true)
    public ClusterReportResponse getDashboardData(
            String landType,
            YearMonth startMonth,
            YearMonth endMonth,
            String agriYear) {

        // 1. Validate agricultural year
        if (agriYear == null ||
                !agriYear.matches("\\d{4}-\\d{4}")) {
            throw new IllegalArgumentException(
                    "Invalid agricultural year. Expected format: YYYY-YYYY"
            );
        }

        if (startMonth == null) {
            throw new IllegalArgumentException(
                    "startMonth is required (yyyy-MM)"
            );
        }

        if (endMonth == null) {
            endMonth = startMonth;
        }

        if (endMonth.isBefore(startMonth)) {
            throw new IllegalArgumentException(
                    "endMonth cannot be before startMonth"
            );
        }

        // 2. Validate land type
        String normalizedLandType = landType == null
                ? ""
                : landType.trim().toUpperCase(Locale.ROOT);

        if (!normalizedLandType.isEmpty()
                && !normalizedLandType.equals("WET")
                && !normalizedLandType.equals("DRY")) {
            throw new IllegalArgumentException(
                    "Invalid landType. Allowed values: WET, DRY"
            );
        }

        boolean includeWet = normalizedLandType.isEmpty()
                || normalizedLandType.equals("WET");

        boolean includeDry = normalizedLandType.isEmpty()
                || normalizedLandType.equals("DRY");

        // 3. Agricultural year boundaries
        LocalDate agriStart =
                AgriYearUtil.getAgriYearStart(agriYear);

        LocalDate agriEnd =
                AgriYearUtil.getAgriYearEnd(agriYear);

        // 4. Fetch active annual allocations
        List<TblWorkAllocation> allocations =
                workAllocationRepository.findDashboardAllocations(
                        agriStart,
                        agriEnd
                );

        /*
         * expectedByDistrict:
         * index 0 = WET allocation
         * index 1 = DRY allocation
         */
        Map<Long, long[]> expectedByDistrict = new HashMap<>();

        // Store district names to avoid querying the district repository
        // separately for each district.
        Map<Long, String> districtNames = new HashMap<>();

        for (TblWorkAllocation allocation : allocations) {

            if (allocation.getZone() == null
                    || allocation.getZone().getDistrictMaster() == null) {
                continue;
            }

            var district = allocation.getZone().getDistrictMaster();

            if (district.getDist_id() == null) {
                continue;
            }

            Long districtId = district.getDist_id().longValue();

            districtNames.putIfAbsent(
                    districtId,
                    district.getDist_name_en()
            );

            long[] expected = expectedByDistrict.computeIfAbsent(
                    districtId,
                    key -> new long[2]
            );

            if (includeWet && allocation.getTotalWetCluster() != null) {
                expected[0] += allocation.getTotalWetCluster().longValue();
            }

            if (includeDry && allocation.getTotalDryCluster() != null) {
                expected[1] += allocation.getTotalDryCluster().longValue();
            }
        }

        // 5. Fetch actual clusters for the selected month range
        LocalDateTime startDate =
                startMonth.atDay(1).atStartOfDay();

        LocalDateTime endDate =
                endMonth.plusMonths(1)
                        .atDay(1)
                        .atStartOfDay()
                        .minusNanos(1);

        List<ClusterMaster> clusters =
                clusterMasterRepository.getDashboardData(
                        startDate,
                        endDate
                );

        /*
         * statusByDistrict:
         * index 0 = WET completed
         * index 1 = WET ongoing
         * index 2 = WET under review
         * index 3 = DRY completed
         * index 4 = DRY ongoing
         * index 5 = DRY under review
         */
        Map<Long, long[]> statusByDistrict = new HashMap<>();

        // 6. Count actual cluster statuses by district and land type
        for (ClusterMaster cluster : clusters) {

            if (cluster.getZone() == null
                    || cluster.getZone().getDistrictMaster() == null) {
                continue;
            }

            var district = cluster.getZone().getDistrictMaster();

            if (district.getDist_id() == null) {
                continue;
            }

            Long districtId = district.getDist_id().longValue();

            districtNames.putIfAbsent(
                    districtId,
                    district.getDist_name_en()
            );

            // Verify this getter against your KeyPlot entity.
            String clusterLandType = cluster.getKeyPlot() == null
                    ? null
                    : cluster.getKeyPlot().getLandType();

            if (clusterLandType == null) {
                continue;
            }

            clusterLandType = clusterLandType.trim()
                    .toUpperCase(Locale.ROOT);

            boolean isWet = clusterLandType.equals("WET");
            boolean isDry = clusterLandType.equals("DRY");

            if ((!isWet && !isDry)
                    || (isWet && !includeWet)
                    || (isDry && !includeDry)) {
                continue;
            }

            // Verify this getter against your ClusterMaster entity.
            String clusterStatus = cluster.getStatus();

            if (clusterStatus == null) {
                continue;
            }

            clusterStatus = clusterStatus.trim()
                    .toUpperCase(Locale.ROOT)
                    .replaceAll("\\s+", " ");

            long[] status = statusByDistrict.computeIfAbsent(
                    districtId,
                    key -> new long[6]
            );

            if (isWet) {
                switch (clusterStatus) {
                    case "COMPLETED" -> status[0]++;
                    case "ONGOING" -> status[1]++;
                    case "UNDER REVIEW", "UNDERVIEW" -> status[2]++;
                    default -> {
                        // Ignore unrecognized statuses.
                    }
                }
            } else {
                switch (clusterStatus) {
                    case "COMPLETED" -> status[3]++;
                    case "ONGOING" -> status[4]++;
                    case "UNDER REVIEW", "UNDERVIEW" -> status[5]++;
                    default -> {
                        // Ignore unrecognized statuses.
                    }
                }
            }
        }

        // 7. Build district-level response
        Map<String, SubDetailsClusterStatusResponse> districtMap =
                new LinkedHashMap<>();

        long totalExpectedClusters = 0L;
        long totalCompleted = 0L;
        long totalOngoing = 0L;
        long totalUnderView = 0L;
        long totalNotStarted = 0L;

        // Include districts with allocations or actual clusters.
        Set<Long> districtIds = new LinkedHashSet<>();
        districtIds.addAll(expectedByDistrict.keySet());
        districtIds.addAll(statusByDistrict.keySet());

        for (Long districtId : districtIds) {

            long[] expected = expectedByDistrict.getOrDefault(
                    districtId,
                    new long[2]
            );

            long[] status = statusByDistrict.getOrDefault(
                    districtId,
                    new long[6]
            );

            long wetCompleted = includeWet ? status[0] : 0L;
            long wetOngoing = includeWet ? status[1] : 0L;
            long wetUnderView = includeWet ? status[2] : 0L;

            long dryCompleted = includeDry ? status[3] : 0L;
            long dryOngoing = includeDry ? status[4] : 0L;
            long dryUnderView = includeDry ? status[5] : 0L;

            // Not started = annual allocation minus actual statuses.
            long wetNotStarted = includeWet
                    ? Math.max(
                    0L,
                    expected[0]
                            - wetCompleted
                            - wetOngoing
                            - wetUnderView
            )
                    : 0L;

            long dryNotStarted = includeDry
                    ? Math.max(
                    0L,
                    expected[1]
                            - dryCompleted
                            - dryOngoing
                            - dryUnderView
            )
                    : 0L;

            // District status totals
            long districtCompleted =
                    wetCompleted + dryCompleted;

            long districtOngoing =
                    wetOngoing + dryOngoing;

            long districtUnderView =
                    wetUnderView + dryUnderView;

            long districtNotStarted =
                    wetNotStarted + dryNotStarted;

            /*
             * IMPORTANT:
             * totalCluster belongs inside each district entry.
             * It is the annual WET + DRY allocation for that district,
             * respecting the requested landType filter.
             */
            long districtTotalCluster = 0L;

            if (includeWet) {
                districtTotalCluster += expected[0];
            }

            if (includeDry) {
                districtTotalCluster += expected[1];
            }

            // Add each district's allocation exactly once to the outer total.
            totalExpectedClusters += districtTotalCluster;

            totalCompleted += districtCompleted;
            totalOngoing += districtOngoing;
            totalUnderView += districtUnderView;
            totalNotStarted += districtNotStarted;

            SubDetailsClusterStatusResponse detail =
                    new SubDetailsClusterStatusResponse();

            detail.setId(districtId);
            detail.setTotalCluster(districtTotalCluster);

            detail.setWetCompleted(wetCompleted);
            detail.setWetOngoing(wetOngoing);
            detail.setWetNotStarted(wetNotStarted);
            detail.setWetUnderView(wetUnderView);

            detail.setDryCompleted(dryCompleted);
            detail.setDryOngoing(dryOngoing);
            detail.setDryNotStarted(dryNotStarted);
            detail.setDryUnderView(dryUnderView);

            String districtName = districtNames.getOrDefault(
                    districtId,
                    String.valueOf(districtId)
            );

            districtMap.put(districtName, detail);
        }

        // 8. Final response
        ClusterReportResponse response = new ClusterReportResponse();

        response.setTotalCluster(totalExpectedClusters);
        response.setCompleted(totalCompleted);
        response.setOngoing(totalOngoing);
        response.setNotStarted(totalNotStarted);
        response.setUnderView(totalUnderView);
        response.setAllSubDetails(districtMap);

        return response;
    }

    //based on DistrictId and corresponding taluk list
    @Transactional(readOnly = true)
    public ClusterReportResponse getTalukWiseDashboardData(
            Integer districtId,
            String landType,
            YearMonth startMonth,
            YearMonth endMonth,
            String agriYear) {

        // 1. Resolve the agricultural year dates.
        // Keep your existing agricultural-year selection logic here.


        LocalDate agriStart = AgriYearUtil.getAgriYearStart(agriYear);
        LocalDate agriEnd = AgriYearUtil.getAgriYearEnd(agriYear);

        // 2. Calculate the selected month range.
        LocalDate startDate = startMonth != null
                ? startMonth.atDay(1)
                : agriStart;

        LocalDate endDate = endMonth != null
                ? endMonth.atEndOfMonth()
                : agriEnd;

        // Prevent an invalid range from being queried.
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start month must not be after end month");
        }

        // 3. Fetch annual allocations for this district.
        List<TblWorkAllocation> allocations =
                tblWorkAllocationRepository.findTalukDashboardAllocations(
                        districtId, agriStart, agriEnd);

        // Key: taluk ID
        // Value: [allocated wet clusters, allocated dry clusters]
        Map<Long, long[]> expectedByTaluk = new HashMap<>();

        // Key: taluk ID
        // Value: taluk name
        Map<Long, String> talukNames = new LinkedHashMap<>();

        for (TblWorkAllocation allocation : allocations) {

            if (allocation.getZone() == null
                    || allocation.getZone().getDesTalukMaster() == null) {
                continue;
            }

            var taluk = allocation.getZone().getDesTalukMaster();

            // getDesTalukId() is primitive: do not compare it with null.
            long talukId = taluk.getDesTalukId();

            talukNames.putIfAbsent(
                    talukId,
                    taluk.getDesTalukNameEn());

            long[] expected = expectedByTaluk.computeIfAbsent(
                    talukId, key -> new long[2]);

            long allocatedWet = allocation.getTotalWetCluster() == null
                    ? 0L
                    : allocation.getTotalWetCluster().longValue();

            long allocatedDry = allocation.getTotalDryCluster() == null
                    ? 0L
                    : allocation.getTotalDryCluster().longValue();

            expected[0] += allocatedWet;
            expected[1] += allocatedDry;
        }

        // 4. Fetch actual cluster records for the selected date range.
        List<ClusterMaster> clusters =
                clusterMasterRepository.getTalukWiseDashboardData(
                        districtId, startDate.atStartOfDay(), endDate.atStartOfDay());

        // Key: taluk ID
        // Value: [dry completed, dry ongoing, dry not started,
        //         dry under review, wet completed, wet ongoing,
        //         wet not started, wet under review]
        Map<Long, long[]> statusByTaluk = new HashMap<>();

        for (ClusterMaster cluster : clusters) {

            if (cluster.getZone() == null
                    || cluster.getZone().getDesTalukMaster() == null) {
                continue;
            }

            var taluk = cluster.getZone().getDesTalukMaster();
            long talukId = taluk.getDesTalukId();

            talukNames.putIfAbsent(
                    talukId,
                    taluk.getDesTalukNameEn());

            long[] counts = statusByTaluk.computeIfAbsent(
                    talukId, key -> new long[8]);

            String status = cluster.getStatus() == null
                    ? ""
                    : cluster.getStatus().trim().toLowerCase(Locale.ROOT);

            String clusterLandType = cluster.getKeyPlot() == null
                    || cluster.getKeyPlot().getLandType() == null
                    ? ""
                    : cluster.getKeyPlot().getLandType()
                    .trim().toLowerCase(Locale.ROOT);

            boolean isWet = clusterLandType.equals("wet");
            boolean isDry = clusterLandType.equals("dry");

            // Ignore unsupported land types.
            if (!isWet && !isDry) {
                continue;
            }

            // Respect the optional landType filter.
            if (landType != null && !landType.isBlank()
                    && !landType.equalsIgnoreCase("ALL")
                    && !clusterLandType.equalsIgnoreCase(landType)) {
                continue;
            }

            int index;

            if (isDry) {
                index = 0;
            } else {
                index = 4;
            }

            if (status.equals("completed")) {
                counts[index]++;
            } else if (status.equals("on going")
                    || status.equals("ongoing")) {
                counts[index + 1]++;
            } else if (status.equals("not started")) {
                counts[index + 2]++;
            } else if (status.equals("under review")) {
                counts[index + 3]++;
            }
        }

        // 5. Build the response.
        Map<String, SubDetailsClusterStatusResponse> allSubDetails =
                new LinkedHashMap<>();

        long totalDryCompleted = 0;
        long totalDryOngoing = 0;
        long totalDryNotStarted = 0;
        long totalDryUnderView = 0;

        long totalWetCompleted = 0;
        long totalWetOngoing = 0;
        long totalWetNotStarted = 0;
        long totalWetUnderView = 0;

        long grandTotalCluster = 0;

        for (Map.Entry<Long, String> entry : talukNames.entrySet()) {

            Long talukId = entry.getKey();

            long[] expected = expectedByTaluk.getOrDefault(
                    talukId, new long[2]);

            long[] counts = statusByTaluk.getOrDefault(
                    talukId, new long[8]);

            long dryCompleted = counts[0];
            long dryOngoing = counts[1];
            long dryUnderView = counts[3];

            long wetCompleted = counts[4];
            long wetOngoing = counts[5];
            long wetUnderView = counts[7];

            // Not-started is the allocated count minus
            // clusters already completed, ongoing or under review.
            long dryNotStarted = Math.max(
                    0L,
                    expected[1] - dryCompleted - dryOngoing - dryUnderView);

            long wetNotStarted = Math.max(
                    0L,
                    expected[0] - wetCompleted - wetOngoing - wetUnderView);

            // Total allocation for this taluk, independent of the month filter.
            long talukTotalCluster = expected[0] + expected[1];

            SubDetailsClusterStatusResponse detail =
                    new SubDetailsClusterStatusResponse(
                            talukId,
                            dryCompleted,
                            dryOngoing,
                            dryNotStarted,
                            dryUnderView,
                            wetCompleted,
                            wetOngoing,
                            wetNotStarted,
                            wetUnderView,
                            talukTotalCluster
                    );

            allSubDetails.put(String.valueOf(talukId), detail);

            totalDryCompleted += dryCompleted;
            totalDryOngoing += dryOngoing;
            totalDryNotStarted += dryNotStarted;
            totalDryUnderView += dryUnderView;

            totalWetCompleted += wetCompleted;
            totalWetOngoing += wetOngoing;
            totalWetNotStarted += wetNotStarted;
            totalWetUnderView += wetUnderView;

            grandTotalCluster += talukTotalCluster;
        }

        // 6. Populate your existing outer response DTO.
        ClusterReportResponse response = new ClusterReportResponse();

        response.setTotalCluster(grandTotalCluster);
        response.setCompleted(totalDryCompleted + totalWetCompleted);
        response.setOngoing(totalDryOngoing + totalWetOngoing);
        response.setNotStarted(totalDryNotStarted + totalWetNotStarted);
        response.setUnderView(totalDryUnderView + totalWetUnderView);
        response.setAllSubDetails(allSubDetails);

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
            String search,
            String agriYear
    ) {

        if (talukId == null) {
            throw new IllegalArgumentException("Taluk ID is required");
        }

        if (startMonth == null) {
            throw new IllegalArgumentException("Start month is required");
        }

        if (endMonth != null && endMonth.isBefore(startMonth)) {
            throw new IllegalArgumentException(
                    "End month cannot be before start month"
            );
        }

        if (agriYear == null || !agriYear.matches("\\d{4}-\\d{4}")) {
            throw new IllegalArgumentException(
                    "Agricultural year must be in YYYY-YYYY format"
            );
        }

        String[] years = agriYear.split("-");

        int firstYear = Integer.parseInt(years[0]);
        int secondYear = Integer.parseInt(years[1]);

        if (secondYear != firstYear + 1) {
            throw new IllegalArgumentException(
                    "Invalid agricultural year: " + agriYear
            );
        }

        if (landType != null) {
            landType = landType.trim().toUpperCase();

            if (!landType.equals("WET") && !landType.equals("DRY")) {
                throw new IllegalArgumentException(
                        "Land type must be WET or DRY"
                );
            }
        }

        boolean includeWet = landType == null || landType.equals("WET");
        boolean includeDry = landType == null || landType.equals("DRY");

        String searchText = search == null
                ? ""
                : search.trim().toLowerCase();


        // --------------------------------------------------
        // 2. MONTH FILTER: ACTUAL CLUSTER STATUSES
        // --------------------------------------------------

        LocalDateTime startDate = startMonth
                .atDay(1)
                .atStartOfDay();

        YearMonth effectiveEndMonth =
                endMonth != null ? endMonth : startMonth;

        LocalDateTime endDate = effectiveEndMonth
                .atEndOfMonth()
                .atTime(23, 59, 59);

        List<ClusterMaster> clusters =
                clusterMasterRepository.getZoneWiseDashboardData(
                        talukId,
                        startDate,
                        endDate
                );


        // --------------------------------------------------
        // 3. AGRICULTURAL YEAR: WORK ALLOCATION TOTALS
        // --------------------------------------------------

        LocalDate agriStart =
                AgriYearUtil.getAgriYearStart(agriYear);

        LocalDate agriEnd =
                AgriYearUtil.getAgriYearEnd(agriYear);

        List<TblWorkAllocation> allocations =
                workAllocationRepository.findZoneDashboardAllocations(
                        talukId,
                        agriStart,
                        agriEnd
                );


        // --------------------------------------------------
        // 4. INITIALIZE RESPONSE AND MAPS
        // --------------------------------------------------

        ClusterReportResponse response = new ClusterReportResponse();

        // Use zone ID as the key to avoid merging duplicate zone names.
        Map<Long, BlockZoneWiseClusterStatusResponse> zoneMap =
                new LinkedHashMap<>();

        // Index 0 = WET, index 1 = DRY.
        Map<Long, long[]> expectedByZone = new LinkedHashMap<>();

        long totalExpected = 0L;


        // --------------------------------------------------
        // 5. PROCESS WORK ALLOCATION
        //    TOTAL IS NOT MONTH-FILTERED
        // --------------------------------------------------

        for (TblWorkAllocation allocation : allocations) {

            if (allocation.getZone() == null
                    || allocation.getZone().getZoneId() == null) {
                continue;
            }

            var zone = allocation.getZone();

            Long zoneId = zone.getZoneId().longValue();

            String zoneName = zone.getZoneNameEn();

            if (zoneName == null) {
                zoneName = "UNKNOWN";
            }

            // Apply search to the zone name.
            if (!searchText.isEmpty()
                    && !zoneName.toLowerCase().contains(searchText)) {
                continue;
            }

            // Resolve block/local-body information.
            Integer blockId = null;
            String blockName = null;

            List<ZoneLocalbodyBlockMapping> mappings =
                    zoneLocalbodyBlockMappingRepository.findByZone(
                            zone.getZoneId()
                    );

            if (mappings != null && !mappings.isEmpty()) {

                ZoneLocalbodyBlockMapping mapping = mappings.get(0);

                if (mapping.getBlockDetails() != null) {

                    if (mapping.getBlockPanchayatMunicipalArea() == 1) {

                        MasterBlock block =
                                masterBlockRepository.findById(
                                        mapping.getBlockDetails()
                                ).orElse(null);

                        if (block != null) {
                            blockId = block.getBlockId();
                            blockName = block.getBlockName();
                        }

                    } else if (
                            mapping.getBlockPanchayatMunicipalArea() == 2
                    ) {

                        TblLocalBody localBody =
                                tblLocalBodyRepository.findById(
                                        mapping.getBlockDetails()
                                ).orElse(null);

                        if (localBody != null) {
                            blockId = localBody.getLocalbodyId();
                            blockName = localBody.getLocalbodyNameEn();
                        }
                    }
                }
            }

            // Initialize this zone only once.
            zoneMap.putIfAbsent(
                    zoneId,
                    new BlockZoneWiseClusterStatusResponse(
                            zoneId,
                            blockId,
                            blockName,
                            0L,  // wet completed
                            0L,  // dry completed
                            0L,  // wet ongoing
                            0L,  // dry ongoing
                            0L,  // wet not started
                            0L,  // dry not started
                            0L,  // wet under review
                            0L,   // dry under review
                            0l
                    )
            );

            long[] expected = expectedByZone.computeIfAbsent(
                    zoneId,
                    key -> new long[2]
            );

            // Sum allocated cluster counts by land type.
            // Do not apply startMonth/endMonth here.
            if (includeWet && allocation.getTotalWetCluster() != null) {

                expected[0] +=
                        allocation.getTotalWetCluster().longValue();
            }

            if (includeDry && allocation.getTotalDryCluster() != null) {

                expected[1] +=
                        allocation.getTotalDryCluster().longValue();
            }
        }


        // --------------------------------------------------
        // 6. SET EACH ZONE'S TOTAL FROM WORK ALLOCATION
        // --------------------------------------------------

        for (Map.Entry<Long, long[]> entry : expectedByZone.entrySet()) {

            Long zoneId = entry.getKey();

            long[] expected = entry.getValue();

            BlockZoneWiseClusterStatusResponse stats =
                    zoneMap.get(zoneId);

            if (stats != null) {

                // This is the fix for the incorrect zone Total.
                stats.setTotalCluster(
                        expected[0] + expected[1]
                );

                totalExpected += expected[0] + expected[1];
            }
        }


        // --------------------------------------------------
        // 7. COUNT ACTUAL STATUSES USING MONTH + LAND TYPE
        // --------------------------------------------------

        long completed = 0L;
        long ongoing = 0L;
        long underView = 0L;

        for (ClusterMaster cluster : clusters) {

            if (cluster.getZone() == null
                    || cluster.getZone().getZoneId() == null
                    || cluster.getStatus() == null
                    || cluster.getKeyPlot() == null) {
                continue;
            }

            Long zoneId = cluster.getZone().getZoneId().longValue();

            String zoneName = cluster.getZone().getZoneNameEn();

            if (zoneName == null) {
                zoneName = "UNKNOWN";
            }

            if (!searchText.isEmpty()
                    && !zoneName.toLowerCase().contains(searchText)) {
                continue;
            }

            String clusterLandType = cluster.getKeyPlot().getLandType();

            if (clusterLandType == null) {
                continue;
            }

            clusterLandType = clusterLandType.trim().toUpperCase();

            if (!clusterLandType.equals("WET")
                    && !clusterLandType.equals("DRY")) {
                continue;
            }

            if (clusterLandType.equals("WET") && !includeWet) {
                continue;
            }

            if (clusterLandType.equals("DRY") && !includeDry) {
                continue;
            }

            // Ignore cluster records for zones outside the allocation
            // result/search scope. Do not invent an allocated total.
            BlockZoneWiseClusterStatusResponse stats =
                    zoneMap.get(zoneId);

            if (stats == null) {
                continue;
            }

            boolean wet = clusterLandType.equals("WET");

            switch (cluster.getStatus().trim().toLowerCase()) {

                case "completed":

                    completed++;

                    if (wet) {
                        stats.setWetCompleted(
                                stats.getWetCompleted() + 1L
                        );
                    } else {
                        stats.setDryCompleted(
                                stats.getDryCompleted() + 1L
                        );
                    }

                    break;

                case "on going":

                    ongoing++;

                    if (wet) {
                        stats.setWetOngoing(
                                stats.getWetOngoing() + 1L
                        );
                    } else {
                        stats.setDryOngoing(
                                stats.getDryOngoing() + 1L
                        );
                    }

                    break;

                case "under review":

                    underView++;

                    if (wet) {
                        stats.setWetUnderView(
                                stats.getWetUnderView() + 1L
                        );
                    } else {
                        stats.setDryUnderView(
                                stats.getDryUnderView() + 1L
                        );
                    }

                    break;

                // Not Started is calculated from allocation balance.
                // Do not count stored Not Started records separately.
            }
        }


        // --------------------------------------------------
        // 8. CALCULATE NOT STARTED FROM THE ALLOCATION BALANCE
        // --------------------------------------------------

        long notStarted = 0L;

        for (Map.Entry<Long, BlockZoneWiseClusterStatusResponse> entry
                : zoneMap.entrySet()) {

            Long zoneId = entry.getKey();

            BlockZoneWiseClusterStatusResponse stats =
                    entry.getValue();

            long[] expected = expectedByZone.getOrDefault(
                    zoneId,
                    new long[]{0L, 0L}
            );

            long wetNotStarted = Math.max(
                    0L,
                    expected[0]
                            - stats.getWetCompleted()
                            - stats.getWetOngoing()
                            - stats.getWetUnderView()
            );

            long dryNotStarted = Math.max(
                    0L,
                    expected[1]
                            - stats.getDryCompleted()
                            - stats.getDryOngoing()
                            - stats.getDryUnderView()
            );

            stats.setWetNotStarted(wetNotStarted);
            stats.setDryNotStarted(dryNotStarted);

            notStarted += wetNotStarted + dryNotStarted;
        }


        // --------------------------------------------------
        // 9. BUILD RESPONSE
        // --------------------------------------------------

        response.setTotalCluster(totalExpected);
        response.setCompleted(completed);
        response.setOngoing(ongoing);
        response.setNotStarted(notStarted);
        response.setUnderView(underView);

        // If your DTO expects Map<String, ...>, see the note below.
        Map<String, BlockZoneWiseClusterStatusResponse> resultMap =
                new LinkedHashMap<>();

        for (Map.Entry<Long, BlockZoneWiseClusterStatusResponse> entry
                : zoneMap.entrySet()) {

            BlockZoneWiseClusterStatusResponse stats =
                    entry.getValue();

            resultMap.put(
                    String.valueOf(entry.getKey()),
                    stats
            );
        }

        response.setAllSubDetails(resultMap);

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

                totalCompleted += 1;

                district.setWetCompleted(
                        district.getWetCompleted() + 1);
            }

            if ("DRY".equalsIgnoreCase(clusterLandType) && includeDry) {

                totalCompleted += 1;

                district.setDryCompleted(
                        district.getDryCompleted() + 1);
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

                totalCompleted += 1;

                taluk.setWetCompleted(
                        taluk.getWetCompleted() + 1);
            }

            if ("DRY".equalsIgnoreCase(clusterLandType) && includeDry) {

                totalCompleted += 1;

                taluk.setDryCompleted(
                        taluk.getDryCompleted() + 1);
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

//        LocalDate startDate =
//                agriStart.atStartOfDay();

//        LocalDateTime endDate =
//                agriEnd.atTime(23, 59, 59);

        List<ClusterMaster> clusters =
                clusterMasterRepository.getZoneWiseDashboardDataByAgriYear(
                        talukId,
                        agriStart,
                        agriEnd);

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

                totalCompleted++;

                zone.setWetCompleted(
                        zone.getWetCompleted() + 1);
            }

            if ("DRY".equalsIgnoreCase(clusterLandType) && includeDry) {

                totalCompleted++;

                zone.setDryCompleted(
                        zone.getDryCompleted() + 1);
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

        LocalDate agriStart =
                AgriYearUtil.getAgriYearStart(agriYear);

        LocalDate agriEnd =
                AgriYearUtil.getAgriYearEnd(agriYear);

        // ==========================================
        // GET ALL ACTIVE WORK ALLOCATION RECORDS
        // FOR THE GIVEN AGRI YEAR
        // ==========================================

        List<TblWorkAllocation> allocations =
                tblWorkAllocationRepository
                        .findByAgriStartAndAgriEndAndIsActiveTrue(
                                agriStart,
                                agriEnd
                        );

        // ==========================================
        // GET ALL ACTIVE DISTRICTS
        // ==========================================

        List<DistrictMaster> districts =
                districtMasterRepository.findByActiveTrue();

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

        List<WorkAllocationProgressResponse.LocationDetails>
                locationDetails = new ArrayList<>();

        for (DistrictMaster district : districts) {

            // Get allocations belonging to this district
            List<TblWorkAllocation> districtAllocations =
                    allocations.stream()
                            .filter(a ->
                                    a.getZone() != null
                                            && a.getZone().getDistId() != null
                                            && a.getZone().getDistId()
                                            .equals(district.getDist_id())
                            )
                            .toList();

            // ======================================
            // VILLAGE RECORDS
            // ======================================

            BigDecimal villageWet =
                    sum(districtAllocations,
                            TblWorkAllocation::getVillageWetArea);

            BigDecimal villageDry =
                    sum(districtAllocations,
                            TblWorkAllocation::getVillageDryArea);

            BigDecimal villageTotal =
                    sum(districtAllocations,
                            TblWorkAllocation::getVillageTotalArea);

            WorkAllocationProgressResponse.VillageRecords
                    villageRecords =
                    WorkAllocationProgressResponse.VillageRecords
                            .builder()
                            .wet(villageWet)
                            .dry(villageDry)
                            .total(villageTotal)
                            .build();

            // ======================================
            // EXCLUDED AREA
            // ======================================

            BigDecimal forestArea =
                    sum(districtAllocations,
                            TblWorkAllocation::getForestAreaA);

            BigDecimal plantationArea =
                    sum(districtAllocations,
                            TblWorkAllocation::getAreaUnderPlant);

            BigDecimal waterBodies =
                    sum(districtAllocations,
                            TblWorkAllocation::
                                    getForestExcludeUnclutivate);

            BigDecimal others =
                    sum(districtAllocations,
                            TblWorkAllocation::getKayalExcludeArea);

            WorkAllocationProgressResponse.ExcludedArea
                    excludedArea =
                    WorkAllocationProgressResponse.ExcludedArea
                            .builder()
                            .forestArea(forestArea)
                            .plantationArea(plantationArea)
                            .areaOfWaterBodies(waterBodies)
                            .others(others)
                            .build();

            // ======================================
            // NUMBER OF PLOTS
            // ======================================

            BigDecimal plotsWet =
                    sum(districtAllocations,
                            TblWorkAllocation::getNoOfPlotsWet);

            BigDecimal plotsDry =
                    sum(districtAllocations,
                            TblWorkAllocation::getNoOfPlotsDry);

            BigDecimal plotsTotal =
                    sum(districtAllocations,
                            TblWorkAllocation::getNoOfPlotsTotal);

            WorkAllocationProgressResponse.EstimationPlots
                    estimationPlots =
                    WorkAllocationProgressResponse.EstimationPlots
                            .builder()
                            .wet(plotsWet)
                            .dry(plotsDry)
                            .total(plotsTotal)
                            .build();

            // ======================================
            // AREA AVAILABLE FOR ESTIMATION
            // ======================================

            BigDecimal areaWet =
                    sum(districtAllocations,
                            TblWorkAllocation::getTotalAreaWet);

            BigDecimal areaDry =
                    sum(districtAllocations,
                            TblWorkAllocation::getTotalAreaDry);

            BigDecimal areaTotal =
                    sum(districtAllocations,
                            TblWorkAllocation::
                                    getTotalAreaForEstimation);

            WorkAllocationProgressResponse.AreaAvailable
                    areaAvailable =
                    WorkAllocationProgressResponse.AreaAvailable
                            .builder()
                            .wet(areaWet)
                            .dry(areaDry)
                            .total(areaTotal)
                            .build();

            // ======================================
            // DISTRICT ESTIMATION
            // ======================================

            WorkAllocationProgressResponse.DistrictEstimation
                    districtEstimation =
                    WorkAllocationProgressResponse.DistrictEstimation
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
                                    districtEstimation
                            )
                            .build()
            );

            // ======================================
            // ADD TO KERALA TOTAL
            // ======================================

            totalPlotsWet =
                    totalPlotsWet.add(plotsWet);

            totalPlotsDry =
                    totalPlotsDry.add(plotsDry);

            totalPlots =
                    totalPlots.add(plotsTotal);

            totalAreaWet =
                    totalAreaWet.add(areaWet);

            totalAreaDry =
                    totalAreaDry.add(areaDry);

            totalArea =
                    totalArea.add(areaTotal);
        }

        // ==========================================
        // KERALA PLOT TOTAL
        // ==========================================

        WorkAllocationProgressResponse.EstimationPlots
                keralaPlots =
                WorkAllocationProgressResponse.EstimationPlots
                        .builder()
                        .wet(totalPlotsWet)
                        .dry(totalPlotsDry)
                        .total(totalPlots)
                        .build();

        // ==========================================
        // KERALA AREA TOTAL
        // ==========================================

        WorkAllocationProgressResponse.AreaAvailable
                keralaArea =
                WorkAllocationProgressResponse.AreaAvailable
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
    // TALUK WISE WORK ALLOCATION PROGRESS PASSING  DISTRICTID
// location = TALUKS
// ==========================================================

    public WorkAllocationProgressResponse getWorkAllocationProgressByDistrict(
            Integer districtId,
            String agriYear) {

        LocalDate agriStart =
                AgriYearUtil.getAgriYearStart(agriYear);

        LocalDate agriEnd =
                AgriYearUtil.getAgriYearEnd(agriYear);

        // ======================================================
        // CHECK DISTRICT
        // ======================================================

        DistrictMaster district =
                districtMasterRepository.findById(Long.valueOf(districtId))
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "District not found for ID: "
                                                + districtId));

        // ======================================================
        // GET WORK ALLOCATION RECORDS
        // FOR THIS DISTRICT + AGRI YEAR
        // ======================================================

        List<TblWorkAllocation> allocations =
                tblWorkAllocationRepository
                        .findByAgriStartAndAgriEndAndIsActiveTrue(
                                agriStart,
                                agriEnd
                        )
                        .stream()
                        .filter(a ->
                                a.getZone() != null
                                        && a.getZone().getDistId() != null
                                        && a.getZone().getDistId()
                                        .equals(districtId)
                        )
                        .toList();

        // ======================================================
        // DISTRICT TOTALS
        // ======================================================

        BigDecimal totalPlotsWet =
                sum(allocations,
                        TblWorkAllocation::getNoOfPlotsWet);

        BigDecimal totalPlotsDry =
                sum(allocations,
                        TblWorkAllocation::getNoOfPlotsDry);

        BigDecimal totalPlots =
                sum(allocations,
                        TblWorkAllocation::getNoOfPlotsTotal);

        BigDecimal totalAreaWet =
                sum(allocations,
                        TblWorkAllocation::getTotalAreaWet);

        BigDecimal totalAreaDry =
                sum(allocations,
                        TblWorkAllocation::getTotalAreaDry);

        BigDecimal totalArea =
                sum(allocations,
                        TblWorkAllocation::getTotalAreaForEstimation);

        // ======================================================
        // GROUP BY TALUK
        // ======================================================

        Map<Integer, List<TblWorkAllocation>> talukWise =
                allocations.stream()
                        .filter(a ->
                                a.getZone() != null
                                        && a.getZone().getDesTalukId() != null
                        )
                        .collect(Collectors.groupingBy(
                                a -> a.getZone().getDesTalukId()
                        ));

        List<WorkAllocationProgressResponse.LocationDetails>
                locations = new ArrayList<>();

        // ======================================================
        // EACH TALUK
        // ======================================================

        for (Map.Entry<Integer, List<TblWorkAllocation>> entry
                : talukWise.entrySet()) {

            Integer talukId = entry.getKey();

            List<TblWorkAllocation> talukAllocations =
                    entry.getValue();

            // ==================================================
            // VILLAGE RECORDS
            // ==================================================

            BigDecimal villageWet =
                    sum(
                            talukAllocations,
                            TblWorkAllocation::getVillageWetArea
                    );

            BigDecimal villageDry =
                    sum(
                            talukAllocations,
                            TblWorkAllocation::getVillageDryArea
                    );

            BigDecimal villageTotal =
                    sum(
                            talukAllocations,
                            TblWorkAllocation::getVillageTotalArea
                    );

            WorkAllocationProgressResponse.VillageRecords
                    villageRecords =
                    WorkAllocationProgressResponse.VillageRecords
                            .builder()
                            .wet(villageWet)
                            .dry(villageDry)
                            .total(villageTotal)
                            .build();

            // ==================================================
            // EXCLUDED AREA
            // ==================================================

            BigDecimal forestArea =
                    sum(
                            talukAllocations,
                            TblWorkAllocation::getForestAreaA
                    );

            BigDecimal plantationArea =
                    sum(
                            talukAllocations,
                            TblWorkAllocation::getAreaUnderPlant
                    );

            BigDecimal waterBodies =
                    sum(
                            talukAllocations,
                            TblWorkAllocation::getForestExcludeUnclutivate
                    );

            BigDecimal others =
                    sum(
                            talukAllocations,
                            TblWorkAllocation::getKayalExcludeArea
                    );

            WorkAllocationProgressResponse.ExcludedArea
                    excludedArea =
                    WorkAllocationProgressResponse.ExcludedArea
                            .builder()
                            .forestArea(forestArea)
                            .plantationArea(plantationArea)
                            .areaOfWaterBodies(waterBodies)
                            .others(others)
                            .build();

            // ==================================================
            // TALUK NO OF PLOTS
            // ==================================================

            BigDecimal plotsWet =
                    sum(
                            talukAllocations,
                            TblWorkAllocation::getNoOfPlotsWet
                    );

            BigDecimal plotsDry =
                    sum(
                            talukAllocations,
                            TblWorkAllocation::getNoOfPlotsDry
                    );

            BigDecimal plotsTotal =
                    sum(
                            talukAllocations,
                            TblWorkAllocation::getNoOfPlotsTotal
                    );

            WorkAllocationProgressResponse.EstimationPlots
                    estimationPlots =
                    WorkAllocationProgressResponse.EstimationPlots
                            .builder()
                            .wet(plotsWet)
                            .dry(plotsDry)
                            .total(plotsTotal)
                            .build();

            // ==================================================
            // TALUK AREA AVAILABLE
            // ==================================================

            BigDecimal areaWet =
                    sum(
                            talukAllocations,
                            TblWorkAllocation::getTotalAreaWet
                    );

            BigDecimal areaDry =
                    sum(
                            talukAllocations,
                            TblWorkAllocation::getTotalAreaDry
                    );

            BigDecimal areaTotal =
                    sum(
                            talukAllocations,
                            TblWorkAllocation::getTotalAreaForEstimation
                    );

            WorkAllocationProgressResponse.AreaAvailable
                    areaAvailable =
                    WorkAllocationProgressResponse.AreaAvailable
                            .builder()
                            .wet(areaWet)
                            .dry(areaDry)
                            .total(areaTotal)
                            .build();

            // ==================================================
            // TALUK ESTIMATION
            // ==================================================

            WorkAllocationProgressResponse.DistrictEstimation
                    estimation =
                    WorkAllocationProgressResponse.DistrictEstimation
                            .builder()
                            .noOfPlots(estimationPlots)
                            .areaInCents(areaAvailable)
                            .build();

            // ==================================================
            // TALUK NAME
            // ==================================================

            String talukName =
                    talukAllocations.get(0)
                            .getZone()
                            .getDesTalukMaster() != null
                            ?
                            talukAllocations.get(0)
                                    .getZone()
                                    .getDesTalukMaster()
                                    .getDesTalukNameEn()
                            :
                            "Unknown";

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
                            .build()
            );
        }

        // ======================================================
        // DISTRICT TOTAL PLOTS
        // ======================================================

        WorkAllocationProgressResponse.EstimationPlots
                districtPlots =
                WorkAllocationProgressResponse.EstimationPlots
                        .builder()
                        .wet(totalPlotsWet)
                        .dry(totalPlotsDry)
                        .total(totalPlots)
                        .build();

        // ======================================================
        // DISTRICT TOTAL AREA
        // ======================================================

        WorkAllocationProgressResponse.AreaAvailable
                districtArea =
                WorkAllocationProgressResponse.AreaAvailable
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
    //work allocation report progress zone and block details while passing  talukid
    @Transactional(readOnly = true)

    public WorkAllocationProgressResponse getWorkAllocationProgressByTaluk(
            Integer talukId,
            String agriYear) {

        LocalDate agriStart =
                AgriYearUtil.getAgriYearStart(agriYear);

        LocalDate agriEnd =
                AgriYearUtil.getAgriYearEnd(agriYear);

        // ==========================================================
        // CHECK TALUK
        // ==========================================================

        DesTaluk taluk =
                desTalukRepository.findById(talukId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Taluk not found for ID: "
                                                + talukId));

        // ==========================================================
        // GET WORK ALLOCATION RECORDS FOR AGRI YEAR
        // ==========================================================

        List<TblWorkAllocation> allocations =
                tblWorkAllocationRepository
                        .findByAgriStartAndAgriEndAndIsActiveTrue(
                                agriStart,
                                agriEnd
                        )
                        .stream()
                        .filter(a ->
                                a.getZone() != null
                                        && a.getZone().getDesTalukId() != null
                                        && a.getZone()
                                        .getDesTalukId()
                                        .equals(talukId)
                        )
                        .toList();
        // ==========================================================
// TALUK TOTALS
// ==========================================================

        BigDecimal totalPlotsWet =
                sum(
                        allocations,
                        TblWorkAllocation::getNoOfPlotsWet
                );

        BigDecimal totalPlotsDry =
                sum(
                        allocations,
                        TblWorkAllocation::getNoOfPlotsDry
                );

        BigDecimal totalPlots =
                sum(
                        allocations,
                        TblWorkAllocation::getNoOfPlotsTotal
                );

        BigDecimal totalAreaWet =
                sum(
                        allocations,
                        TblWorkAllocation::getTotalAreaWet
                );

        BigDecimal totalAreaDry =
                sum(
                        allocations,
                        TblWorkAllocation::getTotalAreaDry
                );

        BigDecimal totalArea =
                sum(
                        allocations,
                        TblWorkAllocation::getTotalAreaForEstimation
                );

// ==========================================================
// GROUP BY ZONE
// ==========================================================

        Map<Integer, List<TblWorkAllocation>> zoneWise =
                allocations.stream()
                        .filter(a ->
                                a.getZone() != null
                                        && a.getZone().getZoneId() != null
                        )
                        .collect(
                                Collectors.groupingBy(
                                        a -> a.getZone().getZoneId()
                                )
                        );

// ==========================================================
// LOCATION / ZONE DETAILS
// ==========================================================

        List<WorkAllocationProgressResponse.LocationDetails>
                locations = new ArrayList<>();

        for (Map.Entry<Integer, List<TblWorkAllocation>> entry
                : zoneWise.entrySet()) {

            Integer zoneId = entry.getKey();

            List<TblWorkAllocation> zoneAllocations =
                    entry.getValue();

            TblMasterZone zone =
                    zoneAllocations
                            .get(0)
                            .getZone();
            // ======================================================
            // VILLAGE RECORDS
            // ======================================================

            BigDecimal villageWet =
                    sum(
                            zoneAllocations,
                            TblWorkAllocation::getVillageWetArea
                    );

            BigDecimal villageDry =
                    sum(
                            zoneAllocations,
                            TblWorkAllocation::getVillageDryArea
                    );

            BigDecimal villageTotal =
                    sum(
                            zoneAllocations,
                            TblWorkAllocation::getVillageTotalArea
                    );

            WorkAllocationProgressResponse.VillageRecords
                    villageRecords =
                    WorkAllocationProgressResponse.VillageRecords
                            .builder()
                            .wet(villageWet)
                            .dry(villageDry)
                            .total(villageTotal)
                            .build();

            // ======================================================
            // EXCLUDED AREA
            // ======================================================

            BigDecimal forestArea =
                    sum(
                            zoneAllocations,
                            TblWorkAllocation::getForestAreaA
                    );

            BigDecimal plantationArea =
                    sum(
                            zoneAllocations,
                            TblWorkAllocation::getAreaUnderPlant
                    );

            BigDecimal waterBodies =
                    sum(
                            zoneAllocations,
                            TblWorkAllocation::
                                    getForestExcludeUnclutivate
                    );

            BigDecimal others =
                    sum(
                            zoneAllocations,
                            TblWorkAllocation::getKayalExcludeArea
                    );

            WorkAllocationProgressResponse.ExcludedArea
                    excludedArea =
                    WorkAllocationProgressResponse.ExcludedArea
                            .builder()
                            .forestArea(forestArea)
                            .plantationArea(plantationArea)
                            .areaOfWaterBodies(waterBodies)
                            .others(others)
                            .build();

            // ======================================================
            // NUMBER OF PLOTS
            // ======================================================

            BigDecimal plotsWet =
                    sum(
                            zoneAllocations,
                            TblWorkAllocation::getNoOfPlotsWet
                    );

            BigDecimal plotsDry =
                    sum(
                            zoneAllocations,
                            TblWorkAllocation::getNoOfPlotsDry
                    );

            BigDecimal plotsTotal =
                    sum(
                            zoneAllocations,
                            TblWorkAllocation::getNoOfPlotsTotal
                    );

            WorkAllocationProgressResponse.EstimationPlots
                    estimationPlots =
                    WorkAllocationProgressResponse.EstimationPlots
                            .builder()
                            .wet(plotsWet)
                            .dry(plotsDry)
                            .total(plotsTotal)
                            .build();

            // ======================================================
            // AREA AVAILABLE FOR ESTIMATION
            // ======================================================

            BigDecimal areaWet =
                    sum(
                            zoneAllocations,
                            TblWorkAllocation::getTotalAreaWet
                    );

            BigDecimal areaDry =
                    sum(
                            zoneAllocations,
                            TblWorkAllocation::getTotalAreaDry
                    );

            BigDecimal areaTotal =
                    sum(
                            zoneAllocations,
                            TblWorkAllocation::
                                    getTotalAreaForEstimation
                    );

            WorkAllocationProgressResponse.AreaAvailable
                    areaAvailable =
                    WorkAllocationProgressResponse.AreaAvailable
                            .builder()
                            .wet(areaWet)
                            .dry(areaDry)
                            .total(areaTotal)
                            .build();

            // ======================================================
            // ESTIMATION
            // ======================================================

            WorkAllocationProgressResponse.DistrictEstimation
                    estimation =
                    WorkAllocationProgressResponse.DistrictEstimation
                            .builder()
                            .noOfPlots(estimationPlots)
                            .areaInCents(areaAvailable)
                            .build();

            // ======================================================
            // BLOCK / LOCALBODY
            // ======================================================

            Integer blockId = null;
            String blockName = null;

//            Optional<ZoneLocalbodyBlockMapping> mapping =
//
//                    zoneLocalbodyBlockMappingRepository
//                            .findByZoneAndIsValidTrue(zoneId);
//
//            if (mapping.isPresent()) {
//
//                ZoneLocalbodyBlockMapping zm =
//                        mapping.get();
//
//                if (zm.getBlockPanchayatMunicipalArea() != null
//                        && zm.getBlockDetails() != null) {
//
//                    // --------------------------------------------------
//                    // 1 = BLOCK
//                    // --------------------------------------------------
//
//                    if (zm.getBlockPanchayatMunicipalArea() == 1) {
//
//                        MasterBlock block =
//                                tblMasterBlockRepository
//                                        .findById(
//                                                zm.getBlockDetails()
//                                        )
//                                        .orElse(null);
//
//                        if (block != null) {
//
//                            blockId =
//                                    block.getBlockId();
//
//                            blockName =
//                                    block.getBlockName();
//                        }
//                    }
//
//                    // --------------------------------------------------
//                    // 2 = LOCAL BODY
//                    // --------------------------------------------------
//
//                    else if (
//                            zm.getBlockPanchayatMunicipalArea() == 2
//                    ) {
//
//                        TblLocalBody localbody =
//                                localBodyRepository
//                                        .findById(
//                                                zm.getBlockDetails()
//                                        )
//                                        .orElse(null);
//
//                        if (localbody != null) {
//
//                            blockId =
//                                    localbody.getLocalbodyId();
//
//                            blockName =
//                                    localbody.getLocalbodyNameEn();
//                        }
//                    }
//                }
//            }
            String lbcode = zoneAllocations.get(0).getLbcode();

            if (lbcode != null && !lbcode.isBlank()) {

                TblLocalBody localbody =
                        localBodyRepository
                                .findByCodeApiAndIsActiveTrue(lbcode)
                                .orElse(null);

                if (localbody != null) {

                    blockId =
                            localbody.getLocalbodyId();

                    blockName =
                            localbody.getLocalbodyNameEn();
                }
            }

            // ======================================================
            // ADD ZONE
            // ======================================================

            locations.add(
                    WorkAllocationProgressResponse.LocationDetails
                            .builder()
                            .id(zoneId)
                            .name(zone.getZoneNameEn())
                            .blockId(blockId)
                            .blockName(blockName)
                            .villageRecords(villageRecords)
                            .excludedArea(excludedArea)
                            .areaAvailableForEstimation(estimation)
                            .build()
            );
        }

        // ==========================================================
        // TALUK TOTAL PLOTS
        // ==========================================================

        WorkAllocationProgressResponse.EstimationPlots
                talukPlots =
                WorkAllocationProgressResponse.EstimationPlots
                        .builder()
                        .wet(totalPlotsWet)
                        .dry(totalPlotsDry)
                        .total(totalPlots)
                        .build();

        // ==========================================================
        // TALUK TOTAL AREA
        // ==========================================================

        WorkAllocationProgressResponse.AreaAvailable
                talukArea =
                WorkAllocationProgressResponse.AreaAvailable
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
}

