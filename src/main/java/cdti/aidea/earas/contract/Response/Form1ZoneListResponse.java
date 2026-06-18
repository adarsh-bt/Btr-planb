package cdti.aidea.earas.contract.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Form1ZoneListResponse {
    private int zoneId;
    private Integer zoneCode;
    private String zoneNameEn;
    private String zoneNameMal;
    private String zoneType;
    private int desTalukId;
    private int desDistId;
    private String talukName;
    private String districtName;
}
