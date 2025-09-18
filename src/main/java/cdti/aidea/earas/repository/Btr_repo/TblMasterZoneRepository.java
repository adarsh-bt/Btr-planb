package cdti.aidea.earas.repository.Btr_repo;


import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface TblMasterZoneRepository extends JpaRepository<TblMasterZone, Integer> {
    List<TblMasterZone> findByDesTalukId(Integer desTalukId);
    List<TblMasterZone> findByDistId(Integer desDistId);


}

