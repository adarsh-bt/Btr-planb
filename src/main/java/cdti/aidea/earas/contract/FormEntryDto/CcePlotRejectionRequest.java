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
  private String reson;
  private UUID userId;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AvailableCcePlotRemoveRequest {
        private UUID cceAvailablePlotId;
        private String remarks;
        private UUID addedBy;
    }
}
