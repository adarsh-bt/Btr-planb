package cdti.aidea.earas.model.Btr_models.Masters;

import cdti.aidea.earas.model.Btr_models.TblBtrType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_master_zone")
public class TblMasterZone {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "zone_id")
  private Integer zoneId;

  @Column(name = "zone_code")
  private Integer zoneCode;

  @Column(name = "zone_name_en", nullable = false)
  private String zoneNameEn;

  @Column(name = "zone_name_mal")
  private String zoneNameMal;

  @Column(name = "des_taluk_id", nullable = false)
  private Integer desTalukId;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "des_taluk_id", referencedColumnName = "des_taluk_id", insertable = false, updatable = false)
  private DesTaluk desTalukMaster;

  @Column(name = "des_dist_id", nullable = false)
  private Integer desDistId;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "dist_id", referencedColumnName = "dist_id", insertable = false, updatable = false)
  private DistrictMaster districtMaster;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive;

  @Column(name = "zone_user")
  private String zoneUser;

  @Column(name = "dist_id")
  private Integer distId;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "btr_type_id", referencedColumnName = "btr_type_id")
  private TblBtrType btrType;

  @Column(name = "added_by")
  private UUID addedBy;

  @Column(name = "updated_by")
  private UUID updatedBy;

  @Column(name = "created_at")
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  @UpdateTimestamp
  private LocalDateTime updatedAt;
}