package cdti.aidea.earas.contract.Response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserClusterSummaryResponse {
  private String message;
  private int completed;
  private int ongoing;
  private int notStarted;
  private int underreview;
  private String cceMessage;
  private List<ClusterStatusResponse> payload;
}
