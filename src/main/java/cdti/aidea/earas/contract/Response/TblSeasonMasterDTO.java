package cdti.aidea.earas.contract.Response;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TblSeasonMasterDTO {
    private Long id;
    private String seasonName;
    private LocalDate defaultStart;
    private LocalDate defaultEnd;
    private UUID uuid;
    private Boolean isActive;
}
