package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.contract.Projection.BtrStatsProjection;
import cdti.aidea.earas.model.Btr_models.TblBtrData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TblBtrDataRepository extends JpaRepository<TblBtrData, Long> {

  boolean existsByDcodeAndTcodeAndLbcodeAndVcodeAndBcodeAndResvnoAndResbdno(
      Integer dcode, Integer tcode,String lbcode, Integer vcode, String bcode, Integer resvno, String resbdno);

  boolean existsByDcodeAndTcodeAndLbcodeAndVcodeAndBcodeAndResvno(
          Integer dcode, Integer tcode, String lbcode, Integer vcode, String bcode, Integer resvno);
  boolean existsByDcodeAndLbcodeAndWardnumberAndHouseno(
          Integer dcode,
          String lbcode,
          Integer wardNumber,
//          Integer houseno
          String houseno
  );

  boolean existsByDcodeAndTcodeAndLbcodeAndVcodeAndBcodeAndOwnernameAndAddressAndTotCent(
          Integer dcode,
          Integer tcode,
          String lbcode,
          Integer vcode,
          String bcode,
          String ownername,
          String address,
          Double totCent
  );


//  Optional<TblBtrData> findByDcodeAndTcodeAndVcodeAndBcodeAndResvnoAndResbdno(
//          Integer dcode, Integer tcode, Integer vcode, String bcode, Integer resvno, String resbdno);

  List<TblBtrData> findByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndResvnoAndResbdno(
          Integer dcode, Integer tcode, Integer vcode, String bcode, String lbcode,Integer resvno, String resbdno);

  // Find records by a list of lsgcodes (pageable)
  Page<TblBtrData> findByLsgcodeIn(List<Integer> lsgcodes, Pageable pageable);

  // Find all records by a list of lsgcodes
  List<TblBtrData> findAllByLsgcodeIn(List<Integer> lsgcodes);

  List<TblBtrData> findByZone(Long zone);

  // Custom query to filter based on multiple fields
  @Query(
      "SELECT b FROM TblBtrData b WHERE b.lsgcode IN :lsgcodes AND "
          + "(CAST(b.bcode AS string) LIKE %:filter% OR "
          + "CAST(b.resvno AS string) LIKE %:filter% OR "
          + "CAST(b.resbdno AS string) LIKE %:filter% OR "
          + "b.ltype LIKE %:filter%)")
  Page<TblBtrData> findByLsgcodeInAndFilter(
      @Param("lsgcodes") List<Integer> lsgcodes, @Param("filter") String filter, Pageable pageable);

  List<TblBtrData> findAllByLbcodeAndLtype(String lbcode, String ltype);

  List<TblBtrData> findAllByLbcode(String lbcode);

  List<TblBtrData> findAllByLbcodeAndResvnoAndLtype(String lbcode, Integer resvno, String ltype);

  List<TblBtrData> findAllByLbcodeAndResvno(String lbcode, Integer resvno);
  //    Optional<TblBtrDataOld> findByLbcodeAndResvnoAndResbdno(String lbcode, Integer resvno,
  // Integer resbdno);
  Optional<TblBtrData> findByLbcodeAndResvnoAndResbdnoAndLtype(
      String lbcode, Integer resvno, String resbdno, String ltype);

  Optional<TblBtrData> findByResvnoAndResbdno(Integer resvno, String resbdno);

  List<TblBtrData> findByLsgcodeAndBcodeAndLtype(Integer lsgcode, String bcode, String ltype);

  List<TblBtrData> findByLsgcodeAndBcode(Integer lsgcode, String bcode);

  List<TblBtrData> findByLsgcodeAndBcodeAndLtypeAndResvno(
      Integer lsgcode, String bcode, String ltype, Integer resvno);

  List<TblBtrData> findByLsgcodeAndBcodeAndLtypeAndResvnoBetween(
      Integer lsgcode, String bcode, String ltype, Integer start, Integer end);

  List<TblBtrData> findByLsgcodeAndBcodeAndResvnoBetween(
      Integer lsgcode, String bcode, Integer start, Integer end);

  @Query(
          "SELECT b FROM TblBtrData b "
                  + "JOIN TblLocalBody lb ON b.lbcode = lb.codeApi "
                  + "WHERE b.zone = :zone "
                  + "ORDER BY lb.localbodyNameEn ASC, "
                  + "b.lsgcode ASC, "
                  + "b.bcode ASC, "
                  + "b.resvno ASC, "
                  + "b.resbdno ASC, "
                  + "b.ltype DESC")
  Page<TblBtrData> findByZoneWithOrder(
          @Param("zone") Long zone,
          Pageable pageable);

  @Query(
          "SELECT b FROM TblBtrData b "
                  + "JOIN TblMasterVillage v ON b.lsgcode = v.lsgCode "
                  + "JOIN TblLocalBody lb ON b.lbcode = lb.codeApi "
                  + "WHERE b.zone = :zone AND ("
                  + "LOWER(v.villageNameEn) LIKE LOWER(CONCAT('%', :filter, '%')) OR "
                  + "LOWER(lb.localbodyNameEn) LIKE LOWER(CONCAT('%', :filter, '%')) OR "
                  + "CAST(b.bcode AS string) LIKE CONCAT('%', :filter, '%') OR "
                  + "CAST(b.resvno AS string) LIKE CONCAT('%', :filter, '%') OR "
                  + "CAST(b.resbdno AS string) LIKE CONCAT('%', :filter, '%') OR "
                  + "LOWER(b.ltype) LIKE LOWER(CONCAT('%', :filter, '%')) OR "
                  + "CONCAT(CAST(b.resvno AS string), '/', CAST(b.resbdno AS string)) LIKE CONCAT('%', :filter, '%')"
                  + ") "
                  + "ORDER BY lb.localbodyNameEn ASC, "
                  + "v.villageNameEn ASC, "
                  + "b.bcode ASC, "
                  + "b.resvno ASC, "
                  + "b.resbdno ASC, "
                  + "b.ltype DESC")
  Page<TblBtrData> findByZoneWithNamesFilter(
          @Param("zone") Long zone,
          @Param("filter") String filter,
          Pageable pageable);

  List<TblBtrData> findByLbcode(String lbcode);

// boolean existsByResvnoAndResbdno(Integer resvno, String resbdno);
    boolean existsByResbdno(String resbdno);

//  Optional<TblBtrData> findByDcodeAndTcodeAndVcodeAndBcodeAndResvno(
//      Integer dcode, Integer tcode, Integer vcode, String bcode, Integer resvno);
List<TblBtrData> findByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndResvno(
        Integer dcode, Integer tcode, Integer vcode, String bcode,String lbcode ,Integer resvno);

  // For BTR type 2 - House List
  boolean existsByDcodeAndTcodeAndLbcodeAndWardnumberAndHouseno(
          Integer dcode, Integer tcode, String lbcode, Integer wardNumber, String houseno);

  // For BTR type 3 - Cultivators List
  boolean existsByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndResvnoAndResbdno(Integer dcode, Integer tcode, Integer vcode, String bcode, String lbcode, Integer resvno, String resbdno);
  boolean existsByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndResvno(Integer dcode, Integer tcode, Integer vcode, String bcode, String lbcode, Integer resvno);

  // For BTR type 4 - Thandaper Number
  boolean existsByDcodeAndTcodeAndVcodeAndBcodeAndTpnoAndTbsubdivisionno(
          Integer dcode, Integer tcode, Integer vcode, String bcode, Integer tpno, String tbsubdivisionno);

  boolean existsByDcodeAndTcodeAndVcodeAndBcodeAndTpno(
          Integer dcode, Integer tcode, Integer vcode, String bcode, Integer tpno);

  // For BTR type 5 - Old Survey Number
  boolean existsByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndOldsvnoAndOldsubno(
          Integer dcode, Integer tcode, Integer vcode, String bcode, String lbcode,Integer oldsvno, String oldsubno);

  boolean existsByDcodeAndTcodeAndVcodeAndBcodeAndLbcodeAndOldsvno(
          Integer dcode, Integer tcode, Integer vcode, String bcode, String lbcode,Integer oldsvno);

  // Query methods to get actual data for remaining area calculation
  List<TblBtrData> findByDcodeAndTcodeAndLbcodeAndWardnumberAndHouseno(
          Integer dcode, Integer tcode, String lbcode, Integer wardNumber, String houseno);

  List<TblBtrData> findByDcodeAndTcodeAndLbcodeAndVcodeAndBcodeAndOwnernameAndAddressAndTotCent(
          Integer dcode, Integer tcode, String lbcode, Integer vcode, String bcode,
          String ownername, String address, Double totCent);

  List<TblBtrData> findByDcodeAndTcodeAndVcodeAndBcodeAndTpnoAndTbsubdivisionno(
          Integer dcode, Integer tcode, Integer vcode, String bcode, Integer tpno, String tbsubdivisionno);

  List<TblBtrData> findByDcodeAndTcodeAndVcodeAndBcodeAndTpno(
          Integer dcode, Integer tcode, Integer vcode, String bcode, Integer tpno);

  List<TblBtrData> findByDcodeAndTcodeAndVcodeAndBcodeAndOldsvnoAndOldsubno(
          Integer dcode, Integer tcode, Integer vcode, String bcode, Integer oldsvno, String oldsubno);

  List<TblBtrData> findByDcodeAndTcodeAndVcodeAndBcodeAndOldsvno(
          Integer dcode, Integer tcode, Integer vcode, String bcode, Integer oldsvno);

  @Modifying
  @Query("UPDATE TblBtrData b SET b.totCent = :totCent, b.updationTime = :updationTime, b.updated_by = :updatedBy WHERE b.id = :btrId")
  int updateTotCent(
          @Param("btrId") Long btrId,
          @Param("totCent") Double totCent,
          @Param("updationTime") LocalDateTime updationTime,
          @Param("updatedBy") UUID updatedBy
  );

  @Query(value = """
    SELECT 
        lbcode,
        COUNT(*) FILTER (WHERE TRIM(UPPER(ltype)) = 'WET') AS wet_plot,
        COUNT(*) FILTER (WHERE TRIM(UPPER(ltype)) = 'DRY') AS dry_plot,
        COALESCE(SUM(tot_cent) FILTER (WHERE TRIM(UPPER(ltype)) = 'WET'), 0) AS wet_area,
        COALESCE(SUM(tot_cent) FILTER (WHERE TRIM(UPPER(ltype)) = 'DRY'), 0) AS dry_area,
        COALESCE(SUM(tot_cent), 0) AS total_area,
        COUNT(*) AS total_plot
    FROM tbl_btr_data
    WHERE zone = :zone
    GROUP BY lbcode
""", nativeQuery = true)
  List<BtrStatsProjection> getZoneStats(@Param("zone") Long zone);

  @Query(value = """
    SELECT 
        lbcode,
        STRING_AGG(DISTINCT village_name_en, ', ') AS villages,
        STRING_AGG(DISTINCT bcode, ', ') AS blocks
    FROM tbl_btr_data b
    JOIN tbl_master_village v ON b.vcode = v.village_id
    WHERE b.zone = :zone
    GROUP BY lbcode
""", nativeQuery = true)
  List<Object[]> getVillageBlockData(@Param("zone") Long zone);

  @Query(value = """
    SELECT 
        COALESCE(SUM(tot_cent) FILTER (WHERE TRIM(UPPER(ltype)) = 'WET'), 0) AS wet_area,
        COALESCE(SUM(tot_cent) FILTER (WHERE TRIM(UPPER(ltype)) = 'DRY'), 0) AS dry_area
    FROM tbl_btr_data
    WHERE zone = :zone
""", nativeQuery = true)
  Object getZoneTotals(Long zone);

}
