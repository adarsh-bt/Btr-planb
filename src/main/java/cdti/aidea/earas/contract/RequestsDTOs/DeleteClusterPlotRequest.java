package cdti.aidea.earas.contract.RequestsDTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteClusterPlotRequest {
  @NotNull(message = "cluster_plot_id is required")
  @JsonProperty("cluster_plot_id") // Ensures JSON key maps to this field
  private Long clusterPlotId;
}
