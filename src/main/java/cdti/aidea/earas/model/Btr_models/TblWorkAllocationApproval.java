package cdti.aidea.earas.model.Btr_models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_work_allocation_approval")
public class TblWorkAllocationApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "status")
    private String status;

    @Column(name = "requested_by")
    private UUID requestedBy;

    @Column(name = "zone_id")
    private Integer zoneId;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "revoked_by")
    private UUID revokedBy;

    @Column(name = "revoked_at")
    private LocalDateTime RevokedAt;

    @Column(columnDefinition = "TEXT")
    private String revokedRemark;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(columnDefinition = "TEXT")
    private String remark;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime UpdatedAt;

    @Column(name = "agri_start")
    private LocalDate agriStart;

    @Column(name = "agri_end")
    private LocalDate agriEnd;


    @Column(name = "is_active")
    private Boolean isActive = true;
}