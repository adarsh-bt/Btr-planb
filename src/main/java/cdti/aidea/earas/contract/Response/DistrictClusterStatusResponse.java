package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistrictClusterStatusResponse {
    private Long completed ;

    private Long ongoing ;

    private Long notStarted ;

    private Long underView ;
}
