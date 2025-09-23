package cdti.aidea.earas.model.Btr_models;
import cdti.aidea.earas.model.Btr_models.Masters.TblLocalBody;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterVillage;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "village_localbody_map")
public class VillageLocalBodyMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relation to Village
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "village_id", referencedColumnName = "village_id")
    private TblMasterVillage village;
    // Relation to Localbody
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "localbody_id")
    private TblLocalBody localBody;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdDate = LocalDateTime.now();

}
