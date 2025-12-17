package cdti.aidea.earas.contract.Response;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResbdnoListReponse {
  private UUID kpId;
  private String lbcode;
  private Integer resvno;
  private String villageName; // ⬅️ New field
  private String blockCode; // ⬅️ New field
  private List<ResbdnoAreaResponse> resbdnoDetails;
  private String statusMessage;
}
