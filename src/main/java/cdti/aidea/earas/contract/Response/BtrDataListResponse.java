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
//  private String resbdno;
  private String resbdno;
  private String lbtype;
  private String lbname;

  private String ltype;
  private String owner_name;
  private String address;
  private Integer tp_no;
  private Integer tp_subdivion_no;
  private Integer house_no;
  private Integer main_no;
  private String sub_main_no;
  private String totalCent;
  //    private Double nhect;
  //    private Double nare;
  //    private Double nsqm;
}
