package cdti.aidea.earas.service;
import cdti.aidea.earas.model.Btr_models.Masters.*;
import cdti.aidea.earas.model.Btr_models.VillageLocalBodyMap;
import cdti.aidea.earas.repository.Btr_repo.LocalBodyRepository;
import cdti.aidea.earas.repository.Btr_repo.TblMasterVillageRepository;
import cdti.aidea.earas.repository.Btr_repo.VillageLocalBodyMapRepository;
//import cdti.aidea.earas.repository.VillageLocalBodyMapRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class VillageLocalBodyMapService {
        private final VillageLocalBodyMapRepository villageLocalBodyMapRepository;
        private final TblMasterVillageRepository villageRepository;
        private final LocalBodyRepository localBodyRepository;

        public VillageLocalBodyMap linkVillageToLocalbody(Integer villageId, Integer localbodyId) {
            TblMasterVillage village = villageRepository.findById(villageId)
                    .orElseThrow(() -> new EntityNotFoundException("Village not found with id " + villageId));

            TblLocalBody localbody = localBodyRepository.findById(localbodyId)
                    .orElseThrow(() -> new EntityNotFoundException("Localbody not found with id " + localbodyId));

            VillageLocalBodyMap map = VillageLocalBodyMap.builder()
                    .village(village)
                    .localBody(localbody)
                    .isActive(true)
                    .build();

            return villageLocalBodyMapRepository.save(map);
        }
    }


