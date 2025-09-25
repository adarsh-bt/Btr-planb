package cdti.aidea.earas.service;

import cdti.aidea.earas.config.FormEntryClient;
import cdti.aidea.earas.contract.FormEntryDto.AvailableCcePlotRejectionRequest;
import cdti.aidea.earas.contract.FormEntryDto.CceCropDetailsResponse;
import cdti.aidea.earas.contract.FormEntryDto.Response;
import cdti.aidea.earas.contract.RequestsDTOs.KeyPlotDetailsRequest;
import cdti.aidea.earas.contract.RequestsDTOs. KeyPlotRejectRequest;
import cdti.aidea.earas.contract.Response.ClusterFormRowDTO;
import cdti.aidea.earas.contract.Response.KeyPlotDetailsResponse;
import cdti.aidea.earas.contract.Response.KeyPlotOwnerDetailsResponse;
import cdti.aidea.earas.contract.Response.SidePlotDTO;
import cdti.aidea.earas.model.Btr_models.*;
import cdti.aidea.earas.model.Btr_models.Masters.TblLocalBody;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterVillage;
import cdti.aidea.earas.model.Btr_models.Masters.TblZoneRevenueVillageMapping;
import cdti.aidea.earas.repository.Btr_repo.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.modelmapper.config.Configuration.AccessLevel.PRIVATE;
//import jakarta.transaction.Transactional;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
@Slf4j
public class KeyPlots_Service {
    private final ModelMapper modelMapper;
    private final UserZoneAssignmentRepositoty userZoneAssignmentRepositoty;
    private  final KeyPlotsRepository keyPlotsRepository;
    private final TblZoneRevenueVillageMappingRepository tblZoneRevenueVillageMappingRepository;
    private final TblMasterVillageRepository tblMasterVillageRepository;
    private final TblBtrDataRepository tblBtrDataRepository;
    private final LocalBodyRepository localBodyRepository;
    private final TblBtrRepository tblBtrRepository;
    private final LandTypeClassificationService landTypeClassificationService;
    private final ClusterMasterRepository clusterMasterRepository;
    private final ClusterFormDataRepository clusterFormDataRepository;
    private final CceCropService cceCropService;
    private final FormEntryClient formEntryClient;

