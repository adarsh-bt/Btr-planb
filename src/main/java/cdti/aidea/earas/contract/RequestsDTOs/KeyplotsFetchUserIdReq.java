package cdti.aidea.earas.contract.RequestsDTOs;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyplotsFetchUserIdReq {
    @NotNull(message = "userId is required")
    private UUID userId;
    private Long zone_id;
}
