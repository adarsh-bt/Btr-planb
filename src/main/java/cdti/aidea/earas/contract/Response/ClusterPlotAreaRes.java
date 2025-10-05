package cdti.aidea.earas.contract.Response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterPlotAreaRes {

  private UUID keyPlotId;
  private String lbcode;
  private Integer resvno;
  private String resbdno;
  private Double area;
  private String bcode;
  private Long plot_id;
}
