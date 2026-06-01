package cdti.aidea.earas.contract.Projection;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ClusterSummaryFastProjection {

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

    private Double totCent;   // only here
}