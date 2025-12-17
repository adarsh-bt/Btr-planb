package cdti.aidea.earas.contract.RequestsDTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlotDetailsRequest {

  @NotNull(message = "kp_id is required")
  @JsonProperty("kp_id") // JSON → field mapping
  private UUID kpId;

  @NotNull(message = "resvno is required")
  @JsonProperty("resvno")
  private Integer resvno;

  @NotNull(message = "resbdno is required")
  @JsonProperty("resbdno")
  private Integer resbdno;
}
