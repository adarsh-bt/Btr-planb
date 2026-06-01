package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.contract.Projection.ClusterSummaryFastProjection;
import cdti.aidea.earas.contract.Projection.ClusterSummaryProjection;
import cdti.aidea.earas.contract.Response.KeyPlotDetailsListResponse;
//import cdti.aidea.earas.model.Btr_models.ClusterFormData;
import cdti.aidea.earas.model.Btr_models.ClusterMaster;
import cdti.aidea.earas.model.Btr_models.KeyPlots;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import cdti.aidea.earas.repository.Btr_repo.projection.ClusterAreaProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClusterMasterRepository extends JpaRepository<ClusterMaster, Long> {
  @Query("SELECT c FROM ClusterMaster c WHERE c.keyPlot.id = :keyPlotId")
  Optional<ClusterMaster> findByKeyPlotId(@Param("keyPlotId") UUID keyPlotId);

  @Query("SELECT c FROM ClusterMaster c WHERE c.zone.zoneId = :userId AND c.isReject = false")
  List<ClusterMaster> findAllByUserId(@Param("userId") UUID userId);

  @Query("SELECT c FROM ClusterMaster c WHERE c.zone.zoneId = :zoneId AND c.isReject = false")
  List<ClusterMaster> findAllByZoneIdAndIsRejectFalse(@Param("zoneId") Integer zoneId);

  Optional<ClusterMaster> findTopByKeyPlotOrderByCreatedAtDesc(KeyPlots keyPlot);

  Optional<ClusterMaster> findByKeyPlot(KeyPlots plot);

//  @Query("""
//    SELECT c FROM ClusterMaster c
//    WHERE c.zoneId = :zoneId
//    AND c.landType IN :landTypes
//    AND c.clusterNumber > :currentClusterNumber
//    ORDER BY c.clusterNumber ASC
//""")
//  List<ClusterMaster> findNextClusterFlexibleLandType(
//          @Param("zoneId") Integer zoneId,
//          @Param("landTypes") List<String> landTypes,
//          @Param("currentClusterNumber") Integer currentClusterNumber
//  );

//  @Query(
  ////      "SELECT cm FROM ClusterMaster cm "
  ////          + "JOIN cm.keyPlot kp "
  ////          + "JOIN kp.zone.zoneId uza "
  ////          + "WHERE uza = :zoneId "
  ////          + "AND (:landType = 'Wet / Dry' OR kp.landType = :landType) "
  ////          + "AND (:landType = 'Wet / Dry' OR kp.landType IN ('Wet', 'Dry')) "
  ////          + "AND cm.clusterNumber > :currentClusterNumber "
  ////          + "AND cm.is_active = true "
  ////          + "AND cm.isReject = false "
  ////          + "ORDER BY cm.clusterNumber ASC")
  ////  List<ClusterMaster> findNextClusterFlexibleLandType(
  ////      @Param("zoneId") int zoneId,
  ////      @Param("landType") String landType,
  ////      @Param("currentClusterNumber") int currentClusterNumber);


  @Query(
          "SELECT cm FROM ClusterMaster cm "
                  + "JOIN cm.keyPlot kp "
                  + "JOIN kp.zone z "
                  + "WHERE z.zoneId = :zoneId "
                  + "AND (:landType = 'Wet / Dry' OR LOWER(kp.landType) = LOWER(:landType)) "
                  + "AND cm.clusterNumber > :currentClusterNumber "
                  + "AND cm.is_active = true "
                  + "AND cm.isReject = false "
                  + "ORDER BY cm.clusterNumber ASC"
  )
  List<ClusterMaster> findNextClusterFlexibleLandType(
          @Param("zoneId") int zoneId,
          @Param("landType") String landType,
          @Param("currentClusterNumber") int currentClusterNumber);


  List<ClusterMaster> findByKeyPlotIn(List<KeyPlots> keyPlots);

  @Query(
          "SELECT MAX(cm.clusterNumber) FROM ClusterMaster cm "
                  + "JOIN cm.keyPlot kp "
                  + "JOIN kp.btrData bd "
                  + "WHERE bd.lbcode = :lbcode "
                  + "AND kp.landType = :landType "
                  + "AND kp.agriStartYear BETWEEN :startDate AND :endDate")
  Optional<Integer> findMaxClusterNumberByLbcodeAndLandTypeAndDateRange(
          @Param("lbcode") String lbcode,
          @Param("landType") String landType,
          @Param("startDate") LocalDate startDate,
          @Param("endDate") LocalDate endDate);

  @Query("SELECT MAX(c.clusterNumber) FROM ClusterMaster c " +
          "WHERE c.zone.zoneId = :zoneId " +
          "AND c.createdAt BETWEEN :start AND :end")
  Optional<Integer> findMaxClusterNumberByZoneAndDateRange(
          @Param("zoneId") Integer zoneId,
          @Param("start") LocalDateTime start,
          @Param("end") LocalDateTime end);
    //after changing to agree year
//@Query("""
//SELECT MAX(c.clusterNumber)
//FROM ClusterMaster c
//WHERE c.zone.zoneId = :zoneId
//AND c.keyPlot.agriStartYear = :agriStart
//AND c.keyPlot.agriEndYear = :agriEnd
//""")
//Optional<Integer> findMaxClusterNumberByZoneAndDateRange(
//        @Param("zoneId") Integer zoneId,
//        @Param("agriStart") LocalDate agriStart,
//        @Param("agriEnd") LocalDate agriEnd
//);

  Optional<ClusterMaster> findByKeyPlot_Id(UUID kpId);
//  Optional<ClusterFormData> findByClusterMaster(ClusterMaster clusterMaster);

  @Query("SELECT cm FROM ClusterMaster cm " +
          "WHERE cm.zone.zoneId = :zoneId " +
          "AND cm.clusterNumber = :clusterNumber " +
          "AND cm.keyPlot.agriStartYear = :agriStart " +
          "AND cm.keyPlot.agriEndYear = :agriEnd")
  Optional<ClusterMaster> findByZoneAndClusterNumberAndAgriYear(
          @Param("zoneId") Long zoneId,
          @Param("clusterNumber") Integer clusterNumber,
          @Param("agriStart") LocalDate agriStart,
          @Param("agriEnd") LocalDate agriEnd);

  @Query("SELECT MAX(cm.clusterNumber) FROM ClusterMaster cm " +
          "WHERE cm.zone.zoneId = :zoneId " +
          "AND cm.keyPlot.agriStartYear = :agriStart " +
          "AND cm.keyPlot.agriEndYear = :agriEnd")
  Optional<Integer> findMaxClusterNumberByZoneAndAgriYear(
          @Param("zoneId") Long zoneId,
          @Param("agriStart") LocalDate agriStart,
          @Param("agriEnd") LocalDate agriEnd);

  @Query("SELECT c FROM ClusterMaster c WHERE c.zone.zoneId = :zoneId AND c.clusterNumber = :clusterNumber")
  Optional<ClusterMaster> findByZoneAndClusterNumber(
          @Param("zoneId") Integer zoneId,
          @Param("clusterNumber") Integer clusterNumber
  );

  @Query("SELECT COUNT(c) > 0 FROM ClusterMaster c WHERE c.keyPlot.id = :keyPlotId AND c.status <> 'Not Started'")
  boolean existsByKeyPlotIdAndStatusNotNotStarted(@Param("keyPlotId") UUID keyPlotId);

  @Query("""
    SELECT cm 
    FROM ClusterMaster cm
    JOIN FETCH cm.keyPlot kp
    JOIN FETCH kp.btrData
    WHERE cm.zone = :zone
    ORDER BY cm.clusterNumber ASC
""")
  List<ClusterMaster> findAllByZoneOrderByClusterNumber(@Param("zone") TblMasterZone zone);

//before including start and end year
//  @Query("""
//SELECT new cdti.aidea.earas.contract.Response.KeyPlotDetailsListResponse(
//    kp.id,
//    b.dcode,
//    b.tcode,
//    cm.cluMasterId,
//    kp.zone.zoneId,
//    cm.clusterNumber,
//
//    b.id,
//    bt.bTypeName,
//
//    COALESCE(v.villageNameEn, 'Unknown'),
//    v.villageId,
//    b.bcode,
//    COALESCE(lb.localbodyNameEn, b.lbcode),
//    b.lbcode,
//
//    cm.status,
//    cm.is_editable,
//
//    CONCAT(b.resvno, '/', b.resbdno),
//    b.ownername,
//    b.address,
//    b.wardnumber,
//    b.houseno,
//    b.tpno,
//    b.tbsubdivisionno,
//    b.oldsvno,
//    b.oldsubno,
//
//    b.totCent,
//    COALESCE(cfd.enumeratedArea, 0.0),
//
//    kp.landType
//)
//FROM ClusterMaster cm
//JOIN cm.keyPlot kp
//JOIN kp.btrData b
//JOIN b.btrtype bt
//LEFT JOIN TblMasterVillage v ON v.lsgCode = b.lsgcode
//LEFT JOIN TblLocalBody lb ON lb.codeApi = b.lbcode
//LEFT JOIN ClusterFormData cfd
//    ON cfd.plot = b
//    AND cfd.plotLabel = 'K'
//    AND cfd.clusterMaster.cluMasterId = cm.cluMasterId
//WHERE cm.zone.zoneId = :zoneId
//ORDER BY cm.clusterNumber
//""")
//  Page<KeyPlotDetailsListResponse> findAllKeyPlotDetails(
//          @Param("zoneId") Integer zoneId,
//          Pageable pageable
//  );
//  Page<KeyPlotDetailsListResponse> findAllKeyPlotDetailsByYear( //after including agri start an end year
//          @Param("zoneId") Integer zoneId,
//          @Param("startYear") Integer startYear,
//          @Param("endYear") Integer endYear,
//          Pageable pageable
//  );
//
@Query("""
SELECT new cdti.aidea.earas.contract.Response.KeyPlotDetailsListResponse(
    kp.id,
    b.dcode,
    b.tcode,
    cm.cluMasterId,
    kp.zone.zoneId,
    cm.clusterNumber,

    b.id,
    bt.bTypeName,

    COALESCE(v.villageNameEn, 'Unknown'),
    v.villageId,
    b.bcode,
    COALESCE(lb.localbodyNameEn, b.lbcode),
    b.lbcode,

    cm.status,
    cm.is_editable,

    CONCAT(b.resvno, '/', b.resbdno),
    b.ownername,
    b.address,
    b.wardnumber,
    b.houseno,
    b.tpno,
    b.tbsubdivisionno,
    b.oldsvno,
    b.oldsubno,

    b.totCent,
    COALESCE(cfd.enumeratedArea, 0.0),

    kp.landType
)
FROM ClusterMaster cm
JOIN cm.keyPlot kp
JOIN kp.btrData b
JOIN b.btrtype bt
LEFT JOIN TblMasterVillage v ON v.lsgCode = b.lsgcode
LEFT JOIN TblLocalBody lb ON lb.codeApi = b.lbcode
LEFT JOIN ClusterFormData cfd
    ON cfd.plot = b
    AND cfd.plotLabel = 'K'
    AND cfd.clusterMaster.cluMasterId = cm.cluMasterId

WHERE cm.zone.zoneId = :zoneId
AND YEAR(kp.agriStartYear) = :startYear
AND YEAR(kp.agriEndYear) = :endYear

ORDER BY cm.clusterNumber
""")
Page<KeyPlotDetailsListResponse> findAllKeyPlotDetailsByYear(
        @Param("zoneId") Integer zoneId,
        @Param("startYear") Integer startYear,
        @Param("endYear") Integer endYear,
        Pageable pageable
);


  @Query("""
SELECT cm FROM ClusterMaster cm
JOIN FETCH cm.keyPlot kp
JOIN FETCH kp.btrData b
WHERE cm.zone.zoneId = :zoneId
AND cm.isReject = false
""")
  List<ClusterMaster> findAllWithDetails(@Param("zoneId") Integer zoneId);

  //Report Generation Status of Cluster details

    //state and district wise list
  @Query("""
    SELECT DISTINCT cm
    FROM ClusterMaster cm
    JOIN FETCH cm.keyPlot kp
    JOIN FETCH cm.zone z
    LEFT JOIN FETCH z.districtMaster

    WHERE
    (
        CAST(:startDate AS timestamp) IS NULL
        OR cm.createdAt >= :startDate
    )

    AND
    (
        CAST(:endDate AS timestamp) IS NULL
        OR cm.createdAt <= :endDate
    )
""")
  List<ClusterMaster> getDashboardData(

          @Param("startDate")
          LocalDateTime startDate,

          @Param("endDate")
          LocalDateTime endDate
  );

  //based on districtId district and talukwise status list
  @Query("""
    SELECT DISTINCT cm
    FROM ClusterMaster cm
    JOIN FETCH cm.keyPlot kp
    JOIN FETCH cm.zone z
    JOIN FETCH z.desTalukMaster dt

    WHERE z.distId = :districtId
  
    AND
    (
        CAST(:startDate AS timestamp) IS NULL
        OR cm.createdAt >= :startDate
    )

    AND
    (
        CAST(:endDate AS timestamp) IS NULL
        OR cm.createdAt <= :endDate
    )
""")
  List<ClusterMaster> getTalukWiseDashboardData(

          @Param("districtId")
          Integer districtId,

          @Param("startDate")
          LocalDateTime startDate,

          @Param("endDate")
          LocalDateTime endDate
  );

  //based on taluk id gets details of zone status
    @Query("""
    SELECT DISTINCT cm
    FROM ClusterMaster cm
    JOIN FETCH cm.keyPlot kp
    JOIN FETCH cm.zone z
    JOIN FETCH z.desTalukMaster dt

    WHERE z.desTalukId = :talukId

    AND
    (
        CAST(:startDate AS timestamp) IS NULL
        OR cm.createdAt >= :startDate
    )

    AND
    (
        CAST(:endDate AS timestamp) IS NULL
        OR cm.createdAt <= :endDate
    )
""")
    List<ClusterMaster> getZoneWiseDashboardData(

            @Param("talukId")
            Integer talukId,

            @Param("startDate")
            LocalDateTime startDate,

            @Param("endDate")
            LocalDateTime endDate
    );


//  @Query("""
//SELECT new cdti.aidea.earas.contract.Projection.ClusterSummaryProjection(
//
//    cm.cluMasterId,
//    cm.clusterNumber,
//
//    kp.id,
//    kp.landType,
//
//    COALESCE(v.villageNameEn, 'Village not found'),
//    b.vcode,
//
//    COALESCE(lb.localbodyNameEn, 'Local body not found'),
//    COALESCE(lbt.name, 'Unknown'),
//    b.lbcode,
//
//    b.bcode,
//    CONCAT(b.resvno, '/', b.resbdno),
//
//    cm.status
//)
//FROM ClusterMaster cm
//JOIN cm.keyPlot kp
//JOIN kp.btrData b
//
//LEFT JOIN TblMasterVillage v ON v.lsgCode = b.lsgcode
//LEFT JOIN TblLocalBody lb ON lb.codeApi = b.lbcode
//LEFT JOIN LocalBodyType lbt ON lbt.id = lb.localbodyType
//
//WHERE cm.zone.zoneId = :zoneId
//AND cm.isReject = false
//
//ORDER BY cm.clusterNumber
//""")
//  List<ClusterSummaryProjection> findClusterSummary(@Param("zoneId") Integer zoneId);

    //after adding agri year
@Query("""
SELECT new cdti.aidea.earas.contract.Projection.ClusterSummaryProjection(

    cm.cluMasterId,
    cm.clusterNumber,

    kp.id,
    kp.landType,

    COALESCE(v.villageNameEn, 'Village not found'),
    b.vcode,

    COALESCE(lb.localbodyNameEn, 'Local body not found'),
    COALESCE(lbt.name, 'Unknown'),
    b.lbcode,

    b.bcode,
    CONCAT(b.resvno, '/', b.resbdno),

    cm.status
)
FROM ClusterMaster cm
JOIN cm.keyPlot kp
JOIN kp.btrData b

LEFT JOIN TblMasterVillage v ON v.lsgCode = b.lsgcode
LEFT JOIN TblLocalBody lb ON lb.codeApi = b.lbcode
LEFT JOIN LocalBodyType lbt ON lbt.id = lb.localbodyType

WHERE cm.zone.zoneId = :zoneId
AND cm.isReject = false

AND YEAR(kp.agriStartYear) = :startYear
AND YEAR(kp.agriEndYear) = :endYear

ORDER BY cm.clusterNumber
""")
List<ClusterSummaryProjection> findClusterSummaryByYear(
        @Param("zoneId") Integer zoneId,
        @Param("startYear") Integer startYear,
        @Param("endYear") Integer endYear
);

  @Query("""
SELECT cfd.clusterMaster.cluMasterId as clusterId,
       SUM(cfd.enumeratedArea) as totalArea
FROM ClusterFormData cfd
WHERE cfd.clusterMaster.cluMasterId IN :clusterIds
GROUP BY cfd.clusterMaster.cluMasterId
""")
  List<ClusterAreaProjection> findTotalAreaByClusterIds(List<Long> clusterIds);
//before including agri start and end year
//  @Query("""
//SELECT new cdti.aidea.earas.contract.Projection.ClusterSummaryFastProjection(
//
//    cm.cluMasterId,
//    cm.clusterNumber,
//
//    kp.id,
//    kp.landType,
//
//    COALESCE(v.villageNameEn, 'Village not found'),
//    b.vcode,
//
//    COALESCE(lb.localbodyNameEn, 'Local body not found'),
//    COALESCE(lbt.name, 'Unknown'),
//    b.lbcode,
//
//    b.bcode,
//    CONCAT(b.resvno, '/', b.resbdno),
//
//    b.totCent
//)
//FROM ClusterMaster cm
//JOIN cm.keyPlot kp
//JOIN kp.btrData b
//
//LEFT JOIN TblMasterVillage v ON v.lsgCode = b.lsgcode
//LEFT JOIN TblLocalBody lb ON lb.codeApi = b.lbcode
//LEFT JOIN LocalBodyType lbt ON lbt.id = lb.localbodyType
//
//WHERE cm.zone.zoneId = :zoneId
//AND cm.isReject = false
//
//ORDER BY cm.clusterNumber
//""")
//  List<ClusterSummaryFastProjection> getClusterSummaryFast(@Param("zoneId") Integer zoneId);


//after including  agri start and end year
@Query("""
SELECT new cdti.aidea.earas.contract.Projection.ClusterSummaryFastProjection(

    cm.cluMasterId,
    cm.clusterNumber,

    kp.id,
    kp.landType,

    COALESCE(v.villageNameEn, 'Village not found'),
    b.vcode,

    COALESCE(lb.localbodyNameEn, 'Local body not found'),
    COALESCE(lbt.name, 'Unknown'),
    b.lbcode,

    b.bcode,
    CONCAT(b.resvno, '/', b.resbdno),

    b.totCent
)
FROM ClusterMaster cm
JOIN cm.keyPlot kp
JOIN kp.btrData b

LEFT JOIN TblMasterVillage v ON v.lsgCode = b.lsgcode
LEFT JOIN TblLocalBody lb ON lb.codeApi = b.lbcode
LEFT JOIN LocalBodyType lbt ON lbt.id = lb.localbodyType

WHERE cm.zone.zoneId = :zoneId
AND cm.isReject = false

AND YEAR(kp.agriStartYear) = :startYear
AND YEAR(kp.agriEndYear) = :endYear

ORDER BY cm.clusterNumber
""")
List<ClusterSummaryFastProjection> getClusterSummaryFastByYear(
        @Param("zoneId") Integer zoneId,
        @Param("startYear") Integer startYear,
        @Param("endYear") Integer endYear
);
}
