package cdti.aidea.earas.contract.Response;

import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class KeyplotsLimitLogResponse {
  private Long id;
  private Long keyplotsLimit;
  private Boolean isEdited;
  private Boolean isActive;
  private UUID addedBy;
  private UUID editPermitter;
  private String remarks;
  private LocalDate agriStartYear;
  private LocalDate agriEndYear;
}
