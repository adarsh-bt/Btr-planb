package cdti.aidea.earas.contract.FormEntryDto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
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
  private Long btrId;
  private String lbCode;
  private Long cropId;
  private String cceSourceType;
  private UUID addedBy;
  private String landType;
  @NotNull(message = "Agricultural year cannot be null")
  private String agriYear;
  private Boolean isActive;
  private Boolean isSelected;
}
