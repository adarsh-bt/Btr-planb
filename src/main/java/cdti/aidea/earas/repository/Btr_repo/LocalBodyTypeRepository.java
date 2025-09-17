package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.LocalBodyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalBodyTypeRepository extends JpaRepository<LocalBodyType, Long> {

    List<LocalBodyType> findByIdIn(List<Long> ids);

}