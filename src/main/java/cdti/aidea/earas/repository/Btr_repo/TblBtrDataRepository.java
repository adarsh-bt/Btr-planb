package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.TblBtrData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TblBtrDataRepository extends JpaRepository<TblBtrData, Long> {

    // Find records by a list of lsgcodes (pageable)
    Page<TblBtrData> findByLsgcodeIn(List<Integer> lsgcodes, Pageable pageable);

    // Find all records by a list of lsgcodes
    List<TblBtrData> findAllByLsgcodeIn(List<Integer> lsgcodes);

    // Custom query to filter based on multiple fields
    @Query("SELECT b FROM TblBtrData b WHERE b.lsgcode IN :lsgcodes AND " +
            "(CAST(b.bcode AS string) LIKE %:filter% OR " +
            "CAST(b.resvno AS string) LIKE %:filter% OR " +
            "CAST(b.resbdno AS string) LIKE %:filter% OR " +
            "b.ltype LIKE %:filter%)")
    Page<TblBtrData> findByLsgcodeInAndFilter(@Param("lsgcodes") List<Integer> lsgcodes,
                                              @Param("filter") String filter,
                                              Pageable pageable);

    List<TblBtrData> findAllByLbcodeAndLtype(String lbcode, String ltype);
    List<TblBtrData> findAllByLbcode(String lbcode);
    List<TblBtrData> findAllByLbcodeAndResvnoAndLtype(String lbcode, Integer resvno, String ltype);
    List<TblBtrData> findAllByLbcodeAndResvno(String lbcode, Integer resvno);

    Optional<TblBtrData> findByLbcodeAndResvnoAndResbdnoAndLtype(
            String lbcode,
            Integer resvno,
            String resbdno,
            String ltype
    );

    Optional<TblBtrData> findByResvnoAndResbdno(Integer resvno, String resbdno);
    List<TblBtrData> findByLsgcodeAndBcodeAndLtype(Integer lsgcode, String bcode, String ltype);
    List<TblBtrData> findByLsgcodeAndBcode(Integer lsgcode, String bcode);
    List<TblBtrData> findByLsgcodeAndBcodeAndLtypeAndResvno(Integer lsgcode, String bcode, String ltype, Integer resvno);
    List<TblBtrData> findByLsgcodeAndBcodeAndLtypeAndResvnoBetween(Integer lsgcode, String bcode, String ltype, Integer start, Integer end);
    List<TblBtrData> findByLsgcodeAndBcodeAndResvnoBetween(Integer lsgcode, String bcode, Integer start, Integer end);

    @Query("SELECT b FROM TblBtrData b " +
            "JOIN TblLocalBody lb ON b.lbcode = lb.codeApi " +
            "WHERE b.lsgcode IN :lsgcodes " +
            "ORDER BY lb.localbodyNameEn ASC, " +
            "b.lsgcode ASC, " +
            "b.bcode ASC, " +
            "b.resvno ASC, " +
            "b.resbdno ASC, " +
            "b.ltype DESC")
    Page<TblBtrData> findByLsgcodeInWithOrder(List<Integer> lsgcodes, Pageable pageable);

    @Query("SELECT b FROM TblBtrData b " +
            "JOIN TblMasterVillage v ON b.lsgcode = v.lsgCode " +
            "JOIN TblLocalBody lb ON b.lbcode = lb.codeApi " +
            "WHERE b.lsgcode IN :lsgcodes AND (" +
            "LOWER(v.villageNameEn) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
            "LOWER(lb.localbodyNameEn) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
            "CAST(b.bcode AS string) LIKE CONCAT('%', :filter, '%') OR " +
            "CAST(b.resvno AS string) LIKE CONCAT('%', :filter, '%') OR " +
            "CAST(b.resbdno AS string) LIKE CONCAT('%', :filter, '%') OR " +
            "LOWER(b.ltype) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
            "CONCAT(CAST(b.resvno AS string), '/', CAST(b.resbdno AS string)) LIKE CONCAT('%', :filter, '%')" +
            ") " +
            "ORDER BY lb.localbodyNameEn ASC, " +
            "v.villageNameEn ASC, " +
            "b.bcode ASC, " +
            "b.resvno ASC, " +
            "b.resbdno ASC, " +
            "b.ltype DESC")
    Page<TblBtrData> findByLsgcodeInWithNamesFilter(
            @Param("lsgcodes") List<Integer> lsgcodes,
            @Param("filter") String filter,
            Pageable pageable
    );

    List<TblBtrData> findByLbcode(String lbcode);
}
