package cdti.aidea.earas.contract.FormEntryDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExternalClusterStatusResponse {
    private Long seasonId;
    private Long clusterId;
    private String status;
}
