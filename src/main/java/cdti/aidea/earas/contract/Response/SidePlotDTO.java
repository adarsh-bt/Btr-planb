package cdti.aidea.earas.contract.Response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SidePlotDTO {
  private String label;
  private List<ClusterFormRowDTO> rows;
}
