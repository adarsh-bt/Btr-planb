package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.LandTypeClassification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LandTypeClassificationRepository
    extends JpaRepository<LandTypeClassification, Long> {
  Optional<LandTypeClassification> findByLandType(String landType);
}
