package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistrictWetLbcodeSummaryResponse {
    private Long districtId;
    private String districtName;
    private Long lbcodeCount;
}
