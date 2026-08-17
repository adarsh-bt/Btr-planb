package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.DistrictMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistrictMasterRepository extends JpaRepository<DistrictMaster, Long> {
    Optional<DistrictMaster> findByDistNameEnIgnoreCase(String distNameEn);
    List<DistrictMaster> findByIsActiveTrue();
}
