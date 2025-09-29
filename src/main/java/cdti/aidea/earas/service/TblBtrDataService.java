package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.Response.TblBtrDataDTO;
import cdti.aidea.earas.contract.ValidationErrorResponse;
import cdti.aidea.earas.model.Btr_models.*;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterVillage;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import cdti.aidea.earas.model.Btr_models.Masters.TblZoneRevenueVillageMapping;
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
  private final TblMasterVillageRepository tblMasterVillageRepository;
  private final TblZoneRevenueVillageMappingRepository tblZoneRevenueVillageMappingRepository;

  // ---------------- Single Save ----------------
  @Transactional
  public Map<String, Object> saveData(TblBtrDataDTO dto) {
    // ✅ Validate required fields
    List<String> requiredErrors = validateRequiredFields(dto);
    if (!requiredErrors.isEmpty()) {
      throw new RuntimeException("Validation failed: " + String.join(", ", requiredErrors));
    }

    // ✅ Validate duplicates
    validateDuplicate(dto);
    validateZoneMapping(dto);
    // 1️⃣ Save TblBtrData
    // ✅ Validate duplicates
    ValidationErrorResponse duplicateError = validateDuplicate(dto);
    if (duplicateError != null) {
      throw new RuntimeException("Duplicate entry detected: " + duplicateError.getMessage());
    }

    // 1️⃣ Save TblBtrData
    TblBtrData btrData = tblBtrDataRepository.save(mapToEntity(dto));

    // 2️⃣ Fetch zone by UUID
    Integer zoneUuid = dto.getZoneId();
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

    // ✅ Return saved entity id
    Map<String, Object> response = new HashMap<>();
    response.put("id", btrData.getId());
    return response;
  }

  // ---------------- Zone Validation ----------------
  private void validateZoneMapping(TblBtrDataDTO dto) {
    // 1️⃣ Fetch the zone
    Integer zoneId = dto.getZoneId();
    TblMasterZone zone =
        tblMasterZoneRepository
            .findById(zoneId)
            .orElseThrow(() -> new RuntimeException("Zone not found for zoneId=" + zoneId));

    // 2️⃣ Get all village mappings for this zone
    List<TblZoneRevenueVillageMapping> zoneMappings =
        tblZoneRevenueVillageMappingRepository.findByZone(zoneId);

    if (zoneMappings.isEmpty()) {
      throw new RuntimeException("No villages mapped for zoneId=" + zoneId);
    }

    // 3️⃣ Extract village IDs
    List<Integer> villageIds =
        zoneMappings.stream().map(TblZoneRevenueVillageMapping::getRevenueVillage).toList();

    // 4️⃣ Fetch all villages by IDs
    List<TblMasterVillage> villages = tblMasterVillageRepository.findAllById(villageIds);

    // 5️⃣ Check if the DTO's lsgcode exists in these villages
    boolean exists = villages.stream().anyMatch(v -> v.getLsgCode().equals(dto.getLsgcode()));

    if (!exists) {
      throw new RuntimeException(
          "Zone mismatch! Provided lsgCode="
              + dto.getLsgcode()
              + " does not belong to zoneId="
              + zoneId);
    }
  }

  // ---------------- Save All ----------------
  @Transactional
  public Map<String, Object> saveAllData(List<TblBtrDataDTO> dtoList) {
    List<ValidationErrorResponse> allErrors = new ArrayList<>();

    for (TblBtrDataDTO dto : dtoList) {
      // Required validation
      List<String> requiredErrors = validateRequiredFields(dto);
      if (!requiredErrors.isEmpty()) {
        allErrors.add(
            new ValidationErrorResponse(
                dto.getResvno(), dto.getResbdno(), String.join(", ", requiredErrors)));
      }

      // Duplicate validation
      ValidationErrorResponse duplicateError = validateDuplicate(dto);
      if (duplicateError != null) {
        allErrors.add(duplicateError);
      }
    }

    // ❌ If any errors → return list of errors
    if (!allErrors.isEmpty()) {
      Map<String, Object> response = new HashMap<>();
      response.put("status", "Validation Failed");
      response.put("errors", allErrors);
      return response;
    }

    // ✅ Save all and collect IDs
    List<Long> savedIds =
        dtoList.stream().map(dto -> (Long) saveData(dto).get("id")).collect(Collectors.toList());

    Map<String, Object> successResponse = new HashMap<>();
    successResponse.put("status", "Success");
    successResponse.put("message", "All records saved successfully");
    successResponse.put("ids", savedIds);
    return successResponse;
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

  // ---------------- Duplicate Validation ----------------
  private ValidationErrorResponse validateDuplicate(TblBtrDataDTO dto) {
    boolean exists =
        tblBtrDataRepository.existsByDcodeAndTcodeAndVcodeAndBcodeAndResvnoAndResbdno(
            dto.getDcode(),
            dto.getTcode(),
            dto.getVcode(),
            dto.getBcode(),
            dto.getResvno(),
            dto.getResbdno());

    if (exists) {
      return new ValidationErrorResponse(
          dto.getResvno(),
          dto.getResbdno(),
          "Duplicate entry already exists for resvno="
              + dto.getResvno()
              + " and resbdno="
              + dto.getResbdno());
    }
    return null;
  }

  // ---------------- Required Fields Validation ----------------
  private List<String> validateRequiredFields(TblBtrDataDTO dto) {
    List<String> errors = new ArrayList<>();

    if (dto.getDcode() == null) errors.add("District code (dcode) is required.");
    if (dto.getTcode() == null) errors.add("Taluk code (tcode) is required.");
    if (dto.getVcode() == null) errors.add("Village code (vcode) is required.");
    if (dto.getBcode() == null || dto.getBcode().trim().isEmpty())
      errors.add("Block code (bcode) is required.");
    if (dto.getResvno() == null) errors.add("Reservation number (resvno) is required.");
    if (dto.getResbdno() == null || dto.getResbdno().trim().isEmpty())
      errors.add("Reservation boundary number (resbdno) is required.");
    if (dto.getZoneId() == null)
      errors.add("Zone Id (zoneId) is required."); // ✅ Added zoneId validation

    return errors;
  }
}
