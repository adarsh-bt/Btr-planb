package cdti.aidea.earas.contract;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusterCompletedProgressResponse {
    private Long totalClusterCompleted;

    private Map<String, ClusterCompletedProgressSubDetails> allSubDetails;
}
