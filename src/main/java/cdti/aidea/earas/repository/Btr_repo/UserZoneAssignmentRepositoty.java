package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import cdti.aidea.earas.model.Btr_models.UserZoneAssignment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserZoneAssignmentRepositoty extends JpaRepository<UserZoneAssignment, Long> {
  @Query("SELECT u.tblMasterZone.zoneId FROM UserZoneAssignment u WHERE u.isActive = true")
  List<Long> findActiveAssignedZoneIds();

//  Optional<UserZoneAssignment> findByUserId(UUID userId);

  Optional<UserZoneAssignment> findByTblMasterZone_ZoneId(Integer zoneId);
  Optional<UserZoneAssignment> findByTblMasterZone_ZoneIdAndIsActiveTrue(Integer zoneId);


  Optional<UserZoneAssignment> findByUserIdAndTblMasterZone_ZoneId(UUID userId, Long zoneId);
  Optional<UserZoneAssignment> findByUserIdAndTblMasterZone_ZoneIdAndIsActiveTrue(
          UUID userId, Long zoneId
  );
  Optional<UserZoneAssignment> findByTblMasterZoneAndIsActiveTrue(TblMasterZone zone);

  List<UserZoneAssignment> findAllByUserIdAndIsActiveTrue(UUID userId);

}
