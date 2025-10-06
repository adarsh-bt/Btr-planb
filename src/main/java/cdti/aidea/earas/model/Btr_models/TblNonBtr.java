package cdti.aidea.earas.model.Btr_models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
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
}