    @PersistenceContext
    private EntityManager entityManager;


//    public Object getExistingKeyPlots(UUID userId,Long zone_id) {
//        var user = userZoneAssignmentRepositoty.findByUserId(userId);
//        LocalDate cutoffDate = LocalDate.now().minusYears(2);
//        System.out.println("user found ");
//        int currentYear = LocalDate.now().getYear();
//        LocalDate today = LocalDate.now();
//
//         List<KeyPlots> existingKeyPlots = keyPlotsRepository.findValidKeyPlots(
//                user.get().getTblMasterZone().getZoneId(),
//                currentYear,
//                today
//        );
//
//        System.out.println("ssss ns");
//        if (existingKeyPlots.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        Map<String, List<KeyPlots>> groupedByPanchayat = existingKeyPlots.stream()
//                .collect(Collectors.groupingBy(kp -> kp.getLocalbody().getLocalbodyNameEn()));
//
//        List<Map<String, Object>> clusterList = new ArrayList<>();
//        int globalSlNo = 1;
//        for (Map.Entry<String, List<KeyPlots>> entry : groupedByPanchayat.entrySet()) {
//            String panchayath = entry.getKey();
//            List<KeyPlots> plots = entry.getValue();
//
//            List<Map<String, Object>> wetSamples = new ArrayList<>();
//            List<Map<String, Object>> drySamples = new ArrayList<>();
//            double wetArea = 0, dryArea = 0;
//            int wetClusters = 0, dryClusters = 0;
//            int classIntervalWet = 0, classIntervalDry = 0;
//
//            for (KeyPlots kp : plots) {
//                TblBtrDataOld plot = kp.getBtrDataOld();
//                Map<String, Object> row = new HashMap<>();
//                row.put("id", plot.getId());
//                row.put("plot_id", kp.getId());
//
//                row.put("no",globalSlNo++);
//                row.put("panchayth", kp.getLocalbody().getLocalbodyNameEn());
//                row.put("Sy. No", plot.getResvno() + "/" + plot.getResbdno());
//                Double roundedArea = BigDecimal.valueOf(plot.getArea())
//                        .setScale(2, RoundingMode.HALF_UP)
//                        .doubleValue();
//                row.put("Area (Cents)", roundedArea);
//
//                row.put("Village/Block", plot.getBcode());
////                row.put("Random No", plot.getId().intValue());
////                row.put("AgreStartYear", kp.getAgriStartYear());
////                row.put("AgreEndYear", kp.getAgriEndYear());
//
//                if ("Wet".equalsIgnoreCase(kp.getLandType())) {
//                    wetSamples.add(row);
//                    wetArea += plot.getArea();
//                    wetClusters++;
//                    classIntervalWet = kp.getIntervals();
//                } else if ("Dry".equalsIgnoreCase(kp.getLandType())) {
//                    drySamples.add(row);
//                    dryArea += plot.getArea();
//                    dryClusters++;
//                    classIntervalDry = kp.getIntervals();
//                }
//            }
//
//            Map<String, Object> clusterData = new HashMap<>();
//            clusterData.put("panchayath", panchayath);
//            clusterData.put("wetSamples", wetSamples);
//            clusterData.put("drySamples", drySamples);
//            clusterData.put("wetarea", Math.round(wetArea));
//            clusterData.put("dryarea", Math.round(dryArea));
//            clusterData.put("totalarea", Math.round(wetArea + dryArea));
//
//            clusterList.add(clusterData);
//        }
//
//        return clusterList;
//    }

//    public Object generateNewKeyPlots(UUID userId) {
//        var user = userZoneAssignmentRepositoty.findByUserId(userId);
//        LocalDate cutoffDate = LocalDate.now().minusYears(2);
//
//        List<KeyPlots> existingKeyPlots = keyPlotsRepository.findValidKeyPlots(
//                Long.valueOf(user.get().getTblMasterZone().getZoneId()),
//                cutoffDate
//        );
//
//        HashSet<KeyPlots> excludedIdSet = new HashSet<>(existingKeyPlots);
//        var zoneRevenueList = tblZoneRevenueVillageMappingRepository.findByZone(user.get().getTblMasterZone().getZoneId());
//
//        List<Integer> villageIds = zoneRevenueList.stream()
//                .map(TblZoneRevenueVillageMapping::getRevenueVillage)
//                .toList();
//        List<TblMasterVillage> villageList = tblMasterVillageRepository.findAllById(villageIds);
//        List<Integer> lsgcodes = villageList.stream().map(TblMasterVillage::getLsgCode).toList();
//
//        List<TblBtrDataOld> allData = tblBtrDataOldRepository.findAllByLsgcodeIn(lsgcodes);
//        Map<String, List<TblBtrDataOld>> panchayathDataMap = allData.stream()
//                .collect(Collectors.groupingBy(TblBtrDataOld::getLbcode));
//
//        List<String> lbcodeList = allData.stream()
//                .map(TblBtrDataOld::getLbcode)
//                .distinct()
//                .toList();
//
//        List<TblLocalBody> localBodies = localBodyRepository.findAllByCodeApiIn(lbcodeList);
//        Map<String, String> localBodyNameMap = localBodies.stream()
//                .collect(Collectors.toMap(TblLocalBody::getCodeApi, TblLocalBody::getLocalbodyNameMal));
//
//        // Phase 1: Area Stats
//        List<Map<String, Object>> areaStats = new ArrayList<>();
//        double totalZoneArea = 0;
//
//        for (Map.Entry<String, List<TblBtrDataOld>> entry : panchayathDataMap.entrySet()) {
//            String lbcode = entry.getKey();
//            double wetArea = 0, dryArea = 0;
//            int wetPlots = 0, dryPlots = 0;
//
//            for (TblBtrDataOld data : entry.getValue()) {
//                double area = data.getArea();
//                String landType = data.getLtype().trim().toUpperCase();
//
//                if ("W".equals(landType)) {
//                    wetArea += area;
//                    wetPlots++;
//                } else if ("D".equals(landType)) {
//                    dryArea += area;
//                    dryPlots++;
//                }
//            }
//
//            double totalArea = wetArea + dryArea;
//            totalZoneArea += totalArea;
//
//            Map<String, Object> stats = new HashMap<>();
//            stats.put("lbcode", lbcode);
//            stats.put("wetArea", wetArea);
//            stats.put("dryArea", dryArea);
//            stats.put("wetPlots", wetPlots);
//            stats.put("dryPlots", dryPlots);
//            stats.put("totalArea", totalArea);
//            areaStats.add(stats);
//        }
//
//        areaStats.sort(Comparator.comparing(s -> localBodyNameMap.getOrDefault((String) s.get("lbcode"), "")));
//
//        // Phase 2: Clustering
//        List<Map<String, Object>> clusterList = new ArrayList<>();
//        int totalClusters = 100, clusterAssigned = 0;
//
//        for (int i = 0; i < areaStats.size(); i++) {
//            Map<String, Object> stats = areaStats.get(i);
//            String lbcode = (String) stats.get("lbcode");
//            double wetArea = (double) stats.get("wetArea");
//            double dryArea = (double) stats.get("dryArea");
//            double totalArea = (double) stats.get("totalArea");
//            int wetPlots = (int) stats.get("wetPlots");
//            int dryPlots = (int) stats.get("dryPlots");
//
//            int clusters = (i == areaStats.size() - 1)
//                    ? totalClusters - clusterAssigned
//                    : (int) Math.round((totalArea / totalZoneArea) * totalClusters);
//            clusterAssigned += clusters;
//
//            int wetClusters = (int) Math.round(clusters * (wetArea / totalArea));
//            int dryClusters = clusters - wetClusters;
//            int classIntervalWet = (wetClusters > 0 && wetPlots > 0) ? (int) Math.round((double) wetPlots / wetClusters) : 0;
//            int classIntervalDry = (dryClusters > 0 && dryPlots > 0) ? (int) Math.round((double) dryPlots / dryClusters) : 0;
//
//            List<TblBtrDataOld> wetList = panchayathDataMap.get(lbcode).stream()
//                    .filter(d -> "W".equalsIgnoreCase(d.getLtype()))
//                    .filter(d -> !excludedIdSet.contains(d.getId()))
//                    .toList();
//
//            List<TblBtrDataOld> dryList = panchayathDataMap.get(lbcode).stream()
//                    .filter(d -> "D".equalsIgnoreCase(d.getLtype()))
//                    .filter(d -> !excludedIdSet.contains(d.getId()))
//                    .toList();
//
//            Map<String, Integer> randomStartMap = Map.of("01108", 242, "01113", 3032);
//            int randomStart = randomStartMap.getOrDefault(lbcode, 0);
//
//            List<Map<String, Object>> wetSamples = getSystematicSample(wetList, wetClusters, classIntervalWet, randomStart, localBodyNameMap);
//            List<Map<String, Object>> drySamples = getSystematicSample(dryList, dryClusters, classIntervalDry, randomStart, localBodyNameMap);
//
//            TblLocalBody localbody = localBodies.stream()
//                    .filter(lb -> lb.getCodeApi().equals(lbcode))
//                    .findFirst()
//                    .orElseThrow(() -> new EntityNotFoundException("Localbody not found for code: " + lbcode));
//
//            saveSamples(wetSamples, classIntervalWet, user.get().getTblMasterZone().getZoneId(), "Wet", localbody,userId, clusterCounter);
//            saveSamples(drySamples, classIntervalDry, user.get().getTblMasterZone().getZoneId(), "Dry", localbody,userId, clusterCounter);
//
//            Map<String, Object> panchayathCluster = new HashMap<>();
//            panchayathCluster.put("panchayath", localBodyNameMap.get(lbcode));
//            panchayathCluster.put("totalarea", (double) Math.round(totalArea));
//            panchayathCluster.put("wetarea", (double) Math.round(wetArea));
//            panchayathCluster.put("dryarea", (double) Math.round(dryArea));
//            panchayathCluster.put("totalClusters", clusters);
//            panchayathCluster.put("wetClusters", wetClusters);
//            panchayathCluster.put("dryClusters", dryClusters);
//            panchayathCluster.put("classIntervalWet", classIntervalWet);
//            panchayathCluster.put("classIntervalDry", classIntervalDry);
//            panchayathCluster.put("wetSamples", wetSamples);
//            panchayathCluster.put("drySamples", drySamples);
//
//            clusterList.add(panchayathCluster);
//        }
//
//        return clusterList;
//    }


// working code but existing and genrtae same

//    public Object KeyplotsFormationOldbtr(UUID userId) {
//        var user = userZoneAssignmentRepositoty.findByUserId(userId);
//        AtomicInteger globalSlNo = new AtomicInteger(1);
//
//        AtomicInteger clusterCounter = new AtomicInteger(1);
//
//        int currentYear = LocalDate.now().getYear();
//
//        List<KeyPlots> existingKeyPlots = keyPlotsRepository.findValidKeyPlots(
//                user.get().getTblMasterZone().getZoneId(),
//                currentYear - 1,
//                LocalDate.now()
//        );
//
//// Only exclude plots used last year and not rejected
//        Set<Long> excludedPlotIds = existingKeyPlots.stream()
//                .filter(kp -> Boolean.FALSE.equals(kp.getIsRejected()))
//                .map(kp -> kp.getBtrDataOld().getId())
//                .collect(Collectors.toSet());
//
//        if (!existingKeyPlots.isEmpty()) {
//            Map<String, List<KeyPlots>> groupedByPanchayat = existingKeyPlots.stream()
//                    .collect(Collectors.groupingBy(kp -> kp.getLocalbody().getLocalbodyNameEn()));
//
//
//            List<Map<String, Object>> clusterList = new ArrayList<>();
//
//            for (Map.Entry<String, List<KeyPlots>> entry : groupedByPanchayat.entrySet()) {
//                String panchayath = entry.getKey();
//                List<KeyPlots> plots = entry.getValue();
//
//                List<Map<String, Object>> wetSamples = new ArrayList<>();
//                List<Map<String, Object>> drySamples = new ArrayList<>();
//                double wetArea = 0, dryArea = 0;
//                int wetClusters = 0, dryClusters = 0;
//                int classIntervalWet = 0, classIntervalDry = 0;
//
//                for (KeyPlots kp : plots) {
//                    TblBtrDataOld plot = kp.getBtrDataOld();
//                    Map<String, Object> row = new HashMap<>();
//                    row.put("id", plot.getId());
//                    row.put("plot_id", kp.getId());
//                    row.put("panchayth", kp.getLocalbody().getLocalbodyNameEn());
//                    row.put("Sy. No", plot.getResvno() + "/" + plot.getResbdno());
//                    Double roundedArea = BigDecimal.valueOf(plot.getArea())
//                            .setScale(2, RoundingMode.HALF_UP)
//                            .doubleValue();
//                    row.put("Area (Cents)", roundedArea);
//
//
//                    row.put("Village/Block", plot.getBcode());
////                    row.put("Random No", plot.getId().intValue()); // Approximation
////                    row.put("Interval", kp.getIntervals());
////                    row.put("Land Type", kp.getLandType());
////                    row.put("AgreStartYear", kp.getAgriStartYear());
////                    row.put("AgreEndYear", kp.getAgriEndYear());
//
//                    if ("Wet".equalsIgnoreCase(kp.getLandType())) {
//                        wetSamples.add(row);
//                        wetArea += Math.round(plot.getArea() * 100.0) / 100.0;
//                        wetClusters++;
//                        classIntervalWet = kp.getIntervals();
//                    } else if ("Dry".equalsIgnoreCase(kp.getLandType())) {
//                        drySamples.add(row);
//                        dryArea += Math.round(plot.getArea() * 100.0) / 100.0;
//                        dryClusters++;
//                        classIntervalDry = kp.getIntervals();
//                    }
//                }
//
//                Map<String, Object> clusterData = new HashMap<>();
//                clusterData.put("panchayath", panchayath);
//                clusterData.put("wetSamples", wetSamples);
//                clusterData.put("drySamples", drySamples);
////                clusterData.put("wetClusters", wetClusters);
////                clusterData.put("dryClusters", dryClusters);
////                clusterData.put("totalClusters", wetClusters + dryClusters);
//                clusterData.put("wetarea", Math.round(wetArea));
//                clusterData.put("dryarea", Math.round(dryArea));
//                clusterData.put("totalarea", Math.round(wetArea + dryArea));
////                clusterData.put("classIntervalWet", classIntervalWet);
////                clusterData.put("classIntervalDry", classIntervalDry);
//
//                clusterList.add(clusterData);
//            }
//
//            Map<String, Object> response = new HashMap<>();
//
//            return clusterList;
//        }
//
//
////        LocalDate cutoffDate = LocalDate.now().minusYears(2);
////        List<Long> excludedPlotIds = keyPlotsRepository.findRecentlyUsedBtrIds(cutoffDate);
//
//
////        Set<Long> excludedIdSet = new HashSet<>(excludedPlotIds);
//        HashSet<KeyPlots> excludedIdSet = new HashSet<>(existingKeyPlots);
//        var zoneRevenueList = tblZoneRevenueVillageMappingRepository.findByZone(user.get().getTblMasterZone().getZoneId());
//
//        List<Integer> villageIds = zoneRevenueList.stream()
//                .map(TblZoneRevenueVillageMapping::getRevenueVillage)
//                .toList();
//        List<TblMasterVillage> villageList = tblMasterVillageRepository.findAllById(villageIds);
//        List<Integer> lsgcodes = villageList.stream().map(TblMasterVillage::getLsgCode).toList();
//
//        List<TblBtrDataOld> allData = tblBtrDataOldRepository.findAllByLsgcodeIn(lsgcodes);
//
//        Map<String, List<TblBtrDataOld>> panchayathDataMap = allData.stream()
//                .collect(Collectors.groupingBy(TblBtrDataOld::getLbcode));
//
//        List<String> lbcodeList = allData.stream()
//                .map(TblBtrDataOld::getLbcode)
//                .distinct()
//                .collect(Collectors.toList());
//
//        List<TblLocalBody> localBodies = localBodyRepository.findAllByCodeApiIn(lbcodeList);
//        Map<String, String> localBodyNameMap = new HashMap<>();
//        localBodies.forEach(localBody -> localBodyNameMap.put(localBody.getCodeApi(), localBody.getLocalbodyNameEn()));
//
//        // First pass: calculate total area per panchayath
//        List<Map<String, Object>> areaStats = new ArrayList<>();
//        double totalZoneArea = 0;
//
//        for (Map.Entry<String, List<TblBtrDataOld>> entry : panchayathDataMap.entrySet()) {
//            String lbcode = entry.getKey();
//            List<TblBtrDataOld> panchayathData = entry.getValue();
//
//            double wetArea = 0, dryArea = 0;
//            int wetPlots = 0, dryPlots = 0;
//
//            for (TblBtrDataOld data : panchayathData) {
//                double area = data.getArea();
//                String landType = data.getLtype().trim().toUpperCase();
//
//                if ("W".equals(landType)) {
//                    wetArea += area;
//                    wetPlots++;
//                } else if ("D".equals(landType)) {
//                    dryArea += area;
//                    dryPlots++;
//                }
//            }
//
//            double totalArea = wetArea + dryArea;
//            totalZoneArea += totalArea;
//
//            Map<String, Object> stats = new HashMap<>();
//            stats.put("lbcode", lbcode);
//            stats.put("wetArea", wetArea);
//            stats.put("dryArea", dryArea);
//            stats.put("wetPlots", wetPlots);
//            stats.put("dryPlots", dryPlots);
//            stats.put("totalArea", totalArea);
//            areaStats.add(stats);
//        }
//
//        // Sort alphabetically by panchayath name
//        areaStats.sort(Comparator.comparing(s -> localBodyNameMap.getOrDefault((String) s.get("lbcode"), "")));
//
//        // Second pass: calculate clusters and intervals
//        List<Map<String, Object>> clusterList = new ArrayList<>();
//        int totalClusters = 100;
//        int clusterAssigned = 0;
//        List<KeyPlots> allWetKeyPlots = new ArrayList<>();
//        List<KeyPlots> allDryKeyPlots = new ArrayList<>();
//        List<Map<String, Object>> wetSamplesMapList = new ArrayList<>();
//        List<Map<String, Object>> drySamplesMapList = new ArrayList<>();
//
//        for (int i = 0; i < areaStats.size(); i++) {
//            Map<String, Object> stats = areaStats.get(i);
//            String lbcode = (String) stats.get("lbcode");
//
//            double wetArea = (double) stats.get("wetArea");
//            double dryArea = (double) stats.get("dryArea");
//            double totalArea = (double) stats.get("totalArea");
//            int wetPlots = (int) stats.get("wetPlots");
//            int dryPlots = (int) stats.get("dryPlots");
//
//            int clusters = (i == areaStats.size() - 1)
//                    ? totalClusters - clusterAssigned
//                    : (int) Math.round((totalArea / totalZoneArea) * totalClusters);
//            clusterAssigned += clusters;
//
//            int wetClusters = (int) Math.round(clusters * (wetArea / totalArea));
//            int dryClusters = clusters - wetClusters;
//
//            int classIntervalWet = (wetClusters > 0 && wetPlots > 0) ? (int) Math.round((double) wetPlots / wetClusters) : 0;
//            int classIntervalDry = (dryClusters > 0 && dryPlots > 0) ? (int) Math.round((double) dryPlots / dryClusters) : 0;
//
//            // Sample selection
//            List<TblBtrDataOld> panchayathData = panchayathDataMap.get(lbcode);
//
//            List<TblBtrDataOld> wetList = panchayathData.stream()
//                    .filter(d -> "W".equalsIgnoreCase(d.getLtype().trim().toUpperCase()))
//                    .filter(d -> !excludedPlotIds.contains(d.getId())) // ✅ using your exclusion logic
//                    .collect(Collectors.toList());
//
//            List<TblBtrDataOld> dryList = panchayathData.stream()
//                    .filter(d -> "D".equalsIgnoreCase(d.getLtype().trim().toUpperCase()))
//                    .filter(d -> !excludedPlotIds.contains(d.getId())) // ✅ using your exclusion logic
//                    .collect(Collectors.toList());
//
//
////            int randomStart = 242;
//            Map<String, Integer> randomStartMap = new HashMap<>();
//            randomStartMap.put("01108", 242);    // കിളിമാനൂർ
//            randomStartMap.put("01113", 3032);   // നാഗരൂർ
//
//// Add more lbcode -> random start pairs as needed
//            int randomStart = randomStartMap.getOrDefault(lbcode, 0); // Fallback to 0 if not found
//
//            List<Map<String, Object>> wetSamples = getSystematicSample(
//                    wetList, wetClusters, classIntervalWet, randomStart, localBodyNameMap, globalSlNo
//            );
//
//            List<Map<String, Object>> drySamples = getSystematicSample(
//                    dryList, dryClusters, classIntervalDry, randomStart, localBodyNameMap, globalSlNo
//            );
//
//
//
//            // You can merge samples or keep separately
//            Map<String, Object> panchayathCluster = new HashMap<>();
//            panchayathCluster.put("panchayath", localBodyNameMap.get(lbcode));
//            panchayathCluster.put("totalarea", (double) Math.round(totalArea));
//            panchayathCluster.put("wetarea", (double) Math.round(wetArea));
//            panchayathCluster.put("dryarea", (double) Math.round(dryArea));
//            panchayathCluster.put("totalClusters", clusters);
//            panchayathCluster.put("wetClusters", wetClusters);
//            panchayathCluster.put("dryClusters", dryClusters);
//            panchayathCluster.put("classIntervalWet", classIntervalWet);
//            panchayathCluster.put("classIntervalDry", classIntervalDry);
//            panchayathCluster.put("wetSamples", wetSamples);
//            panchayathCluster.put("drySamples", drySamples);
//            TblLocalBody localbody = localBodies.stream()
//                    .filter(lb -> lb.getCodeApi().equals(lbcode))
//                    .findFirst()
//                    .orElseThrow(() -> new EntityNotFoundException("Localbody not found for code: " + lbcode));
//
//
//            wetSamplesMapList.addAll(wetSamples);
//            drySamplesMapList.addAll(drySamples);
//            System.out.println("zone id "+user.get().getTblMasterZone().getZoneId());
//            List<KeyPlots> wetKeyPlots = prepareKeyPlots(wetSamples,  classIntervalWet, user.get().getTblMasterZone().getZoneId(), "Wet", localbody, userId);
//            List<KeyPlots> dryKeyPlots = prepareKeyPlots(drySamples, classIntervalDry, user.get().getTblMasterZone().getZoneId(), "Dry", localbody, userId);
//
//            allWetKeyPlots.addAll(wetKeyPlots);
//            allDryKeyPlots.addAll(dryKeyPlots);
//
//// Optionally update sample map to keep reference of the plot after saving
//
//
////            saveSamples(
////
////                    wetSamples,
////                    classIntervalWet,
//////                    localBodyNameMap.get(lbcode) + "_WET",
////                    user.get().getTblMasterZone().getZoneId(),
////                    "Wet",
////                    localbody,
////                    userId,
////                    clusterCounter
////            );
////
////            saveSamples(
////
////                    drySamples,
////                    classIntervalDry,
//////                    localBodyNameMap.get(lbcode) + "_DRY",
////                    user.get().getTblMasterZone().getZoneId(),
////                    "Dry",
////                    localbody,
////                    userId,
////                    clusterCounter
////            );
//            clusterList.add(panchayathCluster);
//        }
//        List<KeyPlots> savedWetKeyPlots = keyPlotsRepository.saveAll(allWetKeyPlots);
//        List<KeyPlots> savedDryKeyPlots = keyPlotsRepository.saveAll(allDryKeyPlots);
//
//        saveClustersForKeyPlots(savedWetKeyPlots, clusterCounter);
//        saveClustersForKeyPlots(savedDryKeyPlots, clusterCounter);
//
//        Map<Long, UUID> wetPlotIdMap = savedWetKeyPlots.stream()
//                .collect(Collectors.toMap(kp -> kp.getBtrDataOld().getId(), KeyPlots::getId));
//
//        Map<Long, UUID> dryPlotIdMap = savedDryKeyPlots.stream()
//                .collect(Collectors.toMap(kp -> kp.getBtrDataOld().getId(), KeyPlots::getId));
//
//        wetSamplesMapList.forEach(row -> {
//            Long btrId = (Long) row.get("id");
//            row.put("plot_id", wetPlotIdMap.get(btrId));
//        });
//
//        drySamplesMapList.forEach(row -> {
//            Long btrId = (Long) row.get("id");
//            row.put("plot_id", dryPlotIdMap.get(btrId));
//        });
//// 👉 Add this
//        List<KeyPlots> allSavedKeyPlots = new ArrayList<>();
//        allSavedKeyPlots.addAll(savedWetKeyPlots);
//        allSavedKeyPlots.addAll(savedDryKeyPlots);
//
//        List<CceCropDetailsResponse> crops = cceCropService.getCceCrops();
//
//        cceCropService.assignCropsToKeyPlots(allSavedKeyPlots, crops,userId);
//        return clusterList;
//    }


