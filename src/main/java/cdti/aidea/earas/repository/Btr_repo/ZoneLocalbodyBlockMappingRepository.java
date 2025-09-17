package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.ZoneLocalbodyBlockMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ZoneLocalbodyBlockMappingRepository extends JpaRepository<ZoneLocalbodyBlockMapping, Long> {

    Optional<ZoneLocalbodyBlockMapping> findByZone(Integer zone);
}
