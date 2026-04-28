package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.contract.RequestsDTOs.ZoneUserAssignDto;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import java.util.List;
import java.util.Optional;

import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
//import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TblMasterZoneRepository extends JpaRepository<TblMasterZone, Integer> {
  List<TblMasterZone> findByDesTalukId(Integer desTalukId);

  List<TblMasterZone> findByDistId(Integer desDistId);
    Optional<TblMasterZone> findByZoneId(Integer zoneId);

    //List<TblMasterZone> findByDistId(Integer distId);
   // List<TblMasterZone> findByDesTalukId(Integer talukId);


    @Query("SELECT z FROM TblMasterZone z WHERE z.zoneId = :zoneId AND z.isActive = true")
  Optional<TblMasterZone> findActiveZoneById(@Param("zoneId") Integer zoneId);

  Page<TblMasterZone> findAllByIsActiveTrue(Pageable pageable);
  Optional<TblMasterZone> findByZoneIdAndIsActiveTrue(Integer zoneId);

  @Query("""
    SELECT z FROM TblMasterZone z
    WHERE (
        LOWER(z.zoneNameEn) LIKE LOWER(CONCAT('%', :search, '%')) OR
        LOWER(z.zoneNameMal) LIKE LOWER(CONCAT('%', :search, '%')) OR
        CAST(z.zoneCode AS string) LIKE CONCAT('%', :search, '%')
    )
""")
  Page<TblMasterZone> searchZones(@Param("search") String search, Pageable pageable);


   @Query("""
       SELECT new cdti.aidea.earas.contract.RequestsDTOs.ZoneUserAssignDto(
            z.zoneId,
            z.zoneNameEn,
            z.desDistId,
            z.desTalukId,
            CASE
                WHEN uza.id IS NOT NULL THEN true
                ELSE false
            END,
            uza.userId
        )
        FROM TblMasterZone z
        LEFT JOIN UserZoneAssignment uza
               ON uza.tblMasterZone.zoneId = z.zoneId
              AND uza.isActive = true
        WHERE z.isActive = true
        ORDER BY z.zoneId
    """)
    List<ZoneUserAssignDto> findActiveZonesWithAssignment();

    Page<TblMasterZone> findByZoneNameEnContainingIgnoreCaseOrZoneCodeContainingIgnoreCase(
            String name, String code, Pageable pageable);
}

