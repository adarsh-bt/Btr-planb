package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterReportResponse {
    private Long totalCluster;

    private Long completed ;

    private Long ongoing ;

    private Long notStarted ;

    private Long underView ;

    private Map<String, DistrictClusterStatusResponse> allDistricts;
}
