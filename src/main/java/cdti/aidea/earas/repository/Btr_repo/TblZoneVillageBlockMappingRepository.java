package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.TblZoneVillageBlockMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TblZoneVillageBlockMappingRepository
    extends JpaRepository<TblZoneVillageBlockMapping, Long> {}
