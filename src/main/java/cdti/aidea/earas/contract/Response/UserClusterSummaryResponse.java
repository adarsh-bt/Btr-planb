package cdti.aidea.earas.contract.Response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserClusterSummaryResponse {
    private String message;
    private int completed;
    private int ongoing;
    private int notStarted;
    private int underreview;
    private String cceMessage;
    private List<ClusterStatusResponse> payload;
}
