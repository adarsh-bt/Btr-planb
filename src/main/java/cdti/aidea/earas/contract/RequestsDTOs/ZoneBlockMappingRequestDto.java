package cdti.aidea.earas.contract.RequestsDTOs;

import lombok.Data;

import java.util.UUID;

@Data
public class ZoneBlockMappingRequestDto {

    private Integer zoneId;
    private Integer typeCode;
    private Integer referenceId;
    private UUID userId;
}