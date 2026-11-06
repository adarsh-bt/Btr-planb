package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.TblWorkAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TblWorkAllocationRepository extends JpaRepository<TblWorkAllocation, Long> {
    List<TblWorkAllocation> findByZone_ZoneId(Integer zoneId);
    Optional<TblWorkAllocation> findByLbcodeAndZone_ZoneId(String lbcode, Integer zoneId);

    @Query("""
    SELECT t
    FROM TblWorkAllocation t
    WHERE t.zone.zoneId = :zoneId
    AND t.agriStart = :agriStart
    AND t.agriEnd = :agriEnd
""")
    List<TblWorkAllocation> findByZoneAndAgriYear(
            @Param("zoneId") Integer zoneId,
            @Param("agriStart") LocalDate agriStart,
            @Param("agriEnd") LocalDate agriEnd);

    List<TblWorkAllocation> findByApprovalId(Long approvalId);

    Optional<TblWorkAllocation>
    findByLbcodeAndZone_ZoneIdAndAgriStartAndAgriEnd(
            String lbcode,
            Integer zoneId,
            LocalDate agriStart,
            LocalDate agriEnd
    );
    //work allocation report district wise all kerala
    List<TblWorkAllocation> findByAgriStartAndAgriEndAndIsActiveTrue(
            LocalDate agriStart,
            LocalDate agriEnd
    );

    @Query("""
        SELECT wa
        FROM TblWorkAllocation wa
        JOIN FETCH wa.zone z
        LEFT JOIN FETCH z.districtMaster d
        WHERE wa.isActive = true
          AND wa.agriStart = :agriStart
          AND wa.agriEnd = :agriEnd
    """)
    List<TblWorkAllocation> findDashboardAllocations(
            @Param("agriStart") LocalDate agriStart,
            @Param("agriEnd") LocalDate agriEnd
    );

    @Query("""
    SELECT wa
    FROM TblWorkAllocation wa
    JOIN FETCH wa.zone z
    WHERE z.desTalukId = :talukId
      AND wa.isActive = true
      AND wa.agriStart = :agriStart
      AND wa.agriEnd = :agriEnd
""")
    List<TblWorkAllocation> findZoneDashboardAllocations(
            @Param("talukId") Integer talukId,
            @Param("agriStart") LocalDate agriStart,
            @Param("agriEnd") LocalDate agriEnd
    );

    @Query("""
    SELECT wa
    FROM TblWorkAllocation wa
    JOIN FETCH wa.zone z
    WHERE z.districtMaster.dist_id = :districtId
      AND wa.isActive = true
      AND wa.agriStart = :agriStart
      AND wa.agriEnd = :agriEnd
""")
    List<TblWorkAllocation> findTalukDashboardAllocations(
            @Param("districtId") Integer districtId,
            @Param("agriStart") LocalDate agriStart,
            @Param("agriEnd") LocalDate agriEnd
    );

//to connect with form based on toatal wet cluster
    @Query(value = """
        SELECT *
        FROM tbl_work_allocation
        WHERE zone_id = :zoneId
          AND agri_start = :agriStart
          AND agri_end = :agriEnd
          AND total_wet_cluster > 0
          AND is_active = true
        ORDER BY lbcode
        """, nativeQuery = true)
    List<TblWorkAllocation> getWetClusterLbcodes(
            @Param("zoneId") Long zoneId,
            @Param("agriStart") LocalDate agriStart,
            @Param("agriEnd") LocalDate agriEnd);

    //connect with form to zone wise -> talukid
    @Query(value = """
    SELECT
        z.zone_id,
        z.zone_name_en,
        COUNT(DISTINCT wa.lbcode) AS lbcode_count
    FROM tbl_work_allocation wa
    JOIN tbl_master_zone z
        ON z.zone_id = wa.zone_id
    WHERE z.des_taluk_id = :talukId
      AND z.is_active = true
      AND wa.agri_start = :agriStart
      AND wa.agri_end = :agriEnd
      AND wa.total_wet_cluster > 0
      AND wa.is_active = true
    GROUP BY z.zone_id, z.zone_name_en
    ORDER BY z.zone_name_en
    """, nativeQuery = true)
    List<Object[]> getWetClusterLbcodesByTaluk(
            @Param("talukId") Long talukId,
            @Param("agriStart") LocalDate agriStart,
            @Param("agriEnd") LocalDate agriEnd
    );
    @Query(value = """
    SELECT
        t.des_taluk_id,
        t.des_taluk_name_en,
        COUNT(DISTINCT wa.lbcode) AS taluk_lbcode_count
    FROM tbl_master_taluk_des t
    JOIN tbl_master_zone z
        ON z.des_taluk_id = t.des_taluk_id
    JOIN tbl_work_allocation wa
        ON wa.zone_id = z.zone_id
    WHERE t.dist_id = :districtId
      AND t.is_active = true
      AND z.is_active = true
      AND wa.agri_start = :agriStart
      AND wa.agri_end = :agriEnd
      AND wa.total_wet_cluster > 0
      AND wa.is_active = true
    GROUP BY t.des_taluk_id, t.des_taluk_name_en
    ORDER BY t.des_taluk_name_en
    """, nativeQuery = true)
    List<Object[]> getWetClusterLbcodesByDistrict(
            @Param("districtId") Long districtId,
            @Param("agriStart") LocalDate agriStart,
            @Param("agriEnd") LocalDate agriEnd
    );
    @Query(value = """
    SELECT
        d.dist_id AS district_id,
        d.dist_name_en AS district_name,
        COUNT(DISTINCT wa.lbcode) AS lbcode_count

    FROM tbl_master_district d

    JOIN tbl_master_taluk_des t
        ON t.dist_id = d.dist_id
        AND t.is_active = true

    JOIN tbl_master_zone z
        ON z.des_taluk_id = t.des_taluk_id
        AND z.is_active = true

    JOIN tbl_work_allocation wa
        ON wa.zone_id = z.zone_id
        AND wa.agri_start = :agriStart
        AND wa.agri_end = :agriEnd
        AND wa.total_wet_cluster > 0
        AND wa.is_active = true

    WHERE d.is_active = true

    GROUP BY d.dist_id, d.dist_name_en

    HAVING COUNT(DISTINCT wa.lbcode) > 0

    ORDER BY d.dist_name_en
    """, nativeQuery = true)
    List<Object[]> getDistrictWiseWetLbcodeCount(
            @Param("agriStart") LocalDate agriStart,
            @Param("agriEnd") LocalDate agriEnd
    );
}