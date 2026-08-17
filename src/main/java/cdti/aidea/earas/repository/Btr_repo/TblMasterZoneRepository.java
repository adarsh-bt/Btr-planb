package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.contract.RequestsDTOs.ZoneUserAssignDto;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import java.util.List;
import java.util.Optional;

import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
//import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TblMasterZoneRepository extends JpaRepository<TblMasterZone, Integer> {
  List<TblMasterZone> findByDesTalukId(Integer desTalukId);

  List<TblMasterZone> findByDistId(Integer desDistId);
    Optional<TblMasterZone> findByZoneId(Integer zoneId);

    //List<TblMasterZone> findByDistId(Integer distId);
   // List<TblMasterZone> findByDesTalukId(Integer talukId);


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


   @Query("""
       SELECT new cdti.aidea.earas.contract.RequestsDTOs.ZoneUserAssignDto(
            z.zoneId,
            z.zoneNameEn,
            z.desDistId,
            z.desTalukId,
            CASE
                WHEN uza.id IS NOT NULL THEN true
                ELSE false
            END,
            uza.userId
        )
        FROM TblMasterZone z
        LEFT JOIN UserZoneAssignment uza
               ON uza.tblMasterZone.zoneId = z.zoneId
              AND uza.isActive = true
        WHERE z.isActive = true
        ORDER BY z.zoneId
    """)
    List<ZoneUserAssignDto> findActiveZonesWithAssignment();

    Page<TblMasterZone> findByZoneNameEnContainingIgnoreCaseOrZoneCodeContainingIgnoreCase(
            String name, String code, Pageable pageable);

  //  to fetch the zone location needs to use in form-service
//    @Query(value = """
//SELECT
//    d.dist_id,
//    d.dist_name_en,
//
//    t.des_taluk_id,
//    t.des_taluk_name_en,
//
//    CASE
//        WHEN zm.block_panchayat_muncipal_area = 1
//        THEN b.block_id
//        ELSE lb.localbody_id
//    END AS blockId,
//
//    CASE
//        WHEN zm.block_panchayat_muncipal_area = 1
//        THEN b.block_name
//        ELSE lb.localbody_name_en
//    END AS blockName
//
//FROM tbl_master_zone z
//
//LEFT JOIN tbl_master_taluk_des t
//       ON z.des_taluk_id = t.des_taluk_id
//
//LEFT JOIN tbl_master_district d
//       ON z.dist_id = d.dist_id
//
//LEFT JOIN tbl_zone_localbody_block_mapping zm
//       ON zm.zone = z.zone_id
//      AND zm.is_valid = true
//
//LEFT JOIN tbl_master_block b
//       ON zm.block_panchayat_muncipal_area = 1
//      AND zm.block_details = b.block_id
//
//LEFT JOIN tbl_master_localbody lb
//       ON zm.block_panchayat_muncipal_area = 2
//      AND zm.block_details = lb.localbody_id
//
//WHERE z.zone_id = :zoneId
//""", nativeQuery = true)
//   List<Object[]> getZoneLocationDetails(@Param("zoneId") Integer zoneId);

  // /zone-location/{zoneId}
//    @Query(value = """
//SELECT
//    d.dist_id,
//    d.dist_name_en,
//    t.des_taluk_id,
//    t.des_taluk_name_en,
//    CASE
//        WHEN zm.block_panchayat_muncipal_area = 1
//        THEN b.block_id
//        ELSE lb.localbody_id
//    END AS blockId,
//    CASE
//        WHEN zm.block_panchayat_muncipal_area = 1
//        THEN b.block_name
//        ELSE lb.localbody_name_en
//    END AS blockName,
//    z.zone_name_en AS zoneName ,  -- added zone name
//    lb.localbody_id AS localbodyId,
//    lb.localbody_name_en AS localbodyName,
//    lb.code_api AS lbCode
//FROM tbl_master_zone z
//LEFT JOIN tbl_master_taluk_des t
//       ON z.des_taluk_id = t.des_taluk_id
//LEFT JOIN tbl_master_district d
//       ON z.dist_id = d.dist_id
//LEFT JOIN tbl_zone_localbody_block_mapping zm
//       ON zm.zone = z.zone_id
//      AND zm.is_valid = true
//LEFT JOIN tbl_master_block b
//       ON zm.block_panchayat_muncipal_area = 1
//      AND zm.block_details = b.block_id
//LEFT JOIN tbl_master_localbody lb
//       ON zm.block_panchayat_muncipal_area = 2
//      AND zm.block_details = lb.localbody_id
//WHERE z.zone_id = :zoneId
//""", nativeQuery = true)
//    List<Object[]> getZoneLocationDetails(@Param("zoneId") Integer zoneId);

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
    z.zone_name_en
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

