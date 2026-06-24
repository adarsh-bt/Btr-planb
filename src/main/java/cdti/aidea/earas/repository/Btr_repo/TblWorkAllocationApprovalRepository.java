package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.TblWorkAllocationApproval;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface TblWorkAllocationApprovalRepository extends JpaRepository<TblWorkAllocationApproval, Long> {
//    Optional<TblWorkAllocationApproval> findByZoneId(Integer zoneId);

//    @Query("SELECT DISTINCT a FROM TblWorkAllocationApproval a " +
//            "JOIN TblWorkAllocation wa ON wa.approvalId = a.id " +
//            "WHERE wa.zone.desTalukId = :talukId AND a.status != 'DRAFT'")
//    Page<TblWorkAllocationApproval> findByTalukId(@Param("talukId") Integer talukId, Pageable pageable);

    // 2. Filter Approvals by District ID via underlying allocations
//    @Query("SELECT DISTINCT a FROM TblWorkAllocationApproval a " +
//            "JOIN TblWorkAllocation wa ON wa.approvalId = a.id " +
//            "WHERE wa.zone.distId = :districtId AND a.status != 'DRAFT'")
//    Page<TblWorkAllocationApproval> findByDistrictId(@Param("districtId") Integer districtId, Pageable pageable);

    // 3. Get all non-draft approvals for Directorate overview
//    @Query("SELECT DISTINCT a FROM TblWorkAllocationApproval a " +
//            "JOIN TblWorkAllocation wa ON wa.approvalId = a.id " +
//            "WHERE a.status != 'DRAFT'")
//    Page<TblWorkAllocationApproval> findAllSubmitted(Pageable pageable);

            @Query("""
        SELECT t
        FROM TblWorkAllocationApproval t
        JOIN TblWorkAllocation w
        ON w.approvalId = t.id
        WHERE w.zone.desTalukId = :talukId
        AND t.agriStart = :agriStart
        AND t.agriEnd = :agriEnd
        """)
            Page<TblWorkAllocationApproval> findByTalukIdAndAgriYear(
                    Integer talukId,
                    LocalDate agriStart,
                    LocalDate agriEnd,
                    Pageable pageable
            );

            @Query("""
        SELECT t
        FROM TblWorkAllocationApproval t
        JOIN TblWorkAllocation w
        ON w.approvalId = t.id
        WHERE w.zone.distId = :districtId
        AND t.agriStart = :agriStart
        AND t.agriEnd = :agriEnd
        """)
            Page<TblWorkAllocationApproval> findByDistrictIdAndAgriYear(
                    Integer districtId,
                    LocalDate agriStart,
                    LocalDate agriEnd,
                    Pageable pageable
            );

            @Query("""
        SELECT t
        FROM TblWorkAllocationApproval t
        WHERE t.status = 'SUBMITTED'
        AND t.agriStart = :agriStart
        AND t.agriEnd = :agriEnd
        """)
            Page<TblWorkAllocationApproval> findAllSubmittedByAgriYear(
                    LocalDate agriStart,
                    LocalDate agriEnd,
                    Pageable pageable
            );

    Optional<TblWorkAllocationApproval>
    findByZoneIdAndAgriStartAndAgriEnd(
            Integer zoneId,
            LocalDate agriStart,
            LocalDate agriEnd
    );
}
