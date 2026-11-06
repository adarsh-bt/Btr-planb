package cdti.aidea.earas.contract.FormEntryDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NucDetailsResponse {
  private UUID nucId;
  private Long clusterId;
  private String clusterLabel;
  private UUID userId;
  private Double nucArea;
  private Double ffsArea;
  private Double cosArea;
  private Long seasonId;
  private String remark;
}
