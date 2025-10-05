package cdti.aidea.earas.contract.Response;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
  List<String> cceCrops;
}
