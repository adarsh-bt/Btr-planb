package cdti.aidea.earas.contract.Projection;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ClusterSummaryProjection {

    private Long clusterId;
    private Integer clusterNumber;

    private UUID keyplotId;
    private String landType;

    private String villageName;
    private Integer vcode;

    private String localBodyName;
    private String localBodyType;
    private String lbcode;

    private String bcode;
    private String surveyNo;

    private String status;
}