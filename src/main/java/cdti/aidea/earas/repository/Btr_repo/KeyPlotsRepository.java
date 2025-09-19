package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.KeyPlots;

import cdti.aidea.earas.model.Btr_models.TblBtrData;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KeyPlotsRepository  extends JpaRepository<KeyPlots, UUID> {


    @Query("SELECT k FROM KeyPlots k " +
            "WHERE k.zone.zoneId = :zoneId " +
            "AND k.isRejected = false " +
            "AND k.selectedDate >= :cutoffDate")
    List<KeyPlots> findValidKeyPlots(@Param("zoneId") Integer zoneId, @Param("cutoffDate") LocalDate cutoffDate);


    @Query("SELECT kp FROM KeyPlots kp " +
            "WHERE kp.zone.zoneId = :zoneId " +
            "AND kp.isRejected = false " +
            "AND EXTRACT(YEAR FROM kp.agriStartYear) = :currentYear " +
            "AND kp.agriEndYear >= :today")
    List<KeyPlots> findValidKeyPlots(
            @Param("zoneId") Integer zoneId,
            @Param("currentYear") int currentYear,
            @Param("today") LocalDate today
    );





//    @Query("SELECT k.btrDataOld.id FROM KeyPlots k " +
//            "WHERE k.selectedDate >= :cutoffDate")
//    List<Long> findRecentlyUsedBtrIds(@Param("cutoffDate") LocalDate cutoffDate);
//    List<KeyPlots> findByUserZoneAssignment_TblMasterZone_ZoneId(Long zoneId);
//    Optional<KeyPlots> findByBtrData(TblBtrData btrData);
//
//    List<KeyPlots> findByBtrData_IdIn(List<Long> btrDataIds);


    List<KeyPlots> findByZone(TblMasterZone zoneId);

}
