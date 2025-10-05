package cdti.aidea.earas.contract.FormEntryDto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CceAssignmentRequest {
  private UUID plotId;
  private Long clusterId;
  private Integer zoneId;
  //    private Long btrId;
  private Long cropId;
  private String cceSourceType;
  private UUID addedBy;
  private String agriStartYear;
  private String agriEndYear;
  private Boolean isActive;
  private Boolean isSelected;
}
