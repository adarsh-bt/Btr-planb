package cdti.aidea.earas.contract.RequestsDTOs;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZoneAssignedRequset {

  private Integer zoneId;
  private UUID user_id;
  private UUID assigner_id;
  private Boolean is_active;
}
