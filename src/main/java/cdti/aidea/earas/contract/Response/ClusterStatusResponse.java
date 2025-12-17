package cdti.aidea.earas.contract.Response;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClusterStatusResponse {
  private Integer clusterNo;
  private UUID keyplotId;
  private boolean isCce;
  private String village;
  private Integer villageId;
  private String localbody;
  private String lbcode;
  private String blockcode;
  private String survyno;
  private Double area;
  private Long clusterId;
  private String clusterType;
  private String status;
  private List<SeasonStatusDto> seasons;
  List<String> cceCrops;
}
