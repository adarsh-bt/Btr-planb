package cdti.aidea.earas.model.Btr_models.Masters;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_zone_localbody_mapping")
public class TblZoneLocalbodyMapping {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "zone_localbody_mapping_id")
  private Integer zoneLocalbodyMappingId;

  private Integer zone; // zone id

  private Integer localbody; // localbody id

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  private Boolean isValid;
  @Column(name = "added_by")
  private UUID addedBy;

  @Column(name = "updated_by")
  private UUID updatedBy;

  @Column(name = "remarks")
  private String remarks;

}
