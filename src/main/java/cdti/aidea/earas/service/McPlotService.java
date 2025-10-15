package cdti.aidea.earas.service;
import cdti.aidea.earas.contract.Response.McPlotDTO;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterVillage;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import cdti.aidea.earas.model.Btr_models.TblBtrData;
import cdti.aidea.earas.repository.Btr_repo.TblBtrDataRepository;
import cdti.aidea.earas.repository.Btr_repo.TblMasterVillageRepository;
import cdti.aidea.earas.repository.Btr_repo.TblMasterZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class McPlotService {
    private final TblBtrDataRepository tblBtrDataRepository;
    private final TblMasterZoneRepository tblMasterZoneRepository;
    private final TblMasterVillageRepository tblMasterVillageRepository;
    public TblBtrData saveMcPlotData(McPlotDTO dto) {

        // ✅ 1. Duplicate check for resbdno
        if (tblBtrDataRepository.existsByResbdno(dto.getResbdno())) {
            throw new RuntimeException("Duplicate Entry: resbdno '" + dto.getResbdno() + "' already exists!");
        }

        // 1. Fetch zone details
        TblMasterZone zone = tblMasterZoneRepository.findByZoneId(dto.getZoneId())
                .orElseThrow(() -> new RuntimeException("Zone not found for zoneId: " + dto.getZoneId()));

        // 2. Fetch village details
        TblMasterVillage village = tblMasterVillageRepository.findByVillageId(dto.getVcode())
                .orElseThrow(() -> new RuntimeException("Village not found for vcode: " + dto.getVcode()));

        TblBtrData entity = new TblBtrData();
        entity.setVcode(dto.getVcode());
        entity.setBcode(dto.getBcode());
        entity.setResvno(dto.getResvno());
        entity.setResbdno(dto.getResbdno());
        entity.setLtype(dto.getLtype());
        entity.setLbcode(dto.getLbcode());
        entity.setTotCent(dto.getTotCent());
        entity.setOwnername(dto.getOwnername());
        entity.setAddress(dto.getAddress());
        // auto-fill values
        entity.setTcode(zone.getDesTalukId());
        entity.setDcode(zone.getDistId());
        entity.setLsgcode(village.getLsgCode());
        // ✅ Added: automatic timestamp and agreement date setup
        entity.setInsertionTime(LocalDateTime.now());
        entity.setUpdationTime(LocalDateTime.now());

        LocalDate now = LocalDate.now();
        entity.setAgreStartYear(LocalDate.of(now.getYear(), 7, 1));   // 01-July-current year
        entity.setAgreEndYear(LocalDate.of(now.getYear() + 1, 6, 30)); // 30-June-next year
        // Save entity to DB
        return tblBtrDataRepository.save(entity);
    }

}
