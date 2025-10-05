package cdti.aidea.earas.contract.FormEntryDto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CropReplaceClusterResponse {
  private Long nextClusterId;
  private UUID plotId;
  private String message;
}
