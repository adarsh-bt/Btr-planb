package cdti.aidea.earas.model.Btr_models.Masters;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@Builder
@Setter
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@Entity(name = "tbl_master_des_taluk_office")
public class DesTalukMaster {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long desTalukOfficeId;

  private Long desTalukId;
  private String talukOfficeNameEn;
  private boolean isActive;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dist_office_id", nullable = false)
  @JsonBackReference
  private DistrictMaster districtMaster;
}
