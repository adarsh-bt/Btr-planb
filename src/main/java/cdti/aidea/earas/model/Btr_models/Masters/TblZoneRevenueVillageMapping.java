package cdti.aidea.earas.model.Btr_models.Masters;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "zone_revenue_village_mapping")
public class TblZoneRevenueVillageMapping {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "zone_revenue_village_mapping_id")
  private Long zoneRevenueVillageMappingId;

  @Column(name = "zone", nullable = false)
  private Integer zone;

  @Column(name = "revenue_village", nullable = false)
  private Integer revenueVillage;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "revenue_taluk", nullable = false)
  private Integer revenueTaluk;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "is_valid", nullable = false)
  private Boolean isValid = true;
  @Column(name = "added_by")
  private UUID addedBy;

  @Column(name = "updated_by")
  private UUID updatedBy;

  @Column(name = "remarks")
  private String remarks;
}
