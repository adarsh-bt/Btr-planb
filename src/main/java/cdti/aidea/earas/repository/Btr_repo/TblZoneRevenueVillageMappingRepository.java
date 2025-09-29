package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.TblZoneRevenueVillageMapping;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TblZoneRevenueVillageMappingRepository
    extends JpaRepository<TblZoneRevenueVillageMapping, Long> {
  List<TblZoneRevenueVillageMapping> findByZone(Integer zoneId);

  Optional<TblZoneRevenueVillageMapping> findByRevenueVillage(Integer revenueVillage);
}
