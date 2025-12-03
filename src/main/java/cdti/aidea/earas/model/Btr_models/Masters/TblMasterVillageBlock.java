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
@Entity
@Table(name = "tbl_master_village_block")
public class TblMasterVillageBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "village_block_id")
    private Integer villageBlockId;

    @Column(name = "block_code", nullable = false)
    private String blockCode;

    @Column(name = "village_id", nullable = false)
    private Integer villageId;

    @Column(name = "added_by")
    private UUID addedBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private LocalDateTime isActive;
}
