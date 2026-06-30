package cdti.aidea.earas.service;

import cdti.aidea.earas.config.FormEntryClient;
import cdti.aidea.earas.contract.FormEntryDto.*;
import cdti.aidea.earas.model.Btr_models.ClusterMaster;
import cdti.aidea.earas.model.Btr_models.KeyPlots;
import cdti.aidea.earas.model.Btr_models.Masters.*;
import cdti.aidea.earas.repository.Btr_repo.*;
import cdti.aidea.earas.utils.AgriYearUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CceCropService {

  private final FormEntryClient formEntryClient;
  private final ObjectMapper objectMapper;
  private final ClusterMasterRepository clusterMasterRepository;
  private final TblMasterZoneRepository masterZoneRepository;
  private final ZoneLocalbodyBlockMappingRepository zoneLocalbodyBlockMappingRepository;
  private final MasterBlockRepository masterBlockRepository;
  private final LocalBodyRepository localBodyRepository;

  int attempt = 0;

  public List<CceCropDetailsResponse> getCceCrops() {
    Map<String, Object> response = formEntryClient.getRawCceCropDetails();
    Object payload = response.get("payload");

    // Convert payload to list of CceCropDetailsResponse
    return objectMapper.convertValue(payload, new TypeReference<List<CceCropDetailsResponse>>() {});
  }

  public void assignCropsToKeyPlots(
      List<KeyPlots> keyPlots, List<CceCropDetailsResponse> crops, UUID userId) {
    Random random = new Random();

    List<KeyPlots> wetPlots =
        keyPlots.stream()
            .filter(kp -> "WET".equalsIgnoreCase(kp.getLandType()))
            .collect(Collectors.toList());

    List<KeyPlots> dryPlots =
        keyPlots.stream()
            .filter(kp -> "DRY".equalsIgnoreCase(kp.getLandType()))
            .collect(Collectors.toList());

    for (CceCropDetailsResponse crop : crops) {
      List<KeyPlots> eligiblePlots;

      switch (crop.getFrameName().toUpperCase()) {
        case "WET":
          eligiblePlots = wetPlots;
          break;
        case "DRY":
          eligiblePlots = dryPlots;
          break;
        case "WET / DRY":
          eligiblePlots = new ArrayList<>();
          eligiblePlots.addAll(wetPlots);
          eligiblePlots.addAll(dryPlots);
          break;
        default:
          log.warn("Unknown frameName: {}", crop.getFrameName());
          continue;
      }

      Collections.shuffle(eligiblePlots, random);
      List<KeyPlots> selectedPlots =
          eligiblePlots.stream().limit(crop.getNoOfCce()).collect(Collectors.toList());

      log.info(
          "\nCrop ID: {} ({}), Assigned to {} key plots:",
          crop.getCropId(),
          crop.getFrameName(),
          selectedPlots.size());

      for (KeyPlots plot : selectedPlots) {
        ClusterMaster cluster = clusterMasterRepository.findByKeyPlot(plot).orElse(null);

        CceAssignmentRequest request =
            new CceAssignmentRequest(
                plot.getId(),
                cluster != null ? cluster.getCluMasterId() : null,
                plot.getZone().getZoneId(),
                    plot.getBtrData().getId(),
                    plot.getBtrData().getLbcode(),
                crop.getCropId(),
                "random",
                userId,
                crop.getAgriStartYear(),
                plot.getLandType(),
                //                        "2025-07-01",
                //                        "2026-06-30",
                true,
                true);

        try {
          formEntryClient.saveCceAssignment(request);
          log.info("✅ Saved: {}", request);
        } catch (Exception e) {
          log.error("Failed to save assignment for plot {}: {}", plot.getId(), e.getMessage());
        }
      }
    }
  }

////  @CircuitBreaker(name = "companyBreaker", fallbackMethod = "fallbackAssignedCcePlots")
////  @Retry(name = "companyBreaker", fallbackMethod = "fallbackAssignedCcePlots")
//  public CcePlotResult getAssignedCcePlotsByZoneId(Long zoneId) {
//    AvailableCcePlotFetchRequest request = new AvailableCcePlotFetchRequest(zoneId);
//    Map<String, Object> response = formEntryClient.getAvailableCcePlotsByZoneId(request);
//    Object payload = response.get("payload");
//    List<AvailableCcePlotResponse> plots =
//        objectMapper.convertValue(payload, new TypeReference<List<AvailableCcePlotResponse>>() {});
//    return new CcePlotResult(plots, false);
//  }
//  public CcePlotResult fallbackAssignedCcePlots(Long zoneId, Throwable t) {
//    log.warn("Fallback triggered for zoneId: {}", zoneId, t);
//    return new CcePlotResult(Collections.emptyList(), true);
//  }
public CcePlotResult getAssignedCcePlotsByZoneId(Long zoneId,String agriYear) {

  try {
    AvailableCcePlotFetchRequest request =
            new AvailableCcePlotFetchRequest(zoneId,agriYear);

    Map<String, Object> response =
            formEntryClient.getAvailableCcePlotsByZoneId(request);

    Object payload = response.get("payload");

    List<AvailableCcePlotResponse> plots =
            objectMapper.convertValue(
                    payload,
                    new TypeReference<List<AvailableCcePlotResponse>>() {}
            );

    return new CcePlotResult(plots, false);

  } catch (Exception e) {
    log.warn("CCE API failed for zoneId {}", zoneId, e);

    // ✅ fallback manually
    return new CcePlotResult(Collections.emptyList(), true);
  }
}

  public List<FetchDistrictResponse> getDistrictsByClusterIds(List<Long> clusterIds) {
    List<ClusterMaster> clusters =
            clusterMasterRepository.findAllById(clusterIds);

    return clusters.stream()
            .filter(cluster ->
                    cluster.getZone() != null
                            && cluster.getZone().getDistrictMaster() != null)
            .map(cluster -> {

              DistrictMaster district =
                      cluster.getZone().getDistrictMaster();

              return FetchDistrictResponse.builder()
                      .clusterId(cluster.getCluMasterId())
                      .landType(cluster.getKeyPlot().getLandType())
                      .districtId(district.getDist_id())
                      .districtName(district.getDist_name_en())
                      .build();
            })
            .toList();
  }

  public List<FetchTalukResponse> getTalukByDistrictId(
          Long distId,
          String agriYear) {

    LocalDate agriStartYear =
            AgriYearUtil.getAgriYearStart(agriYear);

    LocalDate agriEndYear =
            AgriYearUtil.getAgriYearEnd(agriYear);

    System.out.println(agriStartYear);
    System.out.println(agriEndYear);

    List<TblMasterZone> zones =
            masterZoneRepository.findByDistId(
                    distId.intValue());

    System.out.println("Zones Count : " + zones.size());

    List<FetchTalukResponse> responseList =
            new ArrayList<>();

    for (TblMasterZone zone : zones) {

      Integer zoneId = zone.getZoneId();
      Integer talukId = zone.getDesTalukId();

      String talukName = null;

      if (zone.getDesTalukMaster() != null) {
        talukName =
                zone.getDesTalukMaster()
                        .getDesTalukNameEn();
      }

      List<ClusterMaster> clusters =
              clusterMasterRepository.findClustersByDistrictAndAgriYear(
                      zoneId,
                      agriStartYear,
                      agriEndYear);

      System.out.println(
              "Zone : " + zoneId +
                      " Clusters : " + clusters.size());

      for (ClusterMaster cluster : clusters) {

        responseList.add(
                FetchTalukResponse.builder()
                        .clusterId(cluster.getCluMasterId())
                        .talukId(Long.valueOf(talukId))
                        .talukName(talukName)
                        .createdAt(cluster.getCreatedAt())
                        .landType(cluster.getKeyPlot().getLandType())
                        .build());
      }
    }
    return responseList;
  }

  public List<FetchBlocksResponse> getBlocksByTalukId(Long talukId, String agriYear) {

    LocalDate agriStartYear =
            AgriYearUtil.getAgriYearStart(agriYear);

    LocalDate agriEndYear =
            AgriYearUtil.getAgriYearEnd(agriYear);

    List<TblMasterZone> zones =
            masterZoneRepository.findByDesTalukId(
                    talukId.intValue());

    List<FetchBlocksResponse> responseList =
            new ArrayList<>();

    for (TblMasterZone zone : zones) {

      Integer zoneId = zone.getZoneId();

      List<ZoneLocalbodyBlockMapping> mappings =
              zoneLocalbodyBlockMappingRepository
                      .findByZone(zoneId);


      for (ZoneLocalbodyBlockMapping mapping : mappings) {

        Integer blockId = null;
        String blockName = null;

        Integer localBodyId = null;
        String localBodyName = null;

        Integer id = mapping.getBlockDetails();

        Optional<MasterBlock> block =
                masterBlockRepository.findById(id);

        if (block.isPresent()) {

          blockId = id;
          blockName = block.get().getBlockName();

        } else {

          Optional<TblLocalBody> localBody =
                  localBodyRepository.findById(id);

          if (localBody.isPresent()) {

            localBodyId = id;
            localBodyName = localBody.get().getLocalbodyNameEn();

            System.out.println("Localbody found : " + localBodyName);

          }
        }

        List<ClusterMaster> clusters =
                clusterMasterRepository.findCompletedClustersByZoneAndAgriYear(
                        zone.getZoneId(),
                        agriStartYear,
                        agriEndYear);

        for (ClusterMaster cluster : clusters) {

          responseList.add(
                  FetchBlocksResponse.builder()
                          .blockId(blockId)
                          .blockName(blockName)
                          .localBodyId(localBodyId)
                          .localBodyName(localBodyName)
                          .zoneId(zone.getZoneId().longValue())
                          .zoneName(zone.getZoneNameEn())
                          .clusterId(cluster.getCluMasterId())
                          .createdAt(cluster.getCreatedAt())
                          .landType(cluster.getKeyPlot().getLandType())
                          .build()
          );
        }
      }
    }

    return responseList;
  }
}
