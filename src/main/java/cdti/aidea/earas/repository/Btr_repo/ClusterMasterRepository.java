package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.contract.FormEntryDto.FormClusterDetailsResponse;
import cdti.aidea.earas.contract.Projection.ClusterSummaryFastProjection;
import cdti.aidea.earas.contract.Projection.ClusterSummaryProjection;
import cdti.aidea.earas.contract.Response.ClusterIdNumberResponse;
import cdti.aidea.earas.contract.Response.KeyPlotDetailsListResponse;
import cdti.aidea.earas.model.Btr_models.ClusterMaster;
import cdti.aidea.earas.model.Btr_models.KeyPlots;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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


    Optional<ClusterMaster> findTopByKeyPlotOrderByCreatedAtDesc(KeyPlots keyPlot);

    Optional<ClusterMaster> findByKeyPlot(KeyPlots plot);


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


    @Query("""
                SELECT MAX(c.clusterNumber)
                FROM ClusterMaster c
                JOIN c.keyPlot kp
                WHERE c.zone.zoneId = :zoneId
                  AND kp.agriStartYear = :agriStart
                  AND kp.agriEndYear = :agriEnd
            """)
    Optional<Integer> findMaxClusterNumberByAgriYear(
            @Param("zoneId") Integer zoneId,
            @Param("agriStart") LocalDate agriStart,
            @Param("agriEnd") LocalDate agriEnd
    );

    Optional<ClusterMaster> findByKeyPlot_Id(UUID kpId);
//  Optional<ClusterFormData> findByClusterMaster(ClusterMaster clusterMaster);


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
            AND kp.agriStartYear = :agriStart
            AND kp.agriEndYear = :agriEnd
            
            ORDER BY cm.clusterNumber
            """)
    Page<KeyPlotDetailsListResponse> findAllKeyPlotDetails(
            @Param("zoneId") Integer zoneId,
            @Param("agriStart") LocalDate agriStart,
            @Param("agriEnd") LocalDate agriEnd,
            Pageable pageable
    );


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
            AND kp.agriStartYear = :agriStart
            AND kp.agriEndYear = :agriEnd
            
            ORDER BY cm.clusterNumber
            """)
    List<ClusterSummaryProjection> findClusterSummary(
            @Param("zoneId") Integer zoneId,
            @Param("agriStart") LocalDate agriStart,
            @Param("agriEnd") LocalDate agriEnd
    );


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
            AND kp.agriStartYear = :agriStart
            AND kp.agriEndYear = :agriEnd
            
            ORDER BY cm.clusterNumber
            """)
    List<ClusterSummaryFastProjection> getClusterSummaryFast(
            @Param("zoneId") Integer zoneId,
            @Param("agriStart") LocalDate agriStart,
            @Param("agriEnd") LocalDate agriEnd
    );


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

    @Query("""
    SELECT DISTINCT cm
    FROM ClusterMaster cm
    JOIN FETCH cm.keyPlot kp
    JOIN FETCH cm.zone z
    JOIN FETCH z.desTalukMaster dt

    WHERE z.distId = :districtId

      AND cm.createdAt >= :startDate
      AND cm.createdAt <= :endDate
""")
    List<ClusterMaster> getTalukWiseDashboardDataByAgriYear(

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


    @Query("""
    SELECT DISTINCT cm
    FROM ClusterMaster cm
    JOIN FETCH cm.keyPlot kp
    JOIN FETCH cm.zone z
    JOIN FETCH z.desTalukMaster dt

    WHERE z.desTalukId = :talukId

    AND cm.createdAt >= :startDate

    AND cm.createdAt <= :endDate
