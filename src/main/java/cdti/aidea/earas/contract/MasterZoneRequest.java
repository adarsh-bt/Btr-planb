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
public class MasterZoneRequest {
    private Integer zoneId;
    private Integer zoneCode;
    private String zoneNameEn;
    private String zoneNameMal;
    private Integer desTalukId;
    private Integer desDistId;
    private Integer distId;
    private Integer btrTypeId;
    private String zoneUser;
    private Boolean isActive;
    private UUID userId;
}
