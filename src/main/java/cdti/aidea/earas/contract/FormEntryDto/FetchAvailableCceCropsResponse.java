package cdti.aidea.earas.contract.FormEntryDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FetchAvailableCceCropsResponse {
    private Long cropId;
    private String cropName;
}