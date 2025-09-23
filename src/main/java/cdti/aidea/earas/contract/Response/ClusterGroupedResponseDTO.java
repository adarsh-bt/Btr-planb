package cdti.aidea.earas.contract.Response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterGroupedResponseDTO {
  private Long clusterId;
  private List<ClusterLabelGroupDTO> labels;
}
