package cdti.aidea.earas.model.Btr_models.Masters;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_master_block")
public class MasterBlock {

    @Id
    @Column(name = "block_id")
    private int blockId;

    @Column(name = "block_code", nullable = false, length = 32)
    private String blockCode;

    @Column(name = "block_name", nullable = false, length = 255)
    private String blockName;

    @Column(name = "district", nullable = false)
    private int district;

    @Column(name = "is_valid", nullable = false)
    private boolean isValid;

    @Column(name = "lsg_code", nullable = false)
    private int lsgCode;
}