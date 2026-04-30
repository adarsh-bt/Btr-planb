package cdti.aidea.earas.repository.Btr_repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import cdti.aidea.earas.model.Btr_models.TblZoneSeasonSchedule;

import java.util.List;

@Repository
public interface TblZoneSeasonScheduleRepository extends JpaRepository<TblZoneSeasonSchedule, Long>{
    // ✅ Add this new method — no changes to existing code
    List<TblZoneSeasonSchedule> findByZoneZoneIdAndFrameFrameId(Integer zoneId, Long frameId);


    // ✅ Pagination support (built-in JPA)
    Page<TblZoneSeasonSchedule> findAll(Pageable pageable);

    // Deactivate existing active schedules for same zone + season + frame
    List<TblZoneSeasonSchedule>
    findByZoneZoneIdAndSeasonIdAndFrameFrameIdAndIsActiveTrue(
            Integer zoneId,
            Long seasonId,
            Long frameId
    );

    List<TblZoneSeasonSchedule> findByZoneZoneIdAndSeasonIdAndIsActiveTrue(
            Integer zoneId,
            Long seasonId
    );

    boolean existsByZoneZoneIdAndSeasonIdAndIsActiveTrue(
            Integer zoneId,
            Long seasonId
    );

}