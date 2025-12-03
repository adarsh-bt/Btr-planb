package cdti.aidea.earas.model.Btr_models.Masters;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "tbl_master_localbody_type")
public class LocalBodyType {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "localbody_type_id")
  private Long id;

  @Column(name = "localbody_type_name", nullable = false)
  private String name;

  @Column(name = "localbody_type_code", length = 10)
  private String code;

  @Column(name = "is_valid")
  private Boolean isValid;

    @Column(name = "added_by")
    private UUID addedBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
