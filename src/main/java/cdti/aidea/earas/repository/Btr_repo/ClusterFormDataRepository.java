package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.ClusterFormData;
import cdti.aidea.earas.model.Btr_models.ClusterMaster;
import cdti.aidea.earas.model.Btr_models.TblBtrData;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import cdti.aidea.earas.model.Btr_models.TblBtrData;
import cdti.aidea.earas.repository.Btr_repo.projection.ClusterAreaProjection;
import org.springframework.data.jpa.repository.JpaRepository;
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

}
