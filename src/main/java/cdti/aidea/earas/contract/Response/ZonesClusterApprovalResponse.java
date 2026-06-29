package cdti.aidea.earas.contract.Response;

import cdti.aidea.earas.config.Form1EditLogResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZonesClusterApprovalResponse {

    private Integer zoneId;
    private String zoneName;

    private Integer talukId;
    private String talukName;

    private Integer districtId;
    private String districtName;

    private Form1EditLogResponse form1EditLogResponse;
}
