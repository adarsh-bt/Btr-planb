package cdti.aidea.earas.model.Btr_models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "keyplot_deletion_request")
@Getter
@Setter
public class KeyplotDeletionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @Column(name = "request_number", nullable = false, unique = true)
    private String requestNumber;

    @Column(name = "keyplot_id")
    private UUID keyplotId;

    @Column(name = "zone_id", nullable = false)
    private Long zoneId;

    @Column(name = "btr_id", nullable = false)
    private Long btrId;

    @Column(name = "cluster_number")
    private Integer clusterNumber;

    @Column(name = "agri_year", nullable = false, length = 9)
    private String agriYear;

    @Column(name = "reason", nullable = false, columnDefinition = "text")
    private String reason;

    @Column(name = "status", nullable = false, length = 32)
    private String status = "PENDING";

    @Column(name = "requested_by", nullable = false)
    private UUID requestedBy;

    @Column(name = "requested_at", nullable = false)
    private OffsetDateTime requestedAt;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column(name = "admin_remark", columnDefinition = "text")
    private String adminRemark;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "impact_snapshot", columnDefinition = "jsonb")
    private Map<String, Object> impactSnapshot;

    @Column(name = "execution_started_at")
    private OffsetDateTime executionStartedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "failure_reason", columnDefinition = "text")
    private String failureReason;

    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @PrePersist
    void beforeInsert() {
        if (requestedAt == null) {
            requestedAt = OffsetDateTime.now();
        }
        if (correlationId == null) {
            correlationId = UUID.randomUUID();
        }
    }
}