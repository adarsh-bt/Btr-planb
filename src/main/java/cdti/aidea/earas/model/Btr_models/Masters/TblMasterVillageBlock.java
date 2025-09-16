package cdti.aidea.earas.model.Btr_models.Masters;

import jakarta.persistence.*;
import lombok.*;


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
}