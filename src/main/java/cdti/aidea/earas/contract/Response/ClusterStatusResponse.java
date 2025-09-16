package cdti.aidea.earas.contract.Response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterStatusResponse {
    private Integer clusterNo;
    private UUID keyplotId;
    private boolean isCce;
    private String village;
    private String localbody;
    private String blockcode;
    private String survyno;
    private Double area;
    private Long clusterId;
    private String clusterType;
    private String status;
}
