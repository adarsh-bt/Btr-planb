package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WetTalukLbcodeSummaryResponse {
    private Long talukId;
    private String talukName;
    private Long talukLbcodeCount;
}