    private List<KeyPlots> prepareKeyPlots(
            List<Map<String, Object>> samples,
            int interval,
            int zoneId,
            String landType,
            TblLocalBody localbody,
            UUID userId
    ) {
        UserZoneAssignment zone = userZoneAssignmentRepositoty.findByTblMasterZone_ZoneId(zoneId)
                .orElseThrow(() -> new EntityNotFoundException("Zone not found: " + zoneId));

        List<KeyPlots> keyPlots = new ArrayList<>();

        for (Map<String, Object> sample : samples) {
            Long plotId = (Long) sample.get("id");
            TblBtrData btrData = tblBtrDataRepository.findById(plotId)
                    .orElseThrow(() -> new EntityNotFoundException("BTR not found: " + plotId));

            KeyPlots keyPlot = new KeyPlots();
            keyPlot.setBtrData(btrData);
            keyPlot.setZone(zone.getTblMasterZone());
            keyPlot.setIntervals(interval);
            keyPlot.setLandType(landType);
            keyPlot.setLocalbody(localbody);
            keyPlot.setAgriStartYear(LocalDate.now());
            keyPlot.setAgriEndYear(LocalDate.now().plusYears(1));
            keyPlot.setStatus(true);
            keyPlot.setIsRejected(false);
            keyPlot.setCreated_by(userId);

            keyPlots.add(keyPlot);
        }

        return keyPlots;
    }

