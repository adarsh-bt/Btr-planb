package cdti.aidea.earas.model.Btr_models.Masters;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Builder
@Setter
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@Entity(name = "tbl_master_district")
public class DistrictMaster {
  @Id private Integer dist_id;
    private String distNameEn;
    private String distNameMal;
  @Column(name = "is_active")
  private boolean active;
  private Integer dist_lsg_code;
  private String dist_code;
  private String census_code_2011;
  private String census_code_2001;
  private Integer des_dist_code;
    @Column(name = "created_by")
    private UUID addedBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
