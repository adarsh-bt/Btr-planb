package cdti.aidea.earas.model.Btr_models.Masters;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_master_taluk_des")
public class DesTaluk implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "des_taluk_id")
  private Integer desTalukId;

  @Column(name = "des_taluk_name_en", nullable = false)
  private String desTalukNameEn;

  @Column(name = "des_taluk_name_mal", nullable = false)
  private String desTalukNameMal;

  @Column(name = "dist_id", nullable = false)
  private int distId;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  @Column(name = "added_by")
  private UUID addedBy;

  @Column(name = "updated_by")
  private UUID updatedBy;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

}