package cdti.aidea.earas.contract.RequestsDTOs;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropAssignmentTrailSaveDto {

  @NotNull(message = "Crop ID is required")
  private Long cropId;

  private Long clusterId;

  private UUID keyplotId;

  private Long zoneId;

  private String landType;

  private Boolean isRejected = false;

  private String rejectionReason;

  private Boolean isLimitExceeded = false;

  private Boolean isCurrentAssignment = true;

  private UUID rejectedBy;

  private LocalDateTime rejectedAt;

  private LocalDateTime assignedOn;
}
