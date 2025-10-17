package cdti.aidea.earas.contract.RequestsDTOs;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyPlotRejectRequest {
  private UUID userId;
  private String reason;
  private Long zoneId;
  private String reasonForCluster;
}
