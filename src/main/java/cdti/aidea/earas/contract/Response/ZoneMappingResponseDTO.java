package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZoneMappingResponseDTO {
    private Integer zoneId;
    private String zoneName;
    private String zoneType;

    private List<TalukDTO> data;
}
