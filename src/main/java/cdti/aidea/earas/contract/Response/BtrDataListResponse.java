package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BtrDataListResponse {

  private Long id;
  //    private Integer vcode;
  private String villageName;
  //    private Integer bcode;
  private String bcode;
  private Integer resvno;
  private Integer resbdno;
  //    private String resbdno;
  private String lbtype;
  private String lbname;

  private String ltype;
  private String totalCent;
  //    private Double nhect;
  //    private Double nare;
  //    private Double nsqm;
}
