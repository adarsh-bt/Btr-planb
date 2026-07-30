package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.contract.Response.TalukDropdownResponse;
import cdti.aidea.earas.contract.Response.ZoneDropdownResponse;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import java.util.List;
import java.util.Optional;

import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TblMasterZoneRepository extends JpaRepository<TblMasterZone, Integer> {
  List<TblMasterZone> findByDesTalukId(Integer desTalukId);

  List<TblMasterZone> findByDistId(Integer desDistId);
    Optional<TblMasterZone> findByZoneId(Integer zoneId);

  @Query("SELECT z FROM TblMasterZone z WHERE z.zoneId = :zoneId AND z.isActive = true")
  Optional<TblMasterZone> findActiveZoneById(@Param("zoneId") Integer zoneId);

  Page<TblMasterZone> findAllByIsActiveTrue(Pageable pageable);
  Optional<TblMasterZone> findByZoneIdAndIsActiveTrue(Integer zoneId);

  @Query("""
    SELECT z FROM TblMasterZone z
    WHERE (
        LOWER(z.zoneNameEn) LIKE LOWER(CONCAT('%', :search, '%')) OR
        LOWER(z.zoneNameMal) LIKE LOWER(CONCAT('%', :search, '%')) OR
        CAST(z.zoneCode AS string) LIKE CONCAT('%', :search, '%')
    )
""")
  Page<TblMasterZone> searchZones(@Param("search") String search, Pageable pageable);

  Page<TblMasterZone> findByDesTalukId(Integer desTalukId, Pageable pageable);

  Page<TblMasterZone> findByDistId(Integer distId, Pageable pageable);

  Page<TblMasterZone> findAll(Pageable pageable);



  @Query("""
SELECT new cdti.aidea.earas.contract.Response.ZoneDropdownResponse(
    z.zoneId,
    z.zoneNameEn,
    z.btrType.btrType
)
FROM TblMasterZone z
WHERE (
    :type = 'Directorate'
    OR (:type = 'District' AND z.distId = :id)
    OR (:type = 'Taluk' AND z.desTalukId = :id)
)
ORDER BY z.zoneNameEn
""")
  List<ZoneDropdownResponse> findZoneDropdown(
          @Param("type") String type,
          @Param("id") Integer id);



    @Query("""
        SELECT DISTINCT new cdti.aidea.earas.contract.Response.TalukDropdownResponse(
            z.desTalukId,
            t.desTalukNameEn
        )
        FROM TblMasterZone z
        JOIN DesTaluk t
            ON t.desTalukId = z.desTalukId
        WHERE z.distId = :districtId
        ORDER BY t.desTalukNameEn
    """)
    List<TalukDropdownResponse> findTalukDropdown(
            @Param("districtId") Integer districtId);

  @Query(value = """
SELECT
    d.dist_id,
    d.dist_name_en,
    t.des_taluk_id,
    t.des_taluk_name_en,
    CASE
        WHEN zm.block_panchayat_muncipal_area = 1
        THEN b.block_id
        ELSE lb.localbody_id
    END AS blockId,
    CASE
        WHEN zm.block_panchayat_muncipal_area = 1
        THEN b.block_name
        ELSE lb.localbody_name_en
    END AS blockName,
    z.zone_name_en AS zoneName ,  -- added zone name
    lb.localbody_id AS localbodyId,
    lb.localbody_name_en AS localbodyName,
    lb.code_api AS lbCode
FROM tbl_master_zone z
LEFT JOIN tbl_master_taluk_des t
       ON z.des_taluk_id = t.des_taluk_id
LEFT JOIN tbl_master_district d
       ON z.dist_id = d.dist_id
LEFT JOIN tbl_zone_localbody_block_mapping zm
       ON zm.zone = z.zone_id
      AND zm.is_valid = true
LEFT JOIN tbl_master_block b
       ON zm.block_panchayat_muncipal_area = 1
      AND zm.block_details = b.block_id
LEFT JOIN tbl_master_localbody lb
       ON zm.block_panchayat_muncipal_area = 2
      AND zm.block_details = lb.localbody_id
WHERE z.zone_id = :zoneId
""", nativeQuery = true)
  List<Object[]> getZoneLocationDetails(@Param("zoneId") Integer zoneId);
}

