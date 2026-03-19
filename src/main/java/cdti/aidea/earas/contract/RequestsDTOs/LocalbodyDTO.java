package cdti.aidea.earas.contract.RequestsDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocalbodyDTO {

    private Integer mappingId;
    private Integer localbodyId;
    private String localbodyName;
    private Integer zoneId;
    private UUID userId;

}