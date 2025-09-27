package cdti.aidea.earas.service;

import cdti.aidea.earas.config.FormEntryClient;
import cdti.aidea.earas.contract.FormEntryDto.*;
import cdti.aidea.earas.model.Btr_models.ClusterMaster;
import cdti.aidea.earas.model.Btr_models.KeyPlots;
import cdti.aidea.earas.repository.Btr_repo.ClusterMasterRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.annotation.Retry;
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
                //                        plot.getBtrDataOld().getId(),
                crop.getCropId(),
                "random",
                userId,
                crop.getAgriStartYear(),
                crop.getAgriEndYear(),
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

  //    @CircuitBreaker(name = "companyBreaker", fallbackMethod = "fallbackAssignedCcePlots")
//  @Retry(name = "companyBreaker", fallbackMethod = "fallbackAssignedCcePlots")
  public CcePlotResult getAssignedCcePlotsByZoneId(Long zoneId) {
    AvailableCcePlotFetchRequest request = new AvailableCcePlotFetchRequest(zoneId);
    Map<String, Object> response = formEntryClient.getAvailableCcePlotsByZoneId(request);
    Object payload = response.get("payload");
    List<AvailableCcePlotResponse> plots =
        objectMapper.convertValue(payload, new TypeReference<List<AvailableCcePlotResponse>>() {});
    return new CcePlotResult(plots, false);
  }

  public CcePlotResult fallbackAssignedCcePlots(Long zoneId, Throwable t) {
    log.warn("Fallback triggered for zoneId: {}", zoneId, t);
    return new CcePlotResult(Collections.emptyList(), true);
  }
}
