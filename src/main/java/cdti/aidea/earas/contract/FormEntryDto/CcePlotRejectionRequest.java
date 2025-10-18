package cdti.aidea.earas.contract.FormEntryDto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CcePlotRejectionRequest {
  private UUID oldPlotId;
  private Long oldClusterId;
  private String remarks;
  private UUID userId;
}
