package cdti.aidea.earas.model.Btr_models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "crop_rejection_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropAssignmentTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "crop_id", nullable = false)
    private Long cropId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id")
    private ClusterMaster cluster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyplot_id")
    private KeyPlots keyPlot;


    @Column(name = "zone_id")
    private Long zoneId;

    @Column(name = "land_type")
    private String landType;

    @Column(name = "is_rejected")
    private Boolean isRejected = false;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "is_limit_exceeded")
    private Boolean isLimitExceeded = false;

    @Column(name = "is_current_assignment")
    private Boolean isCurrentAssignment = true;

    @Column(name = "rejected_by")
    private java.util.UUID rejectedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "assigned_on")
    private LocalDateTime assignedOn;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}