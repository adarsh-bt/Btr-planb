package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.TblWorkAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TblWorkAllocationRepository extends JpaRepository<TblWorkAllocation, Long> {
    List<TblWorkAllocation> findByZone_ZoneId(Integer zoneId);

}