package cdti.aidea.earas.repository.Btr_repo;


import cdti.aidea.earas.model.Btr_models.Masters.TblMasterVillage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;


@Repository
public interface TblMasterVillageRepository extends JpaRepository<TblMasterVillage, Integer> {
    List<TblMasterVillage> findAllById(Iterable<Integer> villageIds);
    List<TblMasterVillage> findByLsgCodeIn(Set<Integer> lsgCodes);
    Optional<TblMasterVillage> findByLsgCode(Integer lsgCode);
    Optional<TblMasterVillage> findFirstByLsgCode(Integer lsgCode);



}
