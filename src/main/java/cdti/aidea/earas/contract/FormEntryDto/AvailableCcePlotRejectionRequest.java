package cdti.aidea.earas.contract.FormEntryDto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class AvailableCcePlotRejectionRequest {
  private UUID oldPlotId;
  private Long oldClusterId;
  private UUID newPlotId;
  private Long newClusterId;
  private UUID userId;
}
