package cdti.aidea.earas.model.Btr_models.Masters;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_zone_localbody_block_mapping")
public class ZoneLocalbodyBlockMapping {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "zone_localbody_block_mapping_id")
  private Long id;

  @Column(name = "zone", nullable = false)
  private Integer zone;

  @Column(name = "block_panchayat_muncipal_area", nullable = false)
  private Integer blockPanchayatMunicipalArea;

  @Column(name = "block_details")
  private Integer blockDetails;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "is_valid", nullable = false)
  private Boolean isValid = true;
}
