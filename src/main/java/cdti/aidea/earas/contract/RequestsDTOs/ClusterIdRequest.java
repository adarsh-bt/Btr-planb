package cdti.aidea.earas.contract.RequestsDTOs;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterIdRequest {
  @NotNull(message = "clusterId is required")
  private Long clusterId;
}
