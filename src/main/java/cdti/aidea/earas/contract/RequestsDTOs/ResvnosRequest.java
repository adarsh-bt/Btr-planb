package cdti.aidea.earas.contract.RequestsDTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResvnosRequest {

    @NotNull(message = "kp_id is required")
    @JsonProperty("kp_id")          // JSON → field mapping
    private UUID kpId;

    @NotNull(message = "village_id is required")
    @JsonProperty("village_id")
    private Integer villageId;

    @NotNull(message = "block_code is required")
    @JsonProperty("block_code")
    private String blockCode;
}

