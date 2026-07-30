package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZoneDropdownResponse {

    private Integer zoneId;
    private String zoneNameEn;
    private String zoneType;
}