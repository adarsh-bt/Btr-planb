package cdti.aidea.earas.repository.Btr_repo;
import cdti.aidea.earas.model.Btr_models.KeyplotsLimitLog;
import cdti.aidea.earas.model.Btr_models.TblSeasonMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface TblSeasonMasterRepository extends JpaRepository<TblSeasonMaster, Long>{
    List<TblSeasonMaster> findByIsActiveTrue();
}