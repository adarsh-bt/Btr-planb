package cdti.aidea.earas.contract.RequestsDTOs;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class UpdateClusterPlotRequest {

    @NotNull(message = "Enumerated area cannot be null")
    private Double enumeratedArea;

    @NotNull(message = "User ID cannot be null")
    private UUID userId;
}
