package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.TblBtrData;
import java.util.List;

import cdti.aidea.earas.model.Btr_models.TblBtrDataOld;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TblBtrRepository extends JpaRepository<TblBtrData, Long> {

  Page<TblBtrData> findByLsgcodeIn(List<Integer> lsgcodes, Pageable pageable);

  List<TblBtrData> findAllByLsgcodeIn(List<Integer> lsgcodes);

  @Query(
      "SELECT m FROM TblBtrData m WHERE m.lsgcode IN :lsgcodes AND "
          + "(CAST(m.vcode AS string) LIKE %:filter% OR "
          + "CAST(m.bcode AS string) LIKE %:filter% OR "
          + "CAST(m.resvno AS string) LIKE %:filter% OR "
          + "m.resbdno LIKE %:filter% OR "
          + "m.lbtype LIKE %:filter% OR "
          + "CAST(m.lbcode AS string) LIKE %:filter% OR "
          + "m.ltype LIKE %:filter%)")
  Page<TblBtrData> findByLsgcodeInAndFilter(
      @Param("lsgcodes") List<Integer> lsgcodes, @Param("filter") String filter, Pageable pageable);


  @Query(
          "SELECT b FROM TblBtrDataOld b "
                  + "JOIN TblLocalBody lb ON b.lbcode = lb.codeApi "
                  + "WHERE b.lsgcode IN :lsgcodes "
                  + "ORDER BY lb.localbodyNameEn ASC, "
                  + // 1. Localbody Name
                  "b.lsgcode ASC, "
                  + // 2. Village
                  "b.bcode ASC, "
                  + // 3. Block
                  "b.resvno ASC, "
                  + // 4. Survey No (part 1)
                  "b.resbdno ASC, "
                  + // 4. Survey No (part 2)
                  "b.ltype DESC") // 5. Land Type (reverse)
  Page<TblBtrDataOld> findByLsgcodeInWithOrder(List<Integer> lsgcodes, Pageable pageable);

  @Query(
          "SELECT b FROM TblBtrDataOld b "
                  + "JOIN TblMasterVillage v ON b.lsgcode = v.lsgCode "
                  + "JOIN TblLocalBody lb ON b.lbcode = lb.codeApi "
                  + "WHERE b.lsgcode IN :lsgcodes AND ("
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
  Page<TblBtrDataOld> findByLsgcodeInWithNamesFilter(
          @Param("lsgcodes") List<Integer> lsgcodes, @Param("filter") String filter, Pageable pageable);
}
