package cdti.aidea.earas.service;

import cdti.aidea.earas.config.FormEntryClient;
import cdti.aidea.earas.contract.FormEntryDto.CceAssignmentRequest;
import cdti.aidea.earas.contract.RequestsDTOs.CropAssignmentTrailSaveDto;
import cdti.aidea.earas.model.Btr_models.ClusterMaster;
import cdti.aidea.earas.model.Btr_models.CropAssignmentTrail;
import cdti.aidea.earas.model.Btr_models.KeyPlots;
import cdti.aidea.earas.repository.Btr_repo.ClusterMasterRepository;
import cdti.aidea.earas.repository.Btr_repo.CropAssignmentTrailRepository;
import cdti.aidea.earas.repository.Btr_repo.KeyPlotsRepository;
import cdti.aidea.earas.utils.AgriYearUtil;
import feign.FeignException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CropAssignmentTrailService {

  private final CropAssignmentTrailRepository cropAssignmentTrailRepository;
  private final ClusterMasterRepository clusterMasterRepository;
  private final KeyPlotsRepository keyPlotsRepository;
  private final FormEntryClient formEntryClient;


  @Transactional
  public List<Long> saveCropAssignmentTrail(
          List<CropAssignmentTrailSaveDto> saveDtoList) {


    List<Long> savedIds = new ArrayList<>();

    for (CropAssignmentTrailSaveDto saveDto : saveDtoList) {

      try {

        log.info(
                "Processing crop assignment for cropId={}, clusterId={}",
                saveDto.getCropId(),
                saveDto.getClusterId());

        // =========================
        // VALIDATION
        // =========================

        if (saveDto.getCropId() == null) {
          throw new RuntimeException("CropId is required");
        }

        if (saveDto.getClusterId() == null) {
          throw new RuntimeException("ClusterId is required");
        }

        if (saveDto.getZoneId() == null) {
          throw new RuntimeException("ZoneId is required");
        }

        // =========================
        // FETCH CLUSTER
        // =========================

        ClusterMaster cluster =
                clusterMasterRepository
                        .findById(saveDto.getClusterId())
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Cluster not found: "
                                                        + saveDto.getClusterId()));

        // =========================
        // FETCH KEYPLOT
        // =========================

        KeyPlots keyPlot = null;

        if (saveDto.getKeyplotId() != null) {

          keyPlot =
                  keyPlotsRepository
                          .findById(saveDto.getKeyplotId())
                          .orElseThrow(
                                  () ->
                                          new RuntimeException(
                                                  "KeyPlot not found: "
                                                          + saveDto.getKeyplotId()));
        }

        // =========================
        // CHECK EXISTING RECORD
        // =========================

        Optional<CropAssignmentTrail> existingOpt =
                cropAssignmentTrailRepository
                        .findByCropIdAndCluster_CluMasterId(
                                saveDto.getCropId(),
                                saveDto.getClusterId());

        CropAssignmentTrail trail;

        if (existingOpt.isPresent()) {

          // =========================
          // UPDATE EXISTING
          // =========================

          trail = existingOpt.get();

          log.info(
                  "Existing crop assignment found. Updating record id={}",
                  trail.getId());

        } else {

          // =========================
          // CREATE NEW
          // =========================

          trail = new CropAssignmentTrail();

          trail.setCreatedAt(LocalDateTime.now());

          log.info("Creating new crop assignment");
        }

        // =========================
        // SET DATA
        // =========================

        trail.setCropId(saveDto.getCropId());

        trail.setCluster(cluster);

        trail.setKeyPlot(keyPlot);

        trail.setZoneId(saveDto.getZoneId());

        if (keyPlot != null) {
          trail.setLandType(keyPlot.getLandType());
        }

        trail.setIsRejected(
                saveDto.getIsRejected() != null
                        ? saveDto.getIsRejected()
                        : false);

        trail.setRejectionReason(saveDto.getRejectionReason());

        trail.setIsLimitExceeded(
                saveDto.getIsLimitExceeded() != null
                        ? saveDto.getIsLimitExceeded()
                        : false);

        trail.setIsCurrentAssignment(
                saveDto.getIsCurrentAssignment() != null
                        ? saveDto.getIsCurrentAssignment()
                        : true);

        trail.setRejectedBy(saveDto.getRejectedBy());

        trail.setRejectedAt(saveDto.getRejectedAt());

        trail.setAssignedOn(LocalDateTime.now());

        CropAssignmentTrail savedTrail =
                cropAssignmentTrailRepository.save(trail);

        savedIds.add(savedTrail.getId());

        log.info(
                "Crop assignment saved successfully. id={}",
                savedTrail.getId());

        CceAssignmentRequest cceRequest =
                new CceAssignmentRequest();

        if (keyPlot != null && keyPlot.getId() != null) {

          cceRequest.setPlotId(keyPlot.getId());

          if (keyPlot.getBtrData() != null) {

            cceRequest.setBtrId(
                    keyPlot.getBtrData().getId());

            cceRequest.setLbCode(
                    keyPlot.getBtrData().getLbcode());
          }
        } else {

          cceRequest.setPlotId(saveDto.getKeyplotId());
        }

        cceRequest.setClusterId(saveDto.getClusterId());

        cceRequest.setZoneId(
                Math.toIntExact(saveDto.getZoneId()));

        cceRequest.setCropId(saveDto.getCropId());

        cceRequest.setCceSourceType("RANDOM");

        cceRequest.setAddedBy(saveDto.getAddedBy());

        cceRequest.setLandType(saveDto.getLandType());


        LocalDate startDate = AgriYearUtil.getAgriYearStart(saveDto.getAgriYear());
        LocalDate endDate = AgriYearUtil.getAgriYearEnd(saveDto.getAgriYear());
        cceRequest.setAgriYear(saveDto.getAgriYear());

        cceRequest.setIsActive(
                !Boolean.TRUE.equals(saveDto.getIsRejected()));

        cceRequest.setIsSelected(
                Boolean.TRUE.equals(
                        trail.getIsCurrentAssignment()));

        try {

          formEntryClient.saveCceAssignment(cceRequest);

          log.info(
                  "External sync success for cropId={}",
                  saveDto.getCropId());

        } catch (FeignException e) {

          log.error(
                  "Feign error while syncing cropId={} status={} body={}",
                  saveDto.getCropId(),
                  e.status(),
                  e.contentUTF8());

        } catch (Exception e) {

          log.error(
                  "External sync failed for cropId={} error={}",
                  saveDto.getCropId(),
                  e.getMessage());
        }

      } catch (Exception e) {

        log.error(
                "Failed processing cropId={} error={}",
                saveDto.getCropId(),
                e.getMessage());
      }
    }

    return savedIds;
  }
}
