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
public class TblMasterVillageBlockRequest {
    private Integer villageBlockId;
    private String blockCode;
    private Integer villageId;
    private Boolean isActive;
    private UUID userId;
}