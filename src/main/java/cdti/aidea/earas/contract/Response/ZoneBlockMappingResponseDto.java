package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZoneBlockMappingResponseDto {

    private Long id;
    private Integer typeCode;
    private String type;
    private String name;
    private Boolean isActive;
}
