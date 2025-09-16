package cdti.aidea.earas.contract.FormEntryDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CceCropDetailsResponse {
    private Long cropId;
    private Integer noOfCce;
    private String frameName;
    private String agriStartYear;
    private String agriEndYear;
}