package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.Response.TblBtrDataDTO;
import cdti.aidea.earas.contract.ValidationErrorResponse;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import cdti.aidea.earas.model.Btr_models.Masters.ZoneRevenueTalukMapping;
import cdti.aidea.earas.model.Btr_models.TblBtrData;
import cdti.aidea.earas.model.Btr_models.TblNonBtr;
import cdti.aidea.earas.repository.Btr_repo.TblBtrDataRepository;
import cdti.aidea.earas.repository.Btr_repo.TblMasterZoneRepository;
import cdti.aidea.earas.repository.Btr_repo.TblNonBtrRepository;
import cdti.aidea.earas.repository.Btr_repo.ZoneRevenueTalukMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutOfClusterService {
    private final TblBtrDataRepository tblBtrDataRepository;
    private final TblNonBtrRepository tblNonBtrRepository;
    private final TblMasterZoneRepository tblMasterZoneRepository;
    private final ZoneRevenueTalukMappingRepository zoneRevenueTalukMappingRepository;

    @Transactional
    public Map<String, Object> saveBtr(TblBtrDataDTO dto) {

        Map<String, Object> response = new HashMap<>();

        // Required field validation
        List<String> requiredErrors = validateRequiredFields(dto);

        if (!requiredErrors.isEmpty()) {

            List<ValidationErrorResponse> errors = new ArrayList<>();

            errors.add(new ValidationErrorResponse(
                    dto.getResvno(),
                    dto.getResbdno(),
                    dto.getWardno(),
                    dto.getHouseno(),
                    dto.getTotCent(),
                    String.join(", ", requiredErrors)
            ));

            response.put("status", "Validation Failed");
            response.put("errors", errors);

            return response;
        }

        // Save only in tbl_btr_data
        TblBtrData savedBtr = tblBtrDataRepository.save(mapToEntity(dto));

        response.put("btrId", savedBtr.getId());
        response.put("message", "Successfully Saved");

        return response;
    }

    // ---------------- Required Fields Validation ----------------
    private List<String> validateRequiredFields(TblBtrDataDTO dto) {
        List<String> errors = new ArrayList<>();

//        if (dto.getDcode() == null) errors.add("District code (dcode) is required.");
//        if (dto.getTcode() == null) errors.add("Taluk code (tcode) is required.");
     //   if (dto.getDcode() == null) errors.add("District code (dcode) is required.");
      //  if (dto.getTcode() == null) errors.add("Taluk code (tcode) is required.");
        if (dto.getVcode() == null) errors.add("Village code (vcode) is required.");
        if (dto.getBcode() == null || dto.getBcode().trim().isEmpty())
            errors.add("Block code (bcode) is required.");
        if (dto.getBtrtype() == 1) {
            if (dto.getResvno() == null) errors.add("Reservation number (resvno) is required.");
        }
        if (dto.getZoneId() == null) errors.add("Zone Id (zoneId) is required.");

        return errors;
    }

    //   // ---------------- DTO -> Entity Mapper ----------------
    private TblBtrData mapToEntity(TblBtrDataDTO dto) {
        TblBtrData entity = new TblBtrData();
        Optional<TblMasterZone> zone =
                tblMasterZoneRepository.findById(dto.getZoneId());

        List<ZoneRevenueTalukMapping> mappings =
                zoneRevenueTalukMappingRepository.findByZoneAndIsValidTrue(dto.getZoneId());

        entity.setDcode(zone.get().getDistId());

        if (!mappings.isEmpty()) {
            entity.setTcode(mappings.get(0).getRevenueTaluk());
        }
        entity.setDcode(zone.get().getDistId());
//        entity.setTcode(mappings.get(0).getRevenueTaluk());
        entity.setDcode(dto.getDcode());
        entity.setTcode(dto.getTcode());
        entity.setVcode(dto.getVcode());
        entity.setBcode(dto.getBcode());
        entity.setLbcode(dto.getLbcode());
        entity.setLtype(dto.getLtype());
        entity.setLsgcode(dto.getLsgcode());
//        entity.setLsgcode(dto.getLsgcode());
        entity.setTotCent(dto.getTotCent());
        entity.setResvno(dto.getResvno());
        entity.setResbdno(dto.getResbdno());
        entity.setCreated_by(dto.getUser_id());
        entity.setUpdated_by(dto.getUser_id());
        entity.setInsertionTime(LocalDateTime.now());
        entity.setUpdationTime(LocalDateTime.now());
        entity.setZone(Long.valueOf(dto.getZoneId()));
// 🧩 Determine type-based mapping
        if (dto.getBtrtype() != null) {
            long typeId = dto.getBtrtype();
            Optional<TblNonBtr> nonBtr = tblNonBtrRepository.findById(dto.getBtrtype());
            // Type 1 → dcode to resbdno
            if (typeId == 1) {
                entity.setBtrtype(nonBtr.get());
            }
            // Type 2 → dcode to totcent + ownername, address, houseno
            else if (typeId == 2) {
                entity.setOwnername(dto.getOwnername());
                entity.setAddress(dto.getAddress());
                entity.setHouseno(dto.getHouseno());
                entity.setWardnumber(dto.getWardno());
                entity.setBtrtype(nonBtr.get());
            }
            // Type 3 → dcode to totcent + ownername, address
            // but not resvno/resbdno
            else if (typeId == 3) {
                entity.setOwnername(dto.getOwnername());
                entity.setAddress(dto.getAddress());
                entity.setBtrtype(nonBtr.get());
            }
            // Type 4 → dcode to totcent + ownername, address, tpno, tpsubdno
            // (mapped to mainno and subno)
            else if (typeId == 4) {
                entity.setOwnername(dto.getOwnername());
                entity.setAddress(dto.getAddress());
                entity.setTpno(dto.getTpno());
                entity.setTbsubdivisionno(dto.getTbsubdivisionno());
                entity.setBtrtype(nonBtr.get());
            }
            // Type 5 → dcode to totcent + ownername, address, mainno, subno
            // but not resvno/resbdno
            else if (typeId == 5) {
                entity.setOwnername(dto.getOwnername());
                entity.setOldsvno(dto.getOldsvno());
                entity.setOldsubno(dto.getOldsubno());
                entity.setBtrtype(nonBtr.get());
            }
        }
        return entity;
    }
}