""")
    List<ClusterMaster> getZoneWiseDashboardDataByAgriYear(

            @Param("talukId")
            Integer talukId,

            @Param("startDate")
            LocalDateTime startDate,

            @Param("endDate")
            LocalDateTime endDate
    );
    @Query("""
                SELECT DISTINCT cm
                FROM ClusterMaster cm
                JOIN FETCH cm.keyPlot kp
                JOIN FETCH cm.zone z
                JOIN FETCH z.desTalukMaster dt
            
                WHERE z.zoneId = :zoneId
            
                AND kp.agriStartYear = :agriStartYear
            
                AND kp.agriEndYear = :agriEndYear
            
                AND UPPER(cm.status) = 'COMPLETED'
            """)
    List<ClusterMaster> findClustersByDistrictAndAgriYear(

            @Param("zoneId")
            Integer zoneId,

            @Param("agriStartYear")
            LocalDate agriStartYear,

            @Param("agriEndYear")
            LocalDate agriEndYear
    );

    @Query("""
                SELECT DISTINCT cm
                FROM ClusterMaster cm
                JOIN FETCH cm.keyPlot kp
                JOIN FETCH cm.zone z
                WHERE z.zoneId = :zoneId
                  AND kp.agriStartYear = :agriStartYear
                  AND kp.agriEndYear = :agriEndYear
                  AND UPPER(cm.status) = 'COMPLETED'
            """)
    List<ClusterMaster> findCompletedClustersByZoneAndAgriYear(
            @Param("zoneId") Integer zoneId,
            @Param("agriStartYear") LocalDate agriStartYear,
            @Param("agriEndYear") LocalDate agriEndYear);

    @Query("""
SELECT new cdti.aidea.earas.contract.Response.ClusterIdNumberResponse(
    cm.cluMasterId,
    cm.clusterNumber,
    kp.landType
)
FROM ClusterMaster cm
JOIN cm.keyPlot kp
WHERE cm.zone.zoneId = :zoneId
AND kp.agriStartYear = :agriStart
AND kp.agriEndYear = :agriEnd
AND cm.is_active = true
AND cm.isReject = false
ORDER BY cm.clusterNumber
""")
    List<ClusterIdNumberResponse> findClusterIdAndNumber(
            @Param("zoneId") Integer zoneId,
            @Param("agriStart") LocalDate agriStart,
            @Param("agriEnd") LocalDate agriEnd
    );


    @Query(value = """
SELECT
    cm.clu_master_id AS clusterId,
    cm.cluster_number AS clusterNo,
    lb.localbody_name_en AS localBodyName
FROM cluster_master cm
JOIN keyplot_selections kp
    ON cm.plot_id = kp.kp_id
JOIN tbl_btr_data btr
    ON kp.btr_id = btr.id
LEFT JOIN tbl_master_localbody lb
    ON btr.lbcode = lb.code_api
WHERE cm.clu_master_id IN (:clusterIds)
""", nativeQuery = true)
    List<Object[]> findClusterLocalBodyDetails(@Param("clusterIds") List<Long> clusterIds);

    //test lbcode
//    @Query("""
//SELECT
//    lb.localbodyId,
//    lb.localbodyNameEn,
//    lb.codeApi
//FROM ClusterMaster cm
//JOIN cm.keyPlot kp
//JOIN kp.btrData bd
//JOIN TblLocalBody lb
//    ON lb.codeApi = bd.lbcode
//WHERE cm.cluMasterId = :clusterId
//""")
//    Object[] getClusterLocalBodyDetails(@Param("clusterId") Long clusterId);
    @Query(value = """
SELECT
    lb.localbody_id,
    lb.localbody_name_en,
    lb.code_api
FROM cluster_master cm
JOIN keyplot_selections kp
    ON cm.plot_id = kp.kp_id
JOIN tbl_btr_data bd
    ON kp.btr_id = bd.id
JOIN tbl_master_localbody lb
    ON lb.code_api = bd.lbcode
WHERE cm.clu_master_id = :clusterId
""", nativeQuery = true)
    List<Object[]> getClusterLocalBodyDetails(@Param("clusterId") Long clusterId);
}