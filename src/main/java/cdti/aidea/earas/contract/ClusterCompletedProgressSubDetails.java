package cdti.aidea.earas.contract;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusterCompletedProgressSubDetails {
    private Long id;

    private Long wetCompleted;

    private Long dryCompleted;
}
