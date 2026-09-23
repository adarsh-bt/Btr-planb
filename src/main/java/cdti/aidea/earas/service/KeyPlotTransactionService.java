package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.Response.TblBtrDataDTO;
import cdti.aidea.earas.model.Btr_models.ClusterFormData;
import cdti.aidea.earas.model.Btr_models.ClusterMaster;
import cdti.aidea.earas.model.Btr_models.KeyPlots;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import cdti.aidea.earas.model.Btr_models.TblBtrData;
import cdti.aidea.earas.repository.Btr_repo.ClusterFormDataRepository;
import cdti.aidea.earas.repository.Btr_repo.ClusterMasterRepository;
import cdti.aidea.earas.repository.Btr_repo.KeyPlotsRepository;
import cdti.aidea.earas.repository.Btr_repo.TblBtrDataRepository;
import cdti.aidea.earas.repository.Btr_repo.TblMasterZoneRepository;
import cdti.aidea.earas.utils.AgriYearUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeyPlotTransactionService {

    private final TblBtrDataRepository tblBtrDataRepository;
    private final KeyPlotsRepository keyPlotsRepository;
    private final ClusterMasterRepository clusterMasterRepository;
    private final ClusterFormDataRepository clusterFormDataRepository;
    private final TblMasterZoneRepository tblMasterZoneRepository;
    private final cdti.aidea.earas.service.TblBtrDataMapper tblBtrDataMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long saveSinglePlot(TblBtrDataDTO dto) {
        TblMasterZone zone = tblMasterZoneRepository
                .findById(Math.toIntExact(dto.getZoneId()))
                .orElseThrow(() -> new RuntimeException("Zone not found"));

        LocalDate startDate = AgriYearUtil.getAgriYearStart(dto.getAgriYear());
        LocalDate endDate = AgriYearUtil.getAgriYearEnd(dto.getAgriYear());

        TblBtrData btrData;
        if (dto.getId() != null) {
            // BTR is permanent/reusable data — reuse the existing row, never re-insert it.
            btrData = tblBtrDataRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Existing BTR Plot not found for ID: " + dto.getId()));
        } else {
            btrData = tblBtrDataRepository.save(tblBtrDataMapper.mapToEntity(dto));
        }

        Optional<Integer> maxClusterNumberOpt =
                clusterMasterRepository.findMaxClusterNumberByAgriYear(
                        zone.getZoneId(),
                        startDate,
                        endDate
                );
        int nextClusterNumber = maxClusterNumberOpt.orElse(0) + 1;

        // Save KeyPlots
        KeyPlots keyPlot = new KeyPlots();
        keyPlot.setBtrData(btrData);
        keyPlot.setZone(zone);
        keyPlot.setIntervals(1);
        keyPlot.setAgriStartYear(startDate);
        keyPlot.setAgriEndYear(endDate);
        keyPlot.setIsRejected(false);
        keyPlot.setStatus(true);
        keyPlot.setLandType(btrData.getLtype());
        keyPlot.setCreated_by(dto.getUser_id());


        keyPlotsRepository.saveAndFlush(keyPlot);

        ClusterMaster clusterMaster = new ClusterMaster();
        clusterMaster.setKeyPlot(keyPlot);
        clusterMaster.setClusterNumber(nextClusterNumber);
        clusterMaster.setZone(zone);
        clusterMaster.setStatus("Not Started");
        clusterMaster.setIsReject(false);
        clusterMaster.setIs_active(true);
        clusterMaster.setIs_editable(true);
        clusterMasterRepository.save(clusterMaster);

        // Save ClusterFormData
        ClusterFormData clusterFormData = new ClusterFormData();
        clusterFormData.setClusterMaster(clusterMaster);
        clusterFormData.setPlot(btrData);
        clusterFormData.setPlotLabel("K");
        clusterFormData.setEnumeratedArea(btrData.getTotCent());
        clusterFormData.setCreatedBy(dto.getUser_id());
        clusterFormData.setStatus(true);
        clusterFormDataRepository.save(clusterFormData);

        return btrData.getId();
    }
}