package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.TblWorkAllocationApproval;
import cdti.aidea.earas.model.Btr_models.TblWorkAllocationVerification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface TblWorkAllocationVerificationRepository extends JpaRepository<TblWorkAllocationVerification, Long> {
    Optional<TblWorkAllocationVerification>
    findByApproval_Id(Long approvalId);
}
