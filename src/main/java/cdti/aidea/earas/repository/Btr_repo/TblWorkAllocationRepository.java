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
}