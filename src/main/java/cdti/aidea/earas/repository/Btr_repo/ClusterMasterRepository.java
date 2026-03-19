package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.ClusterFormData;
import cdti.aidea.earas.model.Btr_models.ClusterMaster;
import cdti.aidea.earas.model.Btr_models.KeyPlots;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClusterMasterRepository extends JpaRepository<ClusterMaster, Long> {
  @Query("SELECT c FROM ClusterMaster c WHERE c.keyPlot.id = :keyPlotId")
  Optional<ClusterMaster> findByKeyPlotId(@Param("keyPlotId") UUID keyPlotId);

  @Query("SELECT c FROM ClusterMaster c WHERE c.zone.zoneId = :userId AND c.isReject = false")
  List<ClusterMaster> findAllByUserId(@Param("userId") UUID userId);

  @Query("SELECT c FROM ClusterMaster c WHERE c.zone.zoneId = :zoneId AND c.isReject = false")
  List<ClusterMaster> findAllByZoneIdAndIsRejectFalse(@Param("zoneId") Integer zoneId);

  Optional<ClusterMaster> findTopByKeyPlotOrderByCreatedAtDesc(KeyPlots keyPlot);

  Optional<ClusterMaster> findByKeyPlot(KeyPlots plot);

//  @Query(
  ////      "SELECT cm FROM ClusterMaster cm "
  ////          + "JOIN cm.keyPlot kp "
  ////          + "JOIN kp.zone.zoneId uza "
  ////          + "WHERE uza = :zoneId "
  ////          + "AND (:landType = 'Wet / Dry' OR kp.landType = :landType) "
  ////          + "AND (:landType = 'Wet / Dry' OR kp.landType IN ('Wet', 'Dry')) "
  ////          + "AND cm.clusterNumber > :currentClusterNumber "
  ////          + "AND cm.is_active = true "
  ////          + "AND cm.isReject = false "
  ////          + "ORDER BY cm.clusterNumber ASC")
  ////  List<ClusterMaster> findNextClusterFlexibleLandType(
  ////      @Param("zoneId") int zoneId,
  ////      @Param("landType") String landType,
  ////      @Param("currentClusterNumber") int currentClusterNumber);


  @Query(
          "SELECT cm FROM ClusterMaster cm "
                  + "JOIN cm.keyPlot kp "
                  + "JOIN kp.zone z "
                  + "WHERE z.zoneId = :zoneId "
                  + "AND (:landType = 'Wet / Dry' OR LOWER(kp.landType) = LOWER(:landType)) "
                  + "AND cm.clusterNumber > :currentClusterNumber "
                  + "AND cm.is_active = true "
                  + "AND cm.isReject = false "
                  + "ORDER BY cm.clusterNumber ASC"
  )
  List<ClusterMaster> findNextClusterFlexibleLandType(
          @Param("zoneId") int zoneId,
          @Param("landType") String landType,
          @Param("currentClusterNumber") int currentClusterNumber);


  List<ClusterMaster> findByKeyPlotIn(List<KeyPlots> keyPlots);

  @Query(
          "SELECT MAX(cm.clusterNumber) FROM ClusterMaster cm "
                  + "JOIN cm.keyPlot kp "
                  + "JOIN kp.btrData bd "
                  + "WHERE bd.lbcode = :lbcode "
                  + "AND kp.landType = :landType "
                  + "AND kp.agriStartYear BETWEEN :startDate AND :endDate")
  Optional<Integer> findMaxClusterNumberByLbcodeAndLandTypeAndDateRange(
          @Param("lbcode") String lbcode,
          @Param("landType") String landType,
          @Param("startDate") LocalDate startDate,
          @Param("endDate") LocalDate endDate);

  @Query("SELECT MAX(c.clusterNumber) FROM ClusterMaster c " +
          "WHERE c.zone.zoneId = :zoneId " +
          "AND c.createdAt BETWEEN :start AND :end")
  Optional<Integer> findMaxClusterNumberByZoneAndDateRange(
          @Param("zoneId") Integer zoneId,
          @Param("start") LocalDateTime start,
          @Param("end") LocalDateTime end);

  Optional<ClusterMaster> findByKeyPlot_Id(UUID kpId);
//  Optional<ClusterFormData> findByClusterMaster(ClusterMaster clusterMaster);

  @Query("SELECT cm FROM ClusterMaster cm " +
          "WHERE cm.zone.zoneId = :zoneId " +
          "AND cm.clusterNumber = :clusterNumber " +
          "AND cm.keyPlot.agriStartYear = :agriStart " +
          "AND cm.keyPlot.agriEndYear = :agriEnd")
  Optional<ClusterMaster> findByZoneAndClusterNumberAndAgriYear(
          @Param("zoneId") Long zoneId,
          @Param("clusterNumber") Integer clusterNumber,
          @Param("agriStart") LocalDate agriStart,
          @Param("agriEnd") LocalDate agriEnd);

  @Query("SELECT MAX(cm.clusterNumber) FROM ClusterMaster cm " +
          "WHERE cm.zone.zoneId = :zoneId " +
          "AND cm.keyPlot.agriStartYear = :agriStart " +
          "AND cm.keyPlot.agriEndYear = :agriEnd")
  Optional<Integer> findMaxClusterNumberByZoneAndAgriYear(
          @Param("zoneId") Long zoneId,
          @Param("agriStart") LocalDate agriStart,
          @Param("agriEnd") LocalDate agriEnd);
}
