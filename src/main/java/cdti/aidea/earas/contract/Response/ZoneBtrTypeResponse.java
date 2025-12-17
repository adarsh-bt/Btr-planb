package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZoneBtrTypeResponse {
    private Integer zoneId;
    private String zoneName;
    private Integer btrTypeId;
    private String btrType;
    private boolean isBtr;
}