package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.MasterBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasterBlockRepository extends JpaRepository<MasterBlock, Integer> {
    List<MasterBlock> findByDistrictAndIsValidTrue(Integer district);
  // Add custom query methods if required
}
