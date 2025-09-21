package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VillageResponseDTO {
    private Integer villageId;
    private String villageNameEn;
    private Integer localBodyId;
    private String localBodyNameEn;
    private Integer zoneId;
    private String zoneNameEn;
}
