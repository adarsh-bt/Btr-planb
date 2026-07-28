package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.DesTaluk;
import cdti.aidea.earas.model.Btr_models.Masters.DesTalukMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DesTalukRepository extends JpaRepository<DesTaluk, Integer> {
    List<DesTaluk> findByDistIdAndIsActiveTrue(int distId);
}
