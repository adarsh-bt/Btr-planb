package cdti.aidea.earas.contract.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class AdminZoneSeasonResponse {
    private Long id;
    private String seasonName;

    @JsonProperty("defaultStart")
    private LocalDate startDate;

    @JsonProperty("defaultEnd")
    private LocalDate endDate;

    @JsonProperty("extendDate")
    private LocalDate extendedDate;
}
