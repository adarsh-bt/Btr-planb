package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.ClusterLimitLog;
import feign.Param;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ClusterLimitLogRepository extends JpaRepository<ClusterLimitLog, Long> {
  boolean existsByAgriStartYearAndIsActive(LocalDate agriStartYear, boolean isActive);

  ClusterLimitLog findFirstByInActiveTrueOrderByAgriStartYearDesc();

  @Modifying
  @Query("update ClusterLimitLog c set c.inActive = false where c.inActive = true")
  int deactivateAll();

  Optional<ClusterLimitLog> findByInActiveTrue();

  @Query(
      value =
          "SELECT COUNT(*) > 0 FROM cluster_limit_log "
              + "WHERE EXTRACT(YEAR FROM agri_start_year) = :startYear "
              + "AND EXTRACT(YEAR FROM agri_end_year) = :endYear",
      nativeQuery = true)
  boolean existsByAgriStartAndEndYear(
      @Param("startYear") int startYear, @Param("endYear") int endYear);
}
