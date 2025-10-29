package cdti.aidea.earas.model.Btr_models;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "season_master_tbl")
@Getter
@Setter
public class TblSeasonMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "season_name", length = 100, nullable = false)
    private String seasonName;

    @Column(name = "default_start", nullable = false)
    private LocalDate defaultStart;

    @Column(name = "default_end", nullable = false)
    private LocalDate defaultEnd;

    @Column(name = "uuid", columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID uuid;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
