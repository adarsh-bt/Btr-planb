package cdti.aidea.earas.repository.Btr_repo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import cdti.aidea.earas.model.Btr_models.TblZoneSeasonSchedule;

@Repository
public interface TblZoneSeasonScheduleRepository extends JpaRepository<TblZoneSeasonSchedule, Long>{
}
