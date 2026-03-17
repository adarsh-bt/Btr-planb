package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.contract.Response.BtrClusterUsageResponse;
import cdti.aidea.earas.model.Btr_models.ClusterFormData;
import cdti.aidea.earas.model.Btr_models.ClusterMaster;
import cdti.aidea.earas.model.Btr_models.TblBtrData;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import cdti.aidea.earas.model.Btr_models.TblBtrData;
import cdti.aidea.earas.repository.Btr_repo.projection.ClusterAreaProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClusterFormDataRepository extends JpaRepository<ClusterFormData, Long> {
  List<ClusterFormData> findByClusterMaster(ClusterMaster clusterMaster);

  List<ClusterFormData> findByPlotIdIn(List<Long> plotIds);

  List<ClusterFormData> findByPlotAndCreatedAtBetween(
      TblBtrData plot, LocalDateTime localDateTime, LocalDateTime localDateTime1);

  Optional<ClusterFormData> findByClusterMasterAndPlotAndPlotLabel(ClusterMaster clusterMaster, TblBtrData plot, String label);

  @Query("""
    SELECT c.clusterMaster.cluMasterId AS clusterId,
           COALESCE(SUM(c.enumeratedArea), 0)
           AS totalArea
    FROM ClusterFormData c
    WHERE c.clusterMaster.cluMasterId IN :clusterIds
    GROUP BY c.clusterMaster.cluMasterId
""")
  List<ClusterAreaProjection> findTotalAreaByClusterIds(
          @Param("clusterIds") List<Long> clusterIds
  );

  @Query("""
    SELECT
        btr.totCent AS totCent,
        cm.clusterNumber AS clusterNumber,
        cfd.plotLabel AS plotLabel,
        COALESCE(SUM(cfd.enumeratedArea), 0) AS totalEnumeratedArea
    FROM ClusterFormData cfd
    JOIN cfd.clusterMaster cm
    JOIN cfd.plot btr
    WHERE btr.id = :btrId
    GROUP BY
        btr.totCent,
        cm.clusterNumber,
        cfd.plotLabel
""")
  List<BtrClusterUsageResponse> findClusterUsageByBtrId(Long btrId);


  @Query("""
        SELECT COALESCE(SUM(cfd.enumeratedArea), 0)
        FROM ClusterFormData cfd
        WHERE cfd.plot.id = :btrId
    """)
  Double getTotalEnumeratedAreaByBtrId(@Param("btrId") Long btrId);

  @Modifying
  @Query("DELETE FROM ClusterFormData cfd WHERE cfd.plot.id = :btrId")
  void deleteByBtrId(@Param("btrId") Long btrId);

  @Query("""
    SELECT COALESCE(SUM(c.enumeratedArea), 0)
    FROM ClusterFormData c
    WHERE c.plot = :plot
      AND c.status = true
      AND c.createdAt BETWEEN :start AND :end
""")
  Double sumEnumeratedAreaForPlot(
          @Param("plot") TblBtrData plot,
          @Param("start") LocalDateTime start,
          @Param("end") LocalDateTime end
  );

  Optional<ClusterFormData>
  findByPlotLabelAndClusterMaster_CluMasterId(
          String plotLabel,
          Long cluMasterId
  );

}
