package cdti.aidea.earas.contract.FormEntryDto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class AvailableCcePlotFetchRequest {
  @NotNull(message = "Zone id cannot be null")
  private Long zoneId;
}
