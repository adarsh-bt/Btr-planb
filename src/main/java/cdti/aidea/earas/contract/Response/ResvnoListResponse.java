package cdti.aidea.earas.contract.Response;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResvnoListResponse {
  private UUID keyPlotId;
  private String lbcode;
  private List<Integer> resvnos;
}
