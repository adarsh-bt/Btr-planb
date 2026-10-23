package cdti.aidea.earas.repository.Btr_repo;

import cdti.aidea.earas.model.Btr_models.KeyplotDeletionAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface KeyplotDeletionAuditRepository
        extends JpaRepository<KeyplotDeletionAudit, UUID> {

    Optional<KeyplotDeletionAudit> findByRequestId(UUID requestId);
}