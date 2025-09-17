package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.ZoneRevenueTalukMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ZoneRevenueTalukMappingRepository extends JpaRepository<ZoneRevenueTalukMapping, Long> {
//    List<ZoneRevenueTalukMapping> findByZone(Integer zone);
    // Custom queries can be added here if needed


    Optional<ZoneRevenueTalukMapping> findByZone(Integer zone);
}