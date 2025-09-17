package cdti.aidea.earas.model.Btr_models.Masters;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@Builder
@Setter
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@Entity(name = "tbl_master_district")
public class DistrictMaster {
  @Id
  private Integer dist_id;
  private String dist_name_en;
  private String dist_name_mal;
  private boolean is_active;
  private Integer dist_lsg_code;
  private String dist_code;
  private String census_code_2011;
  private String census_code_2001;
  private Integer des_dist_code;
}
