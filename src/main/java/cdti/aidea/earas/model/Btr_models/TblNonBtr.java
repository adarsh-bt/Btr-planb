package cdti.aidea.earas.model.Btr_models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_master_non_btr")
public class TblNonBtr {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "b_type_id")
  private Long bTypeId;

  @Column(name = "b_type_name", nullable = false, length = 255)
  private String bTypeName;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive;

  @Column(name = "added_on", nullable = false)
  private LocalDateTime addedOn;

  @Column(name = "user_id")
  private Long userId;

    @Column(name = "added_by")
    private UUID addedBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
