package cdti.aidea.earas.contract.Response;

import java.math.BigDecimal;
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
  private BigDecimal clusterMin;
  private BigDecimal clusterMax;
  private BigDecimal tsoClusterLimit;

  private String cceMessage;
  private List<ClusterStatusResponse> payload;
}
