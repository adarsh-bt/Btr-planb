package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.TblZoneVillageBlockMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TblZoneVillageBlockMappingRepository
    extends JpaRepository<TblZoneVillageBlockMapping, Long> {
    boolean existsByZoneAndVillageBlockIdAndIsValidTrue(
            Integer zone,
            Integer villageBlockId
    );
    List<TblZoneVillageBlockMapping> findByZoneAndVillageIdAndIsValidTrue(
            Integer zone, Integer villageId);

    @Modifying
    @Query("""
    UPDATE TblZoneVillageBlockMapping b
    SET b.isValid = false,
        b.updatedBy = :userId,
        b.updatedAt = CURRENT_TIMESTAMP
    WHERE b.zone = :zone
      AND b.villageId = :villageId
      AND b.isValid = true
""")
    void softDeleteBlocksByVillage(Integer zone, Integer villageId, UUID userId);
}
