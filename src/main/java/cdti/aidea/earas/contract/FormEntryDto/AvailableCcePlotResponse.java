package cdti.aidea.earas.contract.FormEntryDto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvailableCcePlotResponse {

    private UUID cceAvailablePlotId;
    private UUID plotId;
    private Long clusterId;
    private Long zoneId;
    private Integer cropId;
    private String cropName;
    private String cceSourceType;
    private UUID addedBy;
    private LocalDateTime createdAt;
    private LocalDate agriStartYear;
    private LocalDate agriEndYear;
    private Boolean isActive;
}