package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.Response.TblBtrDataDTO;
import cdti.aidea.earas.model.Btr_models.*;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import cdti.aidea.earas.repository.Btr_repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TblBtrDataService {

    private final TblBtrDataRepository tblBtrDataRepository;
    private final KeyPlotsRepository keyPlotsRepository;
    private final ClusterMasterRepository clusterMasterRepository;
    private final ClusterFormDataRepository clusterFormDataRepository;
    private final TblMasterZoneRepository tblMasterZoneRepository;

    // ---------------- Single Save ----------------
    @Transactional
    public Map<String, Object> saveData(TblBtrDataDTO dto) {
        // 1️⃣ Save TblBtrData
        TblBtrData btrData = tblBtrDataRepository.save(mapToEntity(dto));

        // 2️⃣ Fetch zone by UUID
        Integer zoneUuid = dto.getZoneId(); // ensure dto.getZoneId() is String
        TblMasterZone zone = tblMasterZoneRepository.findById(zoneUuid)
                .orElseThrow(() -> new RuntimeException("Zone not found"));

        // 3️⃣ Save KeyPlots
        KeyPlots keyPlot = new KeyPlots();
        keyPlot.setBtrData(btrData);
        keyPlot.setZone(zone);
        keyPlot.setIntervals(1);
        keyPlot.setAgriStartYear(LocalDate.now());
        keyPlot.setAgriEndYear(LocalDate.now().plusYears(1));
        keyPlot.setIsRejected(false);
        keyPlot.setStatus(true);
        keyPlot.setLandType(btrData.getLtype());
        keyPlot.setCreated_by(UUID.randomUUID());
        keyPlot = keyPlotsRepository.save(keyPlot);

        // 4️⃣ Save ClusterMaster
        ClusterMaster clusterMaster = new ClusterMaster();
        clusterMaster.setKeyPlot(keyPlot);
        clusterMaster.setClusterNumber(1);
        clusterMaster.setStatus("Not Started");
        clusterMaster.setIsReject(false);
        clusterMaster.setIs_active(true);
        clusterMaster.setZone(zone);
        clusterMasterRepository.save(clusterMaster);

        // 5️⃣ Save ClusterFormData
        ClusterFormData clusterFormData = new ClusterFormData();
        clusterFormData.setClusterMaster(clusterMaster);
        clusterFormData.setPlot(btrData);
        clusterFormData.setPlotLabel("Plot-" + btrData.getId());
        clusterFormData.setEnumeratedArea(btrData.getNare() != null ? btrData.getNare() : 0.0);
        clusterFormData.setCreatedBy(UUID.randomUUID());
        clusterFormData.setStatus(true);
        clusterFormDataRepository.save(clusterFormData);

        // ✅ Return saved entities
        Map<String, Object> response = new HashMap<>();
        response.put("btrData", btrData);
        response.put("keyPlot", keyPlot);
        return response;
    }

    // ---------------- Bulk Save ----------------
    @Transactional
    public List<Map<String, Object>> saveAllData(List<TblBtrDataDTO> dtoList) {
        return dtoList.stream()
                .map(this::saveData)
                .collect(Collectors.toList());
    }

    // ---------------- DTO -> Entity Mapper ----------------
    private TblBtrData mapToEntity(TblBtrDataDTO dto) {
        TblBtrData entity = new TblBtrData();
        entity.setDcode(dto.getDcode());
        entity.setTcode(dto.getTcode());
        entity.setVcode(dto.getVcode());
        entity.setBcode(dto.getBcode());
        entity.setLbcode(dto.getLbcode());
        entity.setLtype(dto.getLtype());
        entity.setResvno(dto.getResvno());
        entity.setResbdno(dto.getResbdno());
        entity.setLsgcode(dto.getLsgcode());
        return entity;
    }
}
