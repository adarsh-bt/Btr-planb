package cdti.aidea.earas.model.Btr_models;

import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cluster_master")
public class ClusterMaster {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "clu_master_id")
  private Long cluMasterId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "plot_id", nullable = false)
  private KeyPlots keyPlot;

  @Column(name = "cluster_number")
  private Integer clusterNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "zone_id", nullable = false)
  private TblMasterZone zone;

  @Column(name = "status")
  private String status = "Not Started";

  @Column(name = "is_reject")
  private Boolean isReject = false;

  @Column(name = "is_active")
  private Boolean is_active = true;

  @Column(name = "investigator_remark")
  private String investigatorRemark;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();
}
