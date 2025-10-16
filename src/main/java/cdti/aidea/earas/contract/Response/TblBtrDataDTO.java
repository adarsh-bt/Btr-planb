package cdti.aidea.earas.contract.Response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TblBtrDataDTO {

  private Integer dcode;
  private Integer tcode;
  private Integer vcode;
  private String bcode;
  private String lbcode;
  private String ltype;
  private Integer resvno;
  private String resbdno;
  private Integer lsgcode;
  private Integer zoneId; // new field for zone
  private UUID user_id;
  private double totCent;
  private String address;
  private Integer houseno;
  private Integer mainno;
  private String subno;
  private Long btrtype;
  private String ownername;
  private Integer tpno;
  private Integer tbsubdivisionno;

}
