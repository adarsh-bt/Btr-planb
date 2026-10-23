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
@Table(name = "keyplot_deletion_audit")
@Getter
@Setter
public class KeyplotDeletionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "audit_id", nullable = false)
    private UUID auditId;

    @Column(name = "request_id", nullable = false, unique = true)
    private UUID requestId;

    @Column(name = "zone_id", nullable = false)
    private Long zoneId;

    @Column(name = "agri_year", nullable = false, length = 9)
    private String agriYear;

    @Column(name = "requested_by", nullable = false)
    private UUID requestedBy;

    @Column(name = "approved_by", nullable = false)
    private UUID approvedBy;

    @Column(name = "deletion_reason", nullable = false,
            columnDefinition = "text")
    private String deletionReason;

    @Column(name = "admin_remark", columnDefinition = "text")
    private String adminRemark;

    @Column(name = "keyplots_deleted", nullable = false)
    private Integer keyplotsDeleted = 0;

    @Column(name = "cluster_master_deleted", nullable = false)
    private Integer clusterMasterDeleted = 0;

    @Column(name = "cluster_details_deleted", nullable = false)
    private Integer clusterDetailsDeleted = 0;

    @Column(name = "tour_diary_deleted", nullable = false)
    private Integer tourDiaryDeleted = 0;

    @Column(name = "form_data_deleted", nullable = false)
    private Integer formDataDeleted = 0;

    @Column(name = "nuc_deleted", nullable = false)
    private Integer nucDeleted = 0;

    @Column(name = "crop_records_deleted", nullable = false)
    private Integer cropRecordsDeleted = 0;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "requested_at", nullable = false)
    private OffsetDateTime requestedAt;

    @Column(name = "approved_at", nullable = false)
    private OffsetDateTime approvedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "deletion_summary", columnDefinition = "jsonb")
    private Map<String, Object> deletionSummary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "failure_details", columnDefinition = "jsonb")
    private Map<String, Object> failureDetails;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void beforeInsert() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}