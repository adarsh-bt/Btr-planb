package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.ClusterApprovalLog;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClusterApprovalLogRepository
        extends JpaRepository<ClusterApprovalLog, Long> {

    // Get all logs by zone
    List<ClusterApprovalLog> findByZone(TblMasterZone zone);

    // OR: Get all logs by zone id directly
//    List<ClusterApprovalLog> findByZone_Id(Long zoneId);

    List<ClusterApprovalLog> findByZone_DesTalukId(Integer talukId);

    List<ClusterApprovalLog> findByZone_DistId(Integer districtId);

    List<ClusterApprovalLog> findAll();


    Page<ClusterApprovalLog> findByZone_DesTalukId(
            Integer talukId,
            Pageable pageable
    );

    Page<ClusterApprovalLog> findByZone_DistId(
            Integer districtId,
            Pageable pageable
    );

    Page<ClusterApprovalLog> findAll(Pageable pageable);
}
