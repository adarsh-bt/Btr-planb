package cdti.aidea.earas.contract.RequestsDTOs;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyPlotRejectRequest {
  private UUID userid;
  private String reason;
  private Long zone_id;
  private String reason_for_cluster;
}
