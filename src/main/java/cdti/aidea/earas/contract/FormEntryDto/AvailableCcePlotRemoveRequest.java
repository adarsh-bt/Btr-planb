package cdti.aidea.earas.contract.FormEntryDto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class AvailableCcePlotRemoveRequest {
    private UUID cceAvailablePlotId;
    private String remarks;
    private UUID addedBy;
}
