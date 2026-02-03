package cdti.aidea.earas.contract.RequestsDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEnumeratedKeyAreaDTO {

    private UUID kpId;
    private Double enumeratedArea;
    private String remark; // optional
}
