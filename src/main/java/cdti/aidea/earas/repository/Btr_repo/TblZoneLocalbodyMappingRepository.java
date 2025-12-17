package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.TblZoneLocalbodyMapping;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TblZoneLocalbodyMappingRepository
    extends JpaRepository<TblZoneLocalbodyMapping, Integer> {
  List<TblZoneLocalbodyMapping> findAllByZoneAndIsValid(Integer zone, Boolean isValid);
}
