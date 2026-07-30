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
    private String bcode;
    private double totCent;
    private String address;
    private Integer wardno;
    private String houseno;
    private Integer oldsvno;
    private String oldsubno;
    private String ownername;
    private Integer tpno;
    private String tbsubdivisionno;
    private Long btrtype;
    private String clutserno;
    private String lbCode;
    private String landType;

    private Integer villageId;
    private String villageNameEn;

    private Integer localbodyId;
    private String localbodyNameEn;

    private Integer districtId;
    private String districtName;

    private Integer talukId;
    private String talukName;

    private Integer blockId;
    private String blockName;

    private String zoneName;
}
