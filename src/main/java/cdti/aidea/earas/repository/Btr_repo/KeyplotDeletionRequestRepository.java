package cdti.aidea.earas.repository.Btr_repo;
import cdti.aidea.earas.model.Btr_models.KeyplotDeletionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KeyplotDeletionRequestRepository
        extends JpaRepository<KeyplotDeletionRequest, UUID> {

    boolean existsByRequestNumber(String requestNumber);

    boolean existsByKeyplotIdAndStatusIn(UUID keyplotId, java.util.Collection<String> statuses);

    Optional<KeyplotDeletionRequest> findByRequestNumber(String requestNumber);

    Page<KeyplotDeletionRequest> findByAgriYear(String agriYear, Pageable pageable);

    // 2. District level (Join zone where distId matches)
    @Query("""
        SELECT r FROM KeyplotDeletionRequest r, TblMasterZone z 
        WHERE r.zoneId = z.zoneId 
          AND z.distId = :districtId 
          AND r.agriYear = :agriYear
    """)
    Page<KeyplotDeletionRequest> findByDistrictAndAgriYear(
            @Param("districtId") Integer districtId,
            @Param("agriYear") String agriYear,
            Pageable pageable
    );

    // 3. Taluk level (Join zone where desTalukId matches)
    @Query("""
        SELECT r FROM KeyplotDeletionRequest r, TblMasterZone z 
        WHERE r.zoneId = z.zoneId 
          AND z.desTalukMaster.desTalukId = :talukId 
          AND r.agriYear = :agriYear
    """)
    Page<KeyplotDeletionRequest> findByTalukAndAgriYear(
            @Param("talukId") Integer talukId,
            @Param("agriYear") String agriYear,
            Pageable pageable
    );
}