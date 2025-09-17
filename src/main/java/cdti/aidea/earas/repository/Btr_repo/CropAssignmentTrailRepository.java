package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.CropAssignmentTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CropAssignmentTrailRepository extends JpaRepository<CropAssignmentTrail, Integer> {
    boolean existsByCropIdAndCluster_CluMasterIdAndIsRejectedTrueAndRejectedBy(Long cropId, Long cluster_cluMasterId, UUID rejectedBy);

}
