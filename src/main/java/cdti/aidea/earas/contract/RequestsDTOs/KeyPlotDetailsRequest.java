package cdti.aidea.earas.contract.RequestsDTOs;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class KeyPlotDetailsRequest {

  @NotNull(message = "Cluster ID cannot be null")
  private UUID kpId;

  private String name;
  private String address;
  private String mobileNumber;
  private String geocoordinate;

  @NotNull(message = "Updater ID cannot be null")
  private UUID updatedBy;
}
