package cdti.aidea.earas.contract.RequestsDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZoneCreateRequest {
    private Integer zoneId;
    private Integer zoneCode;
    private String zoneNameEn;
    private String zoneNameMal;
    private Integer desTalukId;
    private Integer desDistId;
    private Boolean isActive;
    private String zoneUser;
    private Integer distId;
    private Integer btrTypeId;
    private UUID userId;
}
