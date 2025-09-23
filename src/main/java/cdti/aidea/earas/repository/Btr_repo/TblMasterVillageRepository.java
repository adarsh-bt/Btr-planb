package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.TblMasterVillage;
import feign.Param;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TblMasterVillageRepository extends JpaRepository<TblMasterVillage, Integer> {
  List<TblMasterVillage> findAllById(Iterable<Integer> villageIds);

  List<TblMasterVillage> findByLsgCodeIn(Set<Integer> lsgCodes);

  Optional<TblMasterVillage> findByLsgCode(Integer lsgCode);

  Optional<TblMasterVillage> findFirstByLsgCode(Integer lsgCode);

  // NEW METHOD: Villages by Zone and LocalBody
  @Query(
      value =
          """
        SELECT DISTINCT
            v.village_id,
            v.village_name_en,
            lb.localbody_id,
            lb.localbody_name_en,
            z.zone_id,
            z.zone_name_en
        FROM tbl_master_village v
        JOIN tbl_master_village_block vb ON v.village_id = vb.village_id
        JOIN tbl_master_block b ON vb.village_block_id = b.block_id
        JOIN tbl_master_localbody lb ON CAST(b.district AS SMALLINT) = lb.dist_id
        JOIN tbl_master_zone z ON lb.dist_id = CAST(z.dist_id AS SMALLINT)
        WHERE lb.localbody_id = :localBodyId
          AND z.zone_id = :zoneId
        """,
      nativeQuery = true)
  List<Object[]> findVillagesByZoneAndLocalBody(
      @Param("zoneId") Integer zoneId, @Param("localBodyId") Integer localBodyId);
}
