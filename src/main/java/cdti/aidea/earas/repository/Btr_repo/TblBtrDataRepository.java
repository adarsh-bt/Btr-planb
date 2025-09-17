package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.TblBtrData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TblBtrDataRepository extends JpaRepository<TblBtrData, Long> {

//    Optional<TblBtrData> findByLbcodeAndResvnoAndResbdnoAndLtype(String lbcode, Integer resvno, String resbdno, String ltype);
//
//    List<TblBtrData> findByLsgcodeAndBcodeAndLtype(Integer lsgcode, Integer bcode, String ltype);
//
//    List<TblBtrData> findByLsgcodeAndBcodeAndLtypeAndResvnoBetween(Integer lsgcode, Integer bcode, String ltype, Integer start, Integer end);
//
//    List<TblBtrData> findByLsgcodeAndBcodeAndLtypeAndResvno(Integer lsgcode, Integer bcode, String ltype, Integer resvno);

}
