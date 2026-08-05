package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.DistrictMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictMasterRepository extends JpaRepository<DistrictMaster, Long> {
    List<DistrictMaster> findByActiveTrue();
    //List<DistrictMaster> findByIsActiveTrue();
}
