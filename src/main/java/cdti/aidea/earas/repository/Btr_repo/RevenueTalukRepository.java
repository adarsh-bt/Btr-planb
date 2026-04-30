package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.RevTaluk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RevenueTalukRepository extends JpaRepository<RevTaluk, Long> {
    List<RevTaluk> findByDistIdAndIsActiveTrue(Integer distId);

    @Query("""
       SELECT t
       FROM RevTaluk t
       WHERE t.revTalukId NOT IN (
           SELECT z.revenueTaluk
           FROM ZoneRevenueTalukMapping z
           WHERE z.zone = :zoneId
           AND z.isValid = true
       )
       """)
    List<RevTaluk> findUnmappedTaluks(@Param("zoneId") Integer zoneId);

}
