package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.TblZoneRevenueVillageMapping;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TblZoneRevenueVillageMappingRepository
        extends JpaRepository<TblZoneRevenueVillageMapping, Long> {

  List<TblZoneRevenueVillageMapping> findByZone(Integer zoneId);

  Optional<TblZoneRevenueVillageMapping> findByRevenueVillage(Integer revenueVillage);

  Optional<TblZoneRevenueVillageMapping>
  findByZoneAndRevenueVillageAndIsValidTrue(
          Integer zone,
          Integer villageId
  );

  List<TblZoneRevenueVillageMapping>
  findByZoneAndIsValidTrue(Integer zone);
  List<TblZoneRevenueVillageMapping> findByZoneAndRevenueTalukAndIsValidTrue(
          Integer zone, Integer talukId);

//  List<TblZoneRevenueVillageMapping> findByZoneAndTalukAndIsValidTrue(Integer zone, Integer revenueTaluk);

  @Query("""
    SELECT v FROM TblZoneRevenueVillageMapping v
    JOIN TblMasterVillage mv ON v.revenueVillage = mv.villageId
    WHERE v.zone = :zone
      AND mv.revTalukId = :talukId
      AND v.isValid = true
""")
  List<TblZoneRevenueVillageMapping> findVillagesByZoneAndTaluk(
          Integer zone,
          Integer talukId
  );
}
