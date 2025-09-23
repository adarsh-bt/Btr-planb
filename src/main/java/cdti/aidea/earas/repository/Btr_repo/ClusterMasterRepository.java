package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.ClusterMaster;
import cdti.aidea.earas.model.Btr_models.KeyPlots;
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

  @Query(
      "SELECT cm FROM ClusterMaster cm "
          + "JOIN cm.keyPlot kp "
          + "JOIN kp.zone.zoneId uza "
          + "WHERE uza = :zoneId "
          + "AND (:landType = 'Wet / Dry' OR kp.landType = :landType) "
          + "AND (:landType = 'Wet / Dry' OR kp.landType IN ('Wet', 'Dry')) "
          + "AND cm.clusterNumber > :currentClusterNumber "
          + "AND cm.is_active = true "
          + "AND cm.isReject = false "
          + "ORDER BY cm.clusterNumber ASC")
  List<ClusterMaster> findNextClusterFlexibleLandType(
      @Param("zoneId") int zoneId,
      @Param("landType") String landType,
      @Param("currentClusterNumber") int currentClusterNumber);

  List<ClusterMaster> findByKeyPlotIn(List<KeyPlots> keyPlots);
}
