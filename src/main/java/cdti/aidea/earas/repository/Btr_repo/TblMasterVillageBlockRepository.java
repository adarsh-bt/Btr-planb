package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.TblMasterVillageBlock;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TblMasterVillageBlockRepository
    extends JpaRepository<TblMasterVillageBlock, Integer> {
  List<TblMasterVillageBlock> findByVillageId(Integer villageId);
  // Custom query methods if needed
  // Example:
  // List<VillageBlock> findByVillageId(Integer villageId);
  // ✅ Fetch blocks for multiple villages at once
  List<TblMasterVillageBlock> findByVillageIdIn(List<Integer> villageIds);


  @Query("""
           SELECT b.villageBlockId
           FROM TblMasterVillageBlock b
           WHERE b.villageId = :villageId
           """)
  List<Integer> findBlockIdsByVillageId(@Param("villageId") Integer villageId);

}
