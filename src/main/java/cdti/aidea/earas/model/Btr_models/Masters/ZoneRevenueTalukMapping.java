package cdti.aidea.earas.model.Btr_models.Masters;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_zone_rev_taluk_updated_mapping")
public class ZoneRevenueTalukMapping {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "zone_revenue_taluk_mapping_id")
  private Long zoneRevenueTalukMappingId;

  @Column(name = "zone")
  private Integer zone;

  @Column(name = "revenue_taluk")
  private Integer revenueTaluk;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "is_valid")
  private Boolean isValid;

  @Column(name = "rev_taluk_name_en")
  private String revTalukNameEn;
}