    private void saveClustersForKeyPlots(List<KeyPlots> savedKeyPlots, AtomicInteger clusterCounter) {
        List<ClusterMaster> clusterList = new ArrayList<>();
        List<ClusterFormData> clusterFormDataList = new ArrayList<>();

        for (KeyPlots kp : savedKeyPlots) {
            ClusterMaster cluster = new ClusterMaster();
            cluster.setKeyPlot(kp);
            cluster.setStatus("Not Started");
            cluster.setClusterNumber(clusterCounter.getAndIncrement());
            cluster.setZone(kp.getZone());
            clusterList.add(cluster);

            ClusterFormData clusterFormData = new ClusterFormData();
            clusterFormData.setPlot(kp.getBtrData());
            clusterFormData.setClusterMaster(cluster);
            clusterFormData.setPlotLabel("K");
            clusterFormData.setStatus(true);
            clusterFormData.setEnumeratedArea(kp.getBtrData().getTotCent());
            clusterFormData.setCreatedBy(kp.getCreated_by());

            clusterFormDataList.add(clusterFormData);
        }

        clusterMasterRepository.saveAll(clusterList);
        clusterFormDataRepository.saveAll(clusterFormDataList);
    }


    public List<Map<String, Object>> getSystematicSample(List<TblBtrData> plots, int clusters, int interval, int randomStart,Map<String, String> localBodyNameMap,AtomicInteger globalSlNo) {
        List<Map<String, Object>> sample = new ArrayList<>();

        if (plots.isEmpty() || clusters == 0 || interval <= 0) return sample;

        // Sort plots by Sy. No (or area or any consistent key)
        plots.sort(Comparator.comparing(TblBtrData::getId));  // Sort by ID or any field that makes sense

        // Start index as per the provided random start
//        Random random = new Random();
//        int randomStarts = random.nextInt(plots.size());

        int index = randomStart;  // Use provided randomStart (e.g., 242)

        for (int i = 0; i < clusters; i++) {

            TblBtrData plot = plots.get(index-1);

            KeyPlots keyPlot = new KeyPlots();
            Map<String, Object> row = new HashMap<>();
            row.put("id", plot.getId());
            row.put("no", globalSlNo.getAndIncrement());  // ✅ Add global serial number
            row.put("plot_id", plot.getId());
            row.put("Sl.No", i + 1);
            row.put("panchayth",plot.getLbcode());
            row.put("Random No", index);  // Original starting point for random
            row.put("Sy. No", plot.getResvno() + "/" + plot.getResbdno());
            row.put("Area (Cents)", plot.getTotCent());
            row.put("Village/Block", plot.getBcode()); // Adjust this as per your model
            row.put("panchayth", localBodyNameMap.getOrDefault(plot.getLbcode(), plot.getLbcode()));


            sample.add(row);

            // Circular step based on interval
            index = (index + interval) % plots.size();
        }

        return sample;
    }

//    private void saveSamples(List<Map<String, Object>> samples, int interval, int zone_id, String landType,
//                             TblLocalBody localbody, UUID userId, AtomicInteger clusterCounter) {
//
//        UserZoneAssignment zone = userZoneAssignmentRepositoty.findByTblMasterZone_ZoneId(zone_id)
//                .orElseThrow(() -> new EntityNotFoundException("BTR data not found: " + zone_id));
//
//        List<KeyPlots> keyPlotsList = new ArrayList<>();
//        List<ClusterMaster> clusterList = new ArrayList<>();
//        List<ClusterFormData> clusterFormDataList = new ArrayList<>();
//
//        for (Map<String, Object> sample : samples) {
//            KeyPlots keyplot = new KeyPlots();
//            Long plotId = (Long) sample.get("id");
//
//            TblBtrDataOld btrData = tblBtrDataOldRepository.findById(plotId)
//                    .orElseThrow(() -> new EntityNotFoundException("BTR data not found: " + plotId));
//
//            keyplot.setBtrDataOld(btrData);
//            keyplot.setZone_id(Long.valueOf(zone.getTblMasterZone().getZoneId()));
//            keyplot.setIntervals(interval);
//            keyplot.setLandType(landType);
//            keyplot.setLocalbody(localbody);
//            keyplot.setAgriStartYear(LocalDate.now());
//            keyplot.setAgriEndYear(LocalDate.now().plusYears(1));
//            keyplot.setStatus(true);
//            keyplot.setIsRejected(false);
//            keyplot.setCreated_by(userId);
//
//            keyPlotsList.add(keyplot);
//        }
//
//        // Bulk save KeyPlots
//        List<KeyPlots> savedKeyPlots = keyPlotsRepository.saveAll(keyPlotsList);
//
//        for (int i = 0; i < savedKeyPlots.size(); i++) {
//            KeyPlots kp = savedKeyPlots.get(i);
//            Map<String, Object> originalSampleRow = samples.get(i);
//            originalSampleRow.put("plot_id", kp.getId());
//        }
//
//        // Now create corresponding ClusterMaster entries
//        for (KeyPlots kp : savedKeyPlots) {
//            ClusterMaster cluster = new ClusterMaster();
//            cluster.setKeyPlot(kp);
//            cluster.setStatus("Not Started");
//            cluster.setClusterNumber(clusterCounter.getAndIncrement());
//            clusterList.add(cluster);
//
//            ClusterFormData clusterFormData = new ClusterFormData();
//            clusterFormData.setPlot(kp.getBtrDataOld());
//            clusterFormData.setClusterMaster(cluster);
//            clusterFormData.setPlotLabel("K");
//            clusterFormData.setStatus(true);
//            clusterFormData.setEnumeratedArea(kp.getBtrDataOld().getArea());
//            clusterFormDataList.add(clusterFormData);
//            clusterFormData.setCreatedBy(kp.getCreated_by());
//        }
//        clusterMasterRepository.saveAll(clusterList);
//        clusterFormDataRepository.saveAll(clusterFormDataList);
//
//    }


//    public Map<String, Object> rejectAndReplaceKeyplot(Long keyPlotId, KeyPlotRejectRequest request) {
//
//        // 1. Find the keyplot to be rejected
//        TblBtrData btrData = tblBtrDataRepository.findById(keyPlotId)
//                .orElseThrow(() -> new EntityNotFoundException("BTR data not found with ID: " + keyPlotId));
//
//        KeyPlots rejectedKeyPlot = keyPlotsRepository.findByBtrData(btrData)
//                .orElseThrow(() -> new EntityNotFoundException("Keyplot not found for BTR ID: " + keyPlotId));
//
//        // 2. Mark the existing keyplot as rejected
//        rejectedKeyPlot.setIsRejected(true);
//        rejectedKeyPlot.setReason( request.getReason());
//        rejectedKeyPlot.setReason(request.getReason());
//        rejectedKeyPlot.setRejectDate(LocalDate.now());
//        rejectedKeyPlot.setCreated_by(request.getUserid());
//        rejectedKeyPlot.setStatus(false);
//        keyPlotsRepository.save(rejectedKeyPlot);
//
//        // 3. Get details for re-sampling
//
//        Optional<UserZoneAssignment> userzone = userZoneAssignmentRepositoty.findByTblMasterZone_ZoneId(rejectedKeyPlot.getZone().getZoneId());
//        if (userzone.isEmpty()) {
//            throw new IllegalStateException("UserZoneAssignment not found.");
//        }
//
//        String rejectedLandType = rejectedKeyPlot.getLandType();
//        TblLocalBody localbody = rejectedKeyPlot.getLocalbody();
//        Integer rejectedInterval = rejectedKeyPlot.getIntervals();
//        if (localbody == null) {
//            throw new IllegalStateException("Localbody is missing.");
//        }
//
//        // ✅ 4. Exclude BTR IDs from only last year
//        LocalDate cutoffDate = LocalDate.now().minusYears(1);
//        List<Long> recentlyUsedIds = keyPlotsRepository.findRecentlyUsedBtrIds(cutoffDate);
//        Set<Long> excludedIds = new HashSet<>(recentlyUsedIds);
//        excludedIds.add(btrData.getId());  // Exclude the current one too
//
//        // 5. Get replacement plots
//        List<TblBtrData> replacements = tblBtrDataRepository
//                .findAllByLbcodeAndLtype(
//                        btrData.getLbcode(),
//                        rejectedLandType.substring(0, 1).toUpperCase()
//                ).stream()
//                .filter(btr -> !excludedIds.contains(btr.getId()))
//                .collect(Collectors.toList());
//
//        if (replacements.isEmpty()) {
//            throw new RuntimeException("No suitable replacement found for " + rejectedLandType);
//        }
//
//        // 6. Select a random plot
//        TblBtrData newPlot = replacements.get(new Random().nextInt(replacements.size()));
//
//        // 7. Create new keyplot
//        KeyPlots newKeyPlot = new KeyPlots();
//        newKeyPlot.setBtrData(newPlot);
//        newKeyPlot.setZone(rejectedKeyPlot.getZone());
//        newKeyPlot.setIntervals(rejectedInterval);
//        newKeyPlot.setLandType(rejectedLandType);
//        newKeyPlot.setCreated_by(request.getUserid());
//        newKeyPlot.setLocalbody(localbody);
//        newKeyPlot.setAgriStartYear(LocalDate.now());
//        newKeyPlot.setAgriEndYear(LocalDate.now().plusYears(1));
//        newKeyPlot.setStatus(true);
//        newKeyPlot.setIsRejected(false);
//        newKeyPlot = keyPlotsRepository.save(newKeyPlot);
//
//        // 8. Replace cluster
//        Optional<ClusterMaster> clusterOpt = clusterMasterRepository.findByKeyPlotId(rejectedKeyPlot.getId());
//        if (clusterOpt.isPresent()) {
//            ClusterMaster oldCluster = clusterOpt.get();
//            Integer cluster_number = oldCluster.getClusterNumber();
//            oldCluster.setIsReject(true);
//            oldCluster.setInvestigatorRemark(request.getReason_for_cluster());
//            clusterFormDataRepository.deleteAll(
//                    clusterFormDataRepository.findByClusterMaster(oldCluster)
//            );
//            clusterMasterRepository.save(oldCluster);
//
//            ClusterMaster newCluster = new ClusterMaster();
//            newCluster.setKeyPlot(newKeyPlot);
//            newCluster.setCreatedAt(LocalDateTime.now());
//            newCluster.setUpdatedAt(LocalDateTime.now());
//            newCluster.setClusterNumber(cluster_number);
//            newCluster.setZone(newKeyPlot.getZone()); // ✅ Fix the error
//
//
//            ClusterFormData clusterFormData = new ClusterFormData();
//            clusterFormData.setPlot(newKeyPlot.getBtrData());
//            clusterFormData.setPlotLabel("K");
//            clusterFormData.setClusterMaster(newCluster);
//            clusterFormData.setStatus(true);
//            clusterFormData.setCreatedBy(request.getUserid());
//            clusterFormData.setCreatedAt(LocalDateTime.now());
//            clusterFormData.setEnumeratedArea(newKeyPlot.getBtrData().getArea());
//
//            clusterMasterRepository.save(newCluster);
//            clusterFormDataRepository.save(clusterFormData);
//
//            // Notify CCE system
//            // After creating the new KeyPlot and Cluster...
//            AvailableCcePlotRejectionRequest rejectionRequest = new AvailableCcePlotRejectionRequest();
//            rejectionRequest.setOldPlotId(rejectedKeyPlot.getId());
//            rejectionRequest.setOldClusterId(clusterOpt.get().getCluMasterId());
//            rejectionRequest.setNewPlotId(newKeyPlot.getId());
//            rejectionRequest.setNewClusterId(newCluster.getCluMasterId());
//            rejectionRequest.setUserId(request.getUserid());
//
//            try {
//                ResponseEntity<Response> rejectionResponse = formEntryClient.availableCcePlotRejection(rejectionRequest);
//
//                if (!rejectionResponse.getStatusCode().equals(HttpStatus.CREATED)) {
//                    String message = (rejectionResponse.getBody() != null) ? rejectionResponse.getBody().getMessage() : "Unknown error";
//
//                    if (message != null && message.contains("Active plots with given oldPlotId and oldClusterId not found")) {
//                        // Log it, but don't block execution
//                        System.out.println("CCE system had no matching data. Skipping sync. Message: " + message);
//                    } else {
//                        throw new RuntimeException("CCE rejection API call failed: " + message);
//                    }
//                } else {
//                    System.out.println("CCE rejection API call success.");
//                }
//            } catch (Exception e) {
//                // Optional: log and continue, or rethrow depending on importance
//                System.out.println("CCE rejection API call exception: " + e.getMessage());
//            }
//
//        } else {
//            throw new RuntimeException("Cluster not found for rejected keyplot.");
//        }
//
//        // 9. Return response
//        Map<String, Object> newPlotMap = new HashMap<>();
//        newPlotMap.put("id", newKeyPlot.getId());
//
//        newPlotMap.put("Sy. No", newKeyPlot.getBtrData().getResvno() + "/" + newKeyPlot.getBtrData().getResbdno());
//        newPlotMap.put("panchayth", newKeyPlot.getLocalbody().getLocalbodyNameEn());
//        newPlotMap.put("Area (Cents)", newKeyPlot.getBtrData().getArea());
//        newPlotMap.put("Village/Block", newKeyPlot.getBtrData().getBcode());
//        newPlotMap.put("Land Type", newKeyPlot.getLandType());
//
//        return newPlotMap;
//    }

