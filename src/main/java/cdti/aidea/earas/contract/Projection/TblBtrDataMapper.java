package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.Response.TblBtrDataDTO;
import cdti.aidea.earas.model.Btr_models.TblBtrData;
import cdti.aidea.earas.model.Btr_models.TblNonBtr;
import cdti.aidea.earas.repository.Btr_repo.TblNonBtrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Extracted from TblBtrDataService#mapToEntity (unchanged logic) so it can be shared by
 * TblBtrDataService and KeyPlotTransactionService without creating a circular
 * dependency between those two services.
 */
@Component
@RequiredArgsConstructor
public class TblBtrDataMapper {

    private final TblNonBtrRepository tblNonBtrRepository;

    public TblBtrData mapToEntity(TblBtrDataDTO dto) {
        TblBtrData entity = new TblBtrData();
        entity.setDcode(dto.getDcode());
        entity.setTcode(dto.getTcode());
        entity.setVcode(dto.getVcode());
        entity.setBcode(dto.getBcode());
        entity.setLbcode(dto.getLbcode());
        entity.setLtype(dto.getLtype());
        entity.setLsgcode(dto.getLsgcode());
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
        // ✅ NEW BLOCK — add this
        LocalDate now = LocalDate.now();
        entity.setInsertionTime(LocalDateTime.now());
        entity.setUpdationTime(LocalDateTime.now());

        // Agreement year logic
        LocalDate agreStart = LocalDate.of(now.getYear(), 7, 1); // July 1 of current year
        LocalDate agreEnd = LocalDate.of(now.getYear() + 1, 6, 30); // June 30 of next year
        entity.setAgreStartYear(agreStart);
        entity.setAgreEndYear(agreEnd);
        return entity;
    }
}