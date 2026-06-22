package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.ZoneRevenueTalukMapping;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ZoneRevenueTalukMappingRepository
    extends JpaRepository<ZoneRevenueTalukMapping, Long> {
  List<ZoneRevenueTalukMapping> findByZoneAndIsValidTrue(Integer zone);

  //    List<ZoneRevenueTalukMapping> findByZone(Integer zone);
  // Custom queries can be added here if needed
  //    Optional<ZoneRevenueTalukMapping> findByZone(Integer zone);
  @Query(value = """
SELECT DISTINCT

    zrt.zone_revenue_taluk_mapping_id,
    rt.rev_taluk_id,
    rt.rev_taluk_name_en,

    zrv.zone_revenue_village_mapping_id,
    v.village_id,
    v.village_name_en,

    zvb.zone_village_block_mapping_id,
    vb.block_code

FROM tbl_zone_rev_taluk_updated_mapping zrt

JOIN tbl_master_taluk_revenue rt
     ON rt.rev_taluk_id = zrt.revenue_taluk

-- ✅ only valid villages
LEFT JOIN zone_revenue_village_mapping zrv
     ON zrv.zone = zrt.zone
     AND zrv.is_valid = true

LEFT JOIN tbl_master_village v
     ON v.village_id = zrv.revenue_village
     AND v.rev_taluk_id = rt.rev_taluk_id

-- ❌ REMOVE this join (very important)
-- LEFT JOIN tbl_master_village_block vb ON vb.village_id = v.village_id

-- ✅ ONLY mapped + valid blocks
LEFT JOIN tbl_zone_village_block_mapping zvb
     ON zvb.zone = zrt.zone
     AND zvb.village_id = v.village_id
     AND zvb.is_valid = true

LEFT JOIN tbl_master_village_block vb
     ON vb.village_block_id = zvb.village_block_id

WHERE zrt.zone = :zoneId
AND zrt.is_valid = true

ORDER BY rt.rev_taluk_name_en, v.village_name_en, vb.block_code
""", nativeQuery = true)
  List<Object[]> getZoneHierarchy(Integer zoneId);

//  findByZoneIdAndTalukIdAndIsValidTrue(Long zoneId, Integer talukId);
Optional<ZoneRevenueTalukMapping>
findByZoneAndRevenueTalukAndIsValidTrue(Integer zone, Integer revenueTaluk);


}
