package cdti.aidea.earas.model.Btr_models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cluster_limit_log")
public class ClusterLimitLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "cluster_min", nullable = false, precision = 10, scale = 1)
  private BigDecimal clusterMin;

  @Column(name = "cluster_max", nullable = false, precision = 10, scale = 1)
  private BigDecimal clusterMax;

  @Column(name = "tso_approval_limit", nullable = false,precision = 10, scale = 1)
  private BigDecimal tsoApprovalLimit;

  @Column(name = "added_by")
  private UUID addedBy;

  @Column(columnDefinition = "TEXT")
  private String remarks;

  @Column(name = "agri_start_year", nullable = false)
  private LocalDate agriStartYear;

  @Column(name = "agri_end_year", nullable = false)
  private LocalDate agriEndYear;

  @Column(name = "in_active")
  private Boolean inActive = false;

  @Column(name = "edit_permitter")
  private UUID editPermitter;

  @Column(name = "is_edited")
  private Boolean isEdited = false;

  @Column(name = "created_at", columnDefinition = "TIMESTAMP WITHOUT TIME ZONE")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITHOUT TIME ZONE")
  private LocalDateTime updatedAt = LocalDateTime.now();

  @Column(name = "is_active")
  private Boolean isActive = true;


  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}









