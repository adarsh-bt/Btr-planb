package cdti.aidea.earas.model.Btr_models.Masters;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
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
  @Column(name = "des_taluk_id")
  private int desTalukId;

  @Column(name = "des_taluk_name_en", nullable = false)
  private String desTalukNameEn;

  @Column(name = "des_taluk_name_mal", nullable = false)
  private String desTalukNameMal;

  @Column(name = "dist_id", nullable = false)
  private int distId;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;
}

