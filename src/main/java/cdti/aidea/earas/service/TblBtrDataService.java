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
        // 🔎 Validation: check if resvno already exists
        tblBtrDataRepository.findByResvno(dto.getResvno())
                .ifPresent(existing -> {
                    throw new RuntimeException("Duplicate resvno not allowed: " + dto.getResvno());
                });
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
        clusterFormData.setPlotLabel("K");
        clusterFormData.setEnumeratedArea(btrData.getTotCent());
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
    // ---------------- Get All ----------------
    public List<TblBtrDataDTO> getAllData() {
        return tblBtrDataRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

    }
    // ---------------- Get By Zone ----------------
    public List<TblBtrDataDTO> getDataByZoneId(Integer zoneId) {
        return tblBtrDataRepository.findByZoneId(zoneId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

  //  public List<Map<String, Object>> getDataClustersByZone(Integer zoneId) {
        List<TblBtrData> btrDataList = tblBtrDataRepository.findAll().stream()
                .filter(btr -> btr.getZoneId() != null && btr.getZoneId().equals(zoneId))
                .collect(Collectors.toList());

        if (btrDataList.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, List<TblBtrData>> groupedByPanchayat = btrDataList.stream()
                .collect(Collectors.groupingBy(TblBtrData::getLbcode));

        List<Map<String, Object>> clusterList = new ArrayList<>();
        int globalSlNo = 1;

        for (Map.Entry<String, List<TblBtrData>> entry : groupedByPanchayat.entrySet()) {
            String panchayath = entry.getKey();
            List<TblBtrData> plots = entry.getValue();

            List<Map<String, Object>> wetSamples = new ArrayList<>();
            List<Map<String, Object>> drySamples = new ArrayList<>();
            double wetArea = 0, dryArea = 0;

            for (TblBtrData plot : plots) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", plot.getId());
                row.put("no", globalSlNo++);
                row.put("panchayath", panchayath);
                row.put("Sy. No", plot.getResvno() + "/" + plot.getResbdno());
                row.put("Area (Cents)", plot.getTotCent());
                row.put("Village/Block", plot.getBcode());

                if ("Wet".equalsIgnoreCase(plot.getLtype())) {
                    wetSamples.add(row);
                    wetArea += plot.getTotCent();
                } else if ("Dry".equalsIgnoreCase(plot.getLtype())) {
                    drySamples.add(row);
                    dryArea += plot.getTotCent();
                }
            }

            Map<String, Object> clusterData = new HashMap<>();
            clusterData.put("panchayath", panchayath);
            clusterData.put("wetSamples", wetSamples);
            clusterData.put("drySamples", drySamples);
            clusterData.put("wetarea", Math.round(wetArea));
            clusterData.put("dryarea", Math.round(dryArea));
            clusterData.put("totalarea", Math.round(wetArea + dryArea));

            clusterList.add(clusterData);
        }

        return clusterList;
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
        entity.setTotCent(dto.getTotCent());
        return entity;
    }
    // ---------------- Entity -> DTO Mapper ----------------
    private TblBtrDataDTO mapToDto(TblBtrData entity) {
        TblBtrDataDTO dto = new TblBtrDataDTO();
        dto.setDcode(entity.getDcode());
        dto.setTcode(entity.getTcode());
        dto.setVcode(entity.getVcode());
        dto.setBcode(entity.getBcode());
        dto.setLbcode(entity.getLbcode());
        dto.setLtype(entity.getLtype());
        dto.setResvno(entity.getResvno());
        dto.setResbdno(entity.getResbdno());
        dto.setLsgcode(entity.getLsgcode());
        // zoneId and user_id are not directly in entity, setting null
        dto.setZoneId(null);
        dto.setUser_id(null);
        return dto;
    }
}
