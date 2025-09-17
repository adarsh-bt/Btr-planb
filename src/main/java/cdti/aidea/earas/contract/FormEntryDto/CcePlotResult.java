package cdti.aidea.earas.contract.FormEntryDto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CcePlotResult {
    private List<AvailableCcePlotResponse> plots;
    private boolean fallbackUsed;
}
