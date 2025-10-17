package cdti.aidea.earas.contract.Response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KeyPlotDetailsResponse {
  private UUID keyplotId;
  private Integer dcode;
  private Integer tcode;
  private Long clusterId;
  private Long btr_id;
  private String btr_type;
  private String kvillageName;
  private Integer kvillageId;
  private String villageBlock;
  private String panchayath;
  private String lbcode;
  private String status;
  private BigDecimal clusterMax;
  private BigDecimal clusterMin;
  private BigDecimal clusterMean;
  private String syNo;
  private double areaCents;
  private String landType;
  private List<SidePlotDTO> sidePlots;
}
