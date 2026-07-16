package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterIdNumberResponse {

    private Long clusterId;
    private Integer clusterNumber;
    private String landType;
}
