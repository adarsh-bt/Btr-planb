package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.Response.VillageResponseDTO;
import cdti.aidea.earas.repository.Btr_repo.TblMasterVillageRepository;  // ✅ CORRECT IMPORT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VillageService {

    @Autowired
    private TblMasterVillageRepository villageRepository;  // ✅ CORRECT INJECTION

    public List<VillageResponseDTO> getVillagesByZoneAndLocalBody(Integer zoneId, Integer localBodyId) {

        List<Object[]> results = villageRepository.findVillagesByZoneAndLocalBody(zoneId, localBodyId);

        return results.stream()
                .map(row -> new VillageResponseDTO(
                        (Integer) row[0],  // village_id
                        (String) row[1],   // village_name_en
                        (Integer) row[2],  // localbody_id
                        (String) row[3],   // localbody_name_en
                        (Integer) row[4],  // zone_id
                        (String) row[5]    // zone_name_en
                ))
                .collect(Collectors.toList());
    }
}
