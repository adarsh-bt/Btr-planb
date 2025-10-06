package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.TblNonBtr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TblNonBtrRepository extends JpaRepository<TblNonBtr, Long> {
    List<TblNonBtr> findByIsActiveTrue();
}
