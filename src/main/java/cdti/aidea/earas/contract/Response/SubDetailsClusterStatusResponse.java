package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubDetailsClusterStatusResponse {

    private Long id;

    private Long dryCompleted ;

    private Long dryOngoing ;

    private Long dryNotStarted ;

    private Long dryUnderView ;

    private Long wetCompleted ;

    private Long wetOngoing ;

    private Long wetNotStarted ;

    private Long wetUnderView ;
}
