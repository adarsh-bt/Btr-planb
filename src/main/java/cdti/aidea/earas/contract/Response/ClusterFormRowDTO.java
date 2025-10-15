package cdti.aidea.earas.contract.Response;

import cdti.aidea.earas.model.Btr_models.TblNonBtr;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClusterFormRowDTO {
  private Long id;
  private Long plot_id;
  private Double actual;
  private Integer svNo; // resvno
  private String subNo; // resbdno

  private Double area;
  private String bcode;
  private String village;

  private String ownername;
  private String address;
  private Integer tpno;
  private Integer tbsubdivisionno;
  private Integer houseno;
  private Integer mainno;
  private String subno;
  private Long btrtype;
}
