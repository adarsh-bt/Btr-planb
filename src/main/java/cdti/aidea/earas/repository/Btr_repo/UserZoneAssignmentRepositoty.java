package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.UserZoneAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserZoneAssignmentRepositoty extends JpaRepository<UserZoneAssignment, Long> {
    @Query("SELECT u.tblMasterZone.zoneId FROM UserZoneAssignment u")
    List<Long> findAssignedZoneIds();

    Optional<UserZoneAssignment> findByUserId(UUID userId);

    Optional<UserZoneAssignment> findByTblMasterZone_ZoneId(Integer zoneId);

    Optional<UserZoneAssignment> findByUserIdAndTblMasterZone_ZoneId(UUID userId, Long zoneId);

    List<UserZoneAssignment> findAllByUserId(UUID userId);



}
