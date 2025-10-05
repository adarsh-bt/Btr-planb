package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.KeyplotsLimitLog;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeyplotsLimitLogRepository extends JpaRepository<KeyplotsLimitLog, Long> {
  boolean existsByAgriStartYearAndIsActive(LocalDate agriStartYear, boolean isActive);
}
