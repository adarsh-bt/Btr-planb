package cdti.aidea.earas.model.Btr_models.Masters;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_zone_village_block_mapping")
public class TblZoneVillageBlockMapping{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "zone_village_block_mapping_id")
    private Long zoneVillageBlockMappingId;

    @Column(name = "zone", nullable = false)
    private Integer zone;

    @Column(name = "village_block_id", nullable = false)
    private Integer villageBlockId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_valid", nullable = false)
    private Boolean isValid = true;


}
