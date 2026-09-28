package cdti.aidea.earas.contract.FormEntryDto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class ManualCropReplaceClusterRequest {

    @NotNull(message = "userId is required")
    private UUID userId;

    @NotNull(message = "zoneId is required")
    private Long zoneId;

    @NotNull(message = "clusterId is required")
    private Long clusterId;

    @NotNull(message = "nextClusterId is required")
    private Long nextClusterId;

    @NotNull(message = "cropLandType is required")
    private String cropLandType;

    @NotNull(message = "cropId is required")
    private Long cropId;
}