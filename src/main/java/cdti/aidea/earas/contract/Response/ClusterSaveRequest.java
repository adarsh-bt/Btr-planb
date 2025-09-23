package cdti.aidea.earas.contract.Response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterSaveRequest {
  private String wardNumber;
  private String wetDry; // Not directly mapped to provided entities
  private String keyplotType; // Not directly mapped to provided entities
  private String reserveKeyplot;
  private List<SidePlotDTO> keyplots; // This is the array of side plots
  private KeyPlotDetailsResponse keyPlotDetailsResponse; // Contains the syNo to link to KeyPlots
}
