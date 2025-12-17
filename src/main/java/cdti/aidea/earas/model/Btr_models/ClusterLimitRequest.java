package cdti.aidea.earas.model.Btr_models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterLimitRequest {

  private Long id; // For update
  private BigDecimal clusterMin;
  private BigDecimal clusterMax;
  private BigDecimal tsoLimit;
  private UUID addedBy;
  private String remarks;
  private LocalDate agriStartYear;
  private LocalDate agriEndYear;
  private Boolean inActive;
}
