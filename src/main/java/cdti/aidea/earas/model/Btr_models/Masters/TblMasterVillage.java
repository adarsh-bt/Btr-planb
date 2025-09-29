package cdti.aidea.earas.model.Btr_models.Masters;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_master_village")
public class TblMasterVillage implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "village_id")
  private Integer villageId;

  @Column(name = "village_name_en", nullable = false)
  private String villageNameEn;

  @Column(name = "village_name_mal", nullable = false)
  private String villageNameMal;

  @Column(name = "rev_taluk_id", nullable = false)
  private Integer revTalukId;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive;

  @Column(name = "village_code_api", nullable = false)
  private String villageCodeApi;

  @Column(name = "lsg_code", nullable = false)
  private Integer lsgCode;

  @Column(name = "census_code_2001")
  private String censusCode2001;

  @Column(name = "census_code_2011")
  private String censusCode2011;
}
