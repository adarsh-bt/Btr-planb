package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.Response.TblBtrDataDTO;
import cdti.aidea.earas.contract.ValidationErrorResponse;
import cdti.aidea.earas.model.Btr_models.*;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import cdti.aidea.earas.repository.Btr_repo.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    // ✅ Validate required fields
    validateRequiredFields(dto);

    // ✅ Validate duplicates
    validateDuplicate(dto);
    // 1️⃣ Save TblBtrData
    TblBtrData btrData = tblBtrDataRepository.save(mapToEntity(dto));

    // 2️⃣ Fetch zone by UUID
    Integer zoneUuid = dto.getZoneId(); // ensure dto.getZoneId() is String
    TblMasterZone zone =
        tblMasterZoneRepository
            .findById(zoneUuid)
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
    //        response.put("btrData", btrData);
    //        response.put("keyPlot", keyPlot);
    response.put("id", btrData.getId());
    return response;
  }

  @Transactional
  public Map<String, Object> saveAllData(List<TblBtrDataDTO> dtoList) {
    List<ValidationErrorResponse> allErrors = new ArrayList<>();

    for (TblBtrDataDTO dto : dtoList) {
      if (dto.getResvno() == null
          || dto.getResbdno() == null
          || dto.getResbdno().trim().isEmpty()) {
        allErrors.add(
            new ValidationErrorResponse(
                dto.getResvno(),
                dto.getResbdno(),
                "Required fields missing: resvno and/or resbdno"));
      }

      ValidationErrorResponse duplicateError = validateDuplicate(dto);
      if (duplicateError != null) {
        allErrors.add(duplicateError);
      }
    }

    if (!allErrors.isEmpty()) {
      Map<String, Object> response = new HashMap<>();
      response.put("status", "Validation Failed");
      response.put("errors", allErrors);
      return response;
    }
    // Save all and collect IDs only
    List<Long> savedIds =
        dtoList.stream().map(dto -> (Long) saveData(dto).get("id")).collect(Collectors.toList());

    List<Map<String, Object>> savedData =
        dtoList.stream().map(this::saveData).collect(Collectors.toList());

    Map<String, Object> successResponse = new HashMap<>();
    successResponse.put("status", "Success");
    successResponse.put("message", "All records saved successfully");
    successResponse.put("ids", savedIds);
    return successResponse;
  }

  private ValidationErrorResponse validateDuplicate(TblBtrDataDTO dto) {
    if (dto.getResvno() != null && dto.getResbdno() != null) {
      boolean exists =
          tblBtrDataRepository.existsByResvnoAndResbdno(dto.getResvno(), dto.getResbdno());
      if (exists) {
        return new ValidationErrorResponse(
            dto.getResvno(),
            dto.getResbdno(),
            "Duplicate entry already exists for resvno="
                + dto.getResvno()
                + " and resbdno="
                + dto.getResbdno());
      }
    }
    return null; // no error
  }

  private void validateRequiredFields(TblBtrDataDTO dto) {
    List<String> errors = new ArrayList<>();
    if (dto.getDcode() == null) errors.add("District code (dcode) is required.");
    if (dto.getTcode() == null) errors.add("Taluk code (tcode) is required.");
    if (dto.getVcode() == null) errors.add("Village code (vcode) is required.");
    if (dto.getBcode() == null || dto.getBcode().trim().isEmpty())
      errors.add("Block code (bcode) is required.");
    if (dto.getResvno() == null) errors.add("Reservation number (resvno) is required.");
    if (dto.getResbdno() == null || dto.getResbdno().trim().isEmpty())
      errors.add("Reservation boundary number (resbdno) is required.");
    if (!errors.isEmpty()) {
      throw new RuntimeException("Validation failed: " + String.join(" ", errors));
    }
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
}
