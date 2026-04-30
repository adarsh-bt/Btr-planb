package cdti.aidea.earas.contract.RequestsDTOs;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddZoneMappingRequest {

    private Long zoneId;
    private UUID userId;
    private Integer talukId;
    private Integer villageId;
    private List<String> blockCodes;


}
