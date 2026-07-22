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
}