package cdti.aidea.earas.contract.Response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KeyplotCountResponse {
    private Integer zoneId;
    private Long allowedKeyplotsLimit;
    private Long usedKeyplotsCount;
    private Long remainingKeyplots;
}