    @Transactional
    public Map<String, Object> rejectAndReplaceKeyplot(UUID keyPlotId, KeyPlotRejectRequest request) {
        // 1. Find the keyplot
        KeyPlots rejectedKeyPlot = keyPlotsRepository.findById(keyPlotId)
                .orElseThrow(() -> new EntityNotFoundException("Keyplot not found with ID: " + keyPlotId));

        // 2. Update keyplot fields
        rejectedKeyPlot.setIsRejected(true);
        rejectedKeyPlot.setReason(request.getReason());
        rejectedKeyPlot.setRejectDate(LocalDate.now());
        rejectedKeyPlot.setCreated_by(request.getUserid());
        rejectedKeyPlot.setStatus(false);
        keyPlotsRepository.save(rejectedKeyPlot);

        // 3. Find and reject cluster linked to this keyplot
        ClusterMaster cluster = clusterMasterRepository.findByKeyPlotId(rejectedKeyPlot.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cluster not found for KeyPlot ID: " + keyPlotId));

        cluster.setIsReject(true);
        cluster.setStatus("rejected");
        cluster.setIs_active(false);
        cluster.setInvestigatorRemark(request.getReason_for_cluster());
        cluster.setUpdatedAt(LocalDateTime.now());
        clusterMasterRepository.save(cluster);

        // 4. Build response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "KeyPlot and its Cluster rejected successfully");
        response.put("keyPlotId", rejectedKeyPlot.getId());
        response.put("clusterId", cluster.getCluMasterId());
        return response;
    }

    public KeyPlotDetailsResponse getKeyPlotDetails(UUID plotId) {
        Optional<KeyPlots> keyPlotOpt = keyPlotsRepository.findById(plotId);

        if (keyPlotOpt.isEmpty()) {
            throw new NoSuchElementException("Plot ID not found: " + plotId);
        }

        KeyPlots keyPlot = keyPlotOpt.get();
        TblBtrData plot = keyPlot.getBtrData();

        String syNo = plot.getResvno() + "/" + plot.getResbdno();
        String villageBlock = plot.getBcode();
        double area = plot.getTotCent();

        String lbcode = plot.getLbcode();
        String panchayath = localBodyRepository.findByCodeApi(lbcode)
                .map(TblLocalBody::getLocalbodyNameEn)
                .orElse(lbcode); // fallback if name not found
        String landType = keyPlot.getLandType();

        Optional<TblMasterVillage> village = tblMasterVillageRepository.findByLsgCode(plot.getLsgcode());
        // Fetch related SidePlotDTOs
        List<SidePlotDTO> sidePlots = fetchSidePlotsForKeyPlot(keyPlot);
        String villageName = village.get().getVillageNameEn();
        Integer villageId = village.get().getVillageId();
        return new KeyPlotDetailsResponse(keyPlot.getId(),villageName,villageId,villageBlock, panchayath, lbcode,syNo, area, landType, sidePlots);
    }

    private List<SidePlotDTO> fetchSidePlotsForKeyPlot(KeyPlots keyPlot) {
        Optional<ClusterMaster> clusterOpt = clusterMasterRepository.findTopByKeyPlotOrderByCreatedAtDesc(keyPlot);

        if (clusterOpt.isEmpty()) {
            return new ArrayList<>();
        }

        ClusterMaster cluster = clusterOpt.get();

        List<ClusterFormData> formDataList = clusterFormDataRepository.findByClusterMaster(cluster);

        // Step 1: Extract unique lsgcodes
        Set<Integer> lsgCodes = formDataList.stream()
                .map(data -> data.getPlot().getLsgcode())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Step 2: Fetch villages by lsgcode
        List<TblMasterVillage> villages = tblMasterVillageRepository.findByLsgCodeIn(lsgCodes);

        // Step 3: Map lsgcode to villageNameMal
        Map<Integer, String> lsgcodeToVillageNameMap = villages.stream()
                .collect(Collectors.toMap(
                        TblMasterVillage::getLsgCode,
                        TblMasterVillage::getVillageNameEn
                ));

        // Step 4: Build grouped map
        Map<String, List<ClusterFormRowDTO>> grouped = formDataList.stream()
                .collect(Collectors.groupingBy(
                        ClusterFormData::getPlotLabel,
                        Collectors.mapping(data -> {
                            TblBtrData plot = data.getPlot();
                            String villageNameMal = lsgcodeToVillageNameMap.getOrDefault(plot.getLsgcode(), "Unknown");

                            return new ClusterFormRowDTO(
                                    data.getCluDetailId(),
                                    plot.getId(),
                                    Double.valueOf(data.getEnumeratedArea()),
                                    plot.getResvno(),
                                    plot.getResbdno(),
                                    BigDecimal.valueOf(plot.getTotCent())
                                            .setScale(2, RoundingMode.HALF_UP)
                                            .doubleValue(),
                                    plot.getBcode(),
                                    villageNameMal

                            );
                        }, Collectors.toList())
                ));

        return grouped.entrySet().stream()
                .map(entry -> new SidePlotDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public KeyPlots keyplotsOwnersaveOrUpdate(@Valid KeyPlotDetailsRequest request) {
        if (request.getKpId() == null) {
            throw new IllegalArgumentException("KeyPlot ID (kpId) must be provided for update.");
        }

        Optional<KeyPlots> optional = keyPlotsRepository.findById(request.getKpId());

        if (optional.isEmpty()) {
            throw new IllegalArgumentException("KeyPlot with ID " + request.getKpId() + " not found.");
        }

        KeyPlots details = optional.get();

        // Update basic fields
        details.setOwner_name(request.getName());
        details.setAddress(request.getAddress());
        details.setPhone_number(request.getMobileNumber());
        details.setGeocoordinate(request.getGeocoordinate());

        // Optionally set updated by (replace with logged-in user ID if needed)
        details.setDetails_updatedby(request.getUpdatedBy());

        return keyPlotsRepository.save(details);
    }

//    public Object KeyplotsFormationOldbtr(UUID userId) {
//        var user = userZoneAssignmentRepositoty.findByUserId(userId);
//        var zoneRevenueList = tblZoneRevenueVillageMappingRepository.findByZone(user.get().getTblMasterZone().getZoneId());
//        List<Integer> villageIds = zoneRevenueList.stream()
//                .map(TblZoneRevenueVillageMapping::getRevenueVillage)
//                .toList();
//        List<TblMasterVillage> villageList = tblMasterVillageRepository.findAllById(villageIds);
//        List<Integer> lsgcodes = villageList.stream().map(TblMasterVillage::getLsgCode).toList();
//
//        List<TblBtrDataOld> allData = tblBtrDataOldRepository.findAllByLsgcodeIn(lsgcodes);
//
//        // Fetch the dynamic land type classification map
//        Map<String, String> landTypeClassificationMap = landTypeClassificationService.getLandTypeClassificationMap();
//
//        System.out.println("landtype class "+landTypeClassificationMap);
//        // Get LB codes
//        List<String> LbcodeList = allData.stream()
//                .map(TblBtrDataOld::getLbcode)
//                .distinct()
//                .collect(Collectors.toList());
//
//        // Fetch Local Bodies
//        List<TblLocalBody> localBodies_full = localBodyRepository.findAllByCodeApiIn(LbcodeList);
//
//        // Prepare the map of LB Code to Local Body Name
//        Map<String, String> localBodyNameMap = new HashMap<>();
//        localBodies_full.forEach(localBody -> {
//            localBodyNameMap.put(localBody.getCodeApi(), localBody.getLocalbodyNameMal());
//        });
//
//        // Group data by Panchayath (LB Code)
//        Map<String, List<TblBtrDataOld>> panchayathDataMap = allData.stream()
//                .collect(Collectors.groupingBy(TblBtrDataOld::getLbcode));
//
//        // Calculate the total area of the zone and also gather wet and dry area data for each panchayath
//        double totalZoneArea = 0;
//        Map<String, Double> panchayathAreaMap = new HashMap<>();
//        Map<String, Double> wetAreaMap = new HashMap<>();
//        Map<String, Double> dryAreaMap = new HashMap<>();
//        Map<String, Integer> wetPlotsMap = new HashMap<>();  // Wet plots count for each panchayath
//        Map<String, Integer> dryPlotsMap = new HashMap<>();  // Dry plots count for each panchayath
//
//        for (Map.Entry<String, List<TblBtrDataOld>> entry : panchayathDataMap.entrySet()) {
//            String lbcode = entry.getKey();
//            List<TblBtrDataOld> panchayathData = entry.getValue();
//
//            double wetArea = 0, dryArea = 0, othersArea = 0;
//            int wetPlots = 0, dryPlots = 0;
//
//            for (TblBtrDataOld data : panchayathData) {
//                String landTypeValue = data.getLtype();
//                double nsqm = data.getNsqm();
//                double nare = data.getNare();
//                double nhect = data.getNhect();
//
//                // Sum up the areas based on land type classification
//                if (landTypeClassificationMap.containsKey(landTypeValue)) {
//                    String classification = landTypeClassificationMap.get(landTypeValue);
//                    double area = nsqm * 0.000001 + nare * 0.0001 + nhect * 0.01;
//
//                    switch (classification) {
//                        case "wet":
//                            wetArea += area;
//                            wetPlots++;  // Count the number of wet plots
//                            break;
//                        case "dry":
//                        case "others": // Treat "others" as "dry"
//                            dryArea += area;
//                            dryPlots++;  // Count the number of dry plots
//                            break;
//                    }
//                }
//            }
//
//            // Total area for the current panchayath
//            double panchayathArea = wetArea + dryArea + othersArea;
//            panchayathAreaMap.put(lbcode, panchayathArea);
//            wetAreaMap.put(lbcode, wetArea);
//            dryAreaMap.put(lbcode, dryArea);
//            wetPlotsMap.put(lbcode, wetPlots);
//            dryPlotsMap.put(lbcode, dryPlots);
//            totalZoneArea += panchayathArea;
//        }
//
//        // Calculate clusters for each panchayath and distribute wet and dry clusters
//        List<Map<String, Object>> clusterList = new ArrayList<>();
//        for (Map.Entry<String, Double> entry : panchayathAreaMap.entrySet()) {
//            String lbcode = entry.getKey();
//            double panchayathArea = entry.getValue();
//            double wetArea = wetAreaMap.get(lbcode);
//            double dryArea = dryAreaMap.get(lbcode);
//            int wetPlots = wetPlotsMap.get(lbcode);
//            int dryPlots = dryPlotsMap.get(lbcode);
//
//            // Calculate the total number of clusters for the current panchayath
//            int totalClusters = (int) Math.round((panchayathArea / totalZoneArea) * 100);
//
//            // Calculate the number of wet clusters and dry clusters
//            int wetClusters = (int) Math.round(totalClusters * (wetArea / panchayathArea));
//            int dryClusters = totalClusters - wetClusters;
//
//            // Calculate the class intervals (wet and dry)
//            int classIntervalWet = wetPlots / wetClusters;
//            int classIntervalDry = dryPlots / dryClusters;
//
//            // Round the class intervals to the nearest integer
//            classIntervalWet = (int) Math.round(classIntervalWet);
//            classIntervalDry = (int) Math.round(classIntervalDry);
//
//            // Prepare response for the current panchayath
//            Map<String, Object> panchayathCluster = new HashMap<>();
//            panchayathCluster.put("panchayath", localBodyNameMap.get(lbcode));
//            panchayathCluster.put("totalClusters", totalClusters);
//            panchayathCluster.put("wetClusters", wetClusters);
//            panchayathCluster.put("dryClusters", dryClusters);
//            panchayathCluster.put("classIntervalWet", classIntervalWet);
//            panchayathCluster.put("classIntervalDry", classIntervalDry);
//
//            clusterList.add(panchayathCluster);
//        }
//
//        // Sort the cluster list by Panchayath name (alphabetically)
//        clusterList.sort(Comparator.comparing(o -> (String) o.get("panchayath")));
//
//        return clusterList;
//    }

    public KeyPlotOwnerDetailsResponse getByKpId(UUID kpId) {
        modelMapper
                .getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(PRIVATE)
                .setPropertyCondition(Conditions.isNotNull());
        KeyPlots keyPlotDetails =
                keyPlotsRepository
                        .findById(kpId)
                        .orElseThrow(() -> new IllegalArgumentException("Id not found: " + kpId));

        return modelMapper.map(keyPlotDetails, KeyPlotOwnerDetailsResponse.class);
    }

    public Object KeyplotsFormation(UUID userId) {
        var user = userZoneAssignmentRepositoty.findByUserId(userId);
        var zoneRevenueList = tblZoneRevenueVillageMappingRepository.findByZone(user.get().getTblMasterZone().getZoneId());
        List<Integer> villageIds = zoneRevenueList.stream()
                .map(TblZoneRevenueVillageMapping::getRevenueVillage)
                .toList();
        List<TblMasterVillage> villageList = tblMasterVillageRepository.findAllById(villageIds);
        List<Integer> lsgcodes = villageList.stream().map(TblMasterVillage::getLsgCode).toList();

        List<TblBtrData> allData = tblBtrRepository.findAllByLsgcodeIn(lsgcodes);

        // Fetch the dynamic land type classification map
        Map<String, String> landTypeClassificationMap = landTypeClassificationService.getLandTypeClassificationMap();


        // Get LB codes
        List<String> LbcodeList = allData.stream()
                .map(TblBtrData::getLbcode)
                .distinct()
                .collect(Collectors.toList());

        // Fetch Local Bodies
        List<TblLocalBody> localBodies_full = localBodyRepository.findAllByCodeApiIn(LbcodeList);

        // Prepare the map of LB Code to Local Body Name
        Map<String, String> localBodyNameMap = new HashMap<>();
        localBodies_full.forEach(localBody -> {
            localBodyNameMap.put(localBody.getCodeApi(), localBody.getLocalbodyNameEn());
        });

        // Group data by Panchayath (LB Code)
        Map<String, List<TblBtrData>> panchayathDataMap = allData.stream()
                .collect(Collectors.groupingBy(TblBtrData::getLbcode));

        // Calculate the total area of the zone and also gather wet and dry area data for each panchayath
        double totalZoneArea = 0;
        Map<String, Double> panchayathAreaMap = new HashMap<>();
        Map<String, Double> wetAreaMap = new HashMap<>();
        Map<String, Double> dryAreaMap = new HashMap<>();
        Map<String, Integer> wetPlotsMap = new HashMap<>();  // Wet plots count for each panchayath
        Map<String, Integer> dryPlotsMap = new HashMap<>();  // Dry plots count for each panchayath

        for (Map.Entry<String, List<TblBtrData>> entry : panchayathDataMap.entrySet()) {
            String lbcode = entry.getKey();
            List<TblBtrData> panchayathData = entry.getValue();

            double wetArea = 0, dryArea = 0, othersArea = 0;
            int wetPlots = 0, dryPlots = 0;

            for (TblBtrData data : panchayathData) {
                String landTypeValue = data.getLtype();
                double nsqm = data.getNsqm();
                double nare = data.getNare();
                double nhect = data.getNhect();

                // Sum up the areas based on land type classification
                if (landTypeClassificationMap.containsKey(landTypeValue)) {
                    String classification = landTypeClassificationMap.get(landTypeValue);
                    double area = nsqm * 0.000001 + nare * 0.0001 + nhect * 0.01;

                    switch (classification) {
                        case "wet":
                            wetArea += area;
                            wetPlots++;  // Count the number of wet plots
                            break;
                        case "dry":
                        case "others": // Treat "others" as "dry"
                            dryArea += area;
                            dryPlots++;  // Count the number of dry plots
                            break;
                    }
                }
            }

            // Total area for the current panchayath
            double panchayathArea = wetArea + dryArea + othersArea;
            panchayathAreaMap.put(lbcode, panchayathArea);
            wetAreaMap.put(lbcode, wetArea);
            dryAreaMap.put(lbcode, dryArea);
            wetPlotsMap.put(lbcode, wetPlots);
            dryPlotsMap.put(lbcode, dryPlots);
            totalZoneArea += panchayathArea;
        }

        // Calculate clusters for each panchayath and distribute wet and dry clusters
        List<Map<String, Object>> clusterList = new ArrayList<>();
        for (Map.Entry<String, Double> entry : panchayathAreaMap.entrySet()) {
            String lbcode = entry.getKey();
            double panchayathArea = entry.getValue();
            double wetArea = wetAreaMap.get(lbcode);
            double dryArea = dryAreaMap.get(lbcode);
            int wetPlots = wetPlotsMap.get(lbcode);
            int dryPlots = dryPlotsMap.get(lbcode);

            // Calculate the total number of clusters for the current panchayath
            int totalClusters = (int) Math.round((panchayathArea / totalZoneArea) * 100);

            // Calculate the number of wet clusters and dry clusters
            int wetClusters = (int) Math.round(totalClusters * (wetArea / panchayathArea));
            int dryClusters = totalClusters - wetClusters;

            // Calculate the class intervals (wet and dry)
            int classIntervalWet = wetPlots / wetClusters;
            int classIntervalDry = dryPlots / dryClusters;

            // Round the class intervals to the nearest integer
            classIntervalWet = (int) Math.round(classIntervalWet);
            classIntervalDry = (int) Math.round(classIntervalDry);

            // Prepare response for the current panchayath
            Map<String, Object> panchayathCluster = new HashMap<>();
            panchayathCluster.put("panchayath", localBodyNameMap.get(lbcode));
            panchayathCluster.put("totalClusters", totalClusters);
            panchayathCluster.put("wetClusters", wetClusters);
            panchayathCluster.put("dryClusters", dryClusters);
            panchayathCluster.put("classIntervalWet", classIntervalWet);
            panchayathCluster.put("classIntervalDry", classIntervalDry);

            clusterList.add(panchayathCluster);
        }

        // Sort the cluster list by Panchayath name (alphabetically)
        clusterList.sort(Comparator.comparing(o -> (String) o.get("panchayath")));

        return clusterList;
    }
}
