package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.MasterBlock;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterBlockRepository extends JpaRepository<MasterBlock, Integer> {
  // Add custom query methods if required
  Page<MasterBlock> findByBlockNameContainingIgnoreCaseOrBlockCodeContainingIgnoreCase(
          String blockName, String blockCode, Pageable pageable);
}
