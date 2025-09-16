package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.TblMasterVillageBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TblMasterVillageBlockRepository extends JpaRepository<TblMasterVillageBlock, Integer> {
    List<TblMasterVillageBlock> findByVillageId(Integer villageId);
    // Custom query methods if needed
    // Example:
    // List<VillageBlock> findByVillageId(Integer villageId);
    // ✅ Fetch blocks for multiple villages at once
    List<TblMasterVillageBlock> findByVillageIdIn(List<Integer> villageIds);
}