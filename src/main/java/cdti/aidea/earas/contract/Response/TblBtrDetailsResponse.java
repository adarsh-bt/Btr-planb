package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TblBtrDetailsResponse {
    private Integer resvno;
    private String resbdno;
    private double totCent;
    private String address;
    private Integer wardno;
    private Integer houseno;
    private Integer oldsvno;
    private String oldsubno;
    private String ownername;
    private Integer tpno;
    private Integer tbsubdivisionno;
    private Long btrtype;
    private Long clutserno;
}
