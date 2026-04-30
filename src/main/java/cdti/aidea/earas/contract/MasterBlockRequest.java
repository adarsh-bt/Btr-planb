package cdti.aidea.earas.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MasterBlockRequest {
    private Integer blockId;
    private String blockCode;
    private String blockName;
    private Integer district;
    private boolean isValid;
    private Integer lsgCode;
    private UUID userId;
}