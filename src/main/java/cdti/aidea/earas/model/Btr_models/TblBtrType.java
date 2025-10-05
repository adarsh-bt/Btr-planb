package cdti.aidea.earas.model.Btr_models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_master_btr_type")
public class TblBtrType {
  @Id
  @Column(name = "btr_type_id")
  private Integer btrTypeId;

  @Column(name = "btr_type", length = 50)
  private String btrType;

  @Column(name = "is_active")
  private Boolean isActive;
}
