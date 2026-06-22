package cdti.aidea.earas.contract.RequestsDTOs;

import cdti.aidea.earas.contract.Response.SidePlotDTO;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveClusterRequestDTO {
  private UUID userId;
  private UUID keyplotId;
  private Long zoneId;
  private Integer clusterNo;
  private String status;
  private String remarks;
  private String agriYear;
  private List<SidePlotDTO> sidePlots;
}
