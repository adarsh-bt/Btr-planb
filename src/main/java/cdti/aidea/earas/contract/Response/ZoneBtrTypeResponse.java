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
    private String zoneNameMal;
    private Integer distId;
    private Integer desTalukId;
    private Integer btrTypeId;
    private String districtName;
    private String TalukName;
    private String btrType;
    private boolean isBtr;
    private boolean isActive;
}