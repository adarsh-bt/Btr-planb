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
@Table(name = "tbl_master_block")
public class MasterBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "added_by")
    private UUID addedBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
