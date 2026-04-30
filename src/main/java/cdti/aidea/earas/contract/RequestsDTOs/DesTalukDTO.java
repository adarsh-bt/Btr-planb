package cdti.aidea.earas.contract.RequestsDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DesTalukDTO {
    private Integer desTalukId;
    private String desTalukNameEn;
    private String desTalukNameMal;
    private Integer distId;
    private Boolean isActive;
    private UUID userId;

}