package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterTourResponse {
    private Integer clusterNo;
    private String zoneName;
    private String landType;
    private String localbody;
    private String block;

}