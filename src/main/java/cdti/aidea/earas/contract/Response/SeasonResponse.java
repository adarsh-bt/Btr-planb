package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SeasonResponse {
    private Long id;
    private String seasonName;
    private LocalDate defaultStart;
    private LocalDate defaultEnd;
    private LocalDate extendDate;
}
