package cdti.aidea.earas.contract.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyplotCountResponse {
    private Integer zoneId;
    private Long allowedKeyplotsLimit;
    private Long usedKeyplotsCount;
    private Long remainingKeyplots;
}
