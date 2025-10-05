package cdti.aidea.earas.model.Btr_models;

import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cluster_approval_log")
public class ClusterApprovalLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cluster_id", nullable = false)
  private ClusterMaster clusterMaster;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "zone_id", nullable = false)
  private TblMasterZone zone;

  @Column(name = "total_area", precision = 10, scale = 2)
  private BigDecimal totalArea;

  @Column(name = "added_by")
  private UUID addedBy;

  @Column(name = "admin_id")
  private UUID adminId;

  @Column(name = "remarks", columnDefinition = "TEXT")
  private String remarks;

  @Column(name = "in_approved", columnDefinition = "BOOLEAN DEFAULT FALSE")
  private Boolean inApproved = false;

  @Column(
      name = "created_at",
      updatable = false,
      columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime updatedAt = LocalDateTime.now();

  @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT TRUE")
  private Boolean isActive = true;
}
