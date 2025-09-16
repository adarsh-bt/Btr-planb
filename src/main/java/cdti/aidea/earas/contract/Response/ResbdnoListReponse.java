package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResbdnoListReponse {
    private UUID kpId;
    private String lbcode;
    private Integer resvno;
    private String villageName;   // ⬅️ New field
    private String blockCode;     // ⬅️ New field
    private List<ResbdnoAreaResponse> resbdnoDetails;
    private String statusMessage;
}
