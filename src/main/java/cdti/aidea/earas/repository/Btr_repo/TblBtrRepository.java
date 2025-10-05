package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.TblBtrData;
import java.util.List;
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
}
