package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormClusterDetailsResponse {
    private Long clusterId;
    private Integer clusterNo;
    private String localBodyName;
}
