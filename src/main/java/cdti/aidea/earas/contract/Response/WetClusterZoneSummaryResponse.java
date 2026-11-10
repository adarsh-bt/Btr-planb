package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class WetClusterZoneSummaryResponse {
    private Long zoneId;
    private String zoneName;
    private Long lbcodeCount;

}
