package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.Masters.TblLocalBody;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LocalBodyRepository extends JpaRepository<TblLocalBody, Integer> {

  // Corrected method name to match the entity field
  List<TblLocalBody> findAllByCodeApiIn(List<String> codeApis);

  Optional<TblLocalBody> findByCodeApi(String codeApi);

  // Alternatively, using a custom query
  @Query("SELECT lb FROM TblLocalBody lb WHERE lb.localbodyId IN :lbCodes")
  List<TblLocalBody> findAllByIdIn(@Param("lbCodes") List<String> lbCodes);

  @Query("SELECT lb FROM TblLocalBody lb WHERE lb.localbodyId IN :lbIds")
  List<TblLocalBody> findAllByLocalbodyIdIn(@Param("lbIds") List<Integer> lbIds);
}
