package cdti.aidea.earas.contract.FormEntryDto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClusterDetailsResponse {

  private List<NucDetailsResponse> nucDetails;
  private List<CropInfoResponse> cropDetails;
  private List<ClusterLabelCountResponse> landUtilizationDetails;
  private List<ClusterLabelCountResponse> irrigationDetails;
  private List<AvailableCcePlotDetailsResponse> availablePlotDetails;
}
