package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.CropAssignmentTrail;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CropAssignmentTrailRepository extends JpaRepository<CropAssignmentTrail, Long> {

  boolean existsByCropIdAndCluster_CluMasterIdAndIsRejectedTrueAndRejectedBy(
      Long cropId, Long cluster_cluMasterId, UUID rejectedBy);
  // Basic finders if needed later
  List<CropAssignmentTrail> findByCropId(Long cropId);

  List<CropAssignmentTrail> findByCluster_CluMasterIdOrderByCreatedAtDesc(Long clusterId);

  // Count methods for statistics (optional)
  long countByCropId(Long cropId);

  long countByCropIdAndIsRejectedTrue(Long cropId);
}
