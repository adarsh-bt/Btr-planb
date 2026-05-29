package cdti.aidea.earas.contract.FormEntryDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FetchAvailableCceCropsResponse {
  private Long cropId;
  private String cropName;
  private UUID cceAvailablePlotId;
  private Boolean isActive;
}
