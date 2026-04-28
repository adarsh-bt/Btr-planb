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
public class TblMasterVillageRequest {
    private Integer villageId;
    private String villageNameEn;
    private String villageNameMal;
    private Integer revTalukId;
    private Boolean isActive;
    private String villageCodeApi;
    private Integer lsgCode;
    private String censusCode2001;
    private String censusCode2011;
    private UUID userId;
}
