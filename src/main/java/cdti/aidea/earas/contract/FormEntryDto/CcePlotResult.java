package cdti.aidea.earas.contract.FormEntryDto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CcePlotResult {
  private List<AvailableCcePlotResponse> plots;
  private boolean fallbackUsed;
}
