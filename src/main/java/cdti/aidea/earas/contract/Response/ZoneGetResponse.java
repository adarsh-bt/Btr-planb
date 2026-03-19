package cdti.aidea.earas.contract.Response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZoneGetResponse {
    private Integer zoneId;
    private Integer zoneCode;
    private String zoneNameEn;
    private String zoneNameMal;
    private String btrType;

    private Integer desTalukId;
    private Integer desDistId;

    private String talukName;
    private String districtName;

//    private UUID assignedUserId;
}
