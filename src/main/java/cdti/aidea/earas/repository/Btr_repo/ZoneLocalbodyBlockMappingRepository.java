package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.ZoneLocalbodyBlockMapping;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZoneLocalbodyBlockMappingRepository
    extends JpaRepository<ZoneLocalbodyBlockMapping, Long> {

  Optional<ZoneLocalbodyBlockMapping> findByZoneAndIsValid(Integer zone, Boolean isValid);

  List<ZoneLocalbodyBlockMapping> findByZone(Integer zone);


        Optional<ZoneLocalbodyBlockMapping>
        findByZoneAndIsValidTrue(Integer zone);


}
