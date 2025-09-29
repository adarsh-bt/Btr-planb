package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.ClusterFormData;
import cdti.aidea.earas.model.Btr_models.ClusterMaster;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClusterFormDataRepository extends JpaRepository<ClusterFormData, Long> {
  List<ClusterFormData> findByClusterMaster(ClusterMaster clusterMaster);

  List<ClusterFormData> findByPlotIdIn(List<Long> plotIds);
}
