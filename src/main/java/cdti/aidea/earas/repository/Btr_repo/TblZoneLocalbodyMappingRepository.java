package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.TblZoneLocalbodyMapping;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TblZoneLocalbodyMappingRepository
    extends JpaRepository<TblZoneLocalbodyMapping, Integer> {
  List<TblZoneLocalbodyMapping> findAllByZoneAndIsValid(Integer zone, Boolean isValid);

  @Query(value = """
    SELECT 
        zlm.zone_localbody_mapping_id,
        ml.localbody_id,
        ml.localbody_name_en
    FROM tbl_zone_localbody_mapping zlm
    JOIN tbl_master_localbody ml
         ON ml.localbody_id = zlm.localbody
    WHERE zlm.zone = :zoneId
      AND zlm.is_valid = true
""", nativeQuery = true)
  List<Object[]> findLocalBodiesByZone(Integer zoneId);

  @Query(value = """
    SELECT 
        ml.localbody_id,
        ml.localbody_name_en
    FROM tbl_master_localbody ml

    WHERE ml.dist_id = (
        SELECT mz.dist_id
        FROM tbl_master_zone mz
        WHERE mz.zone_id = :zoneId
    )

    AND ml.localbody_id NOT IN (
        SELECT zlm.localbody
        FROM tbl_zone_localbody_mapping zlm
        WHERE zlm.zone = :zoneId
          AND zlm.is_valid = true
    )

    ORDER BY ml.localbody_name_en
""", nativeQuery = true)
  List<Object[]> findAvailableLocalBodies(Integer zoneId);

  Optional<TblZoneLocalbodyMapping>
  findByZoneAndLocalbody(Integer zone, Integer localbody);
}
