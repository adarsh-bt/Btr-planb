package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.ClusterEditAllowed;
import cdti.aidea.earas.model.Btr_models.EditRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ClusterEditAllowedRepository extends JpaRepository<ClusterEditAllowed, Long> {

//    List<ClusterEditAllowed> findByClusterMasterCluMasterId(Long clusterId);
//
//    List<ClusterEditAllowed> findByStatus(EditRequestStatus status);
//
//    List<ClusterEditAllowed> findByZoneZoneId(Integer zoneId);

}