package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BtrDataResponse {
    private Long id;
    private Integer dcode;
    private Integer tcode;
    private Integer vcode;
    private String bcode;
    private Integer resvno;
    private String resbdno;
    private String lbtype;
    private String lbcode;
    private String govpriv;
    private String ltype;
    private String landuse;
    private Double nhect;
    private Double nare;
    private Double nsqm;
    private Double east;
    private Double west;
    private Double north;
    private Double south;
    private Integer lsgcode;
    private Double totCent;

    // Additional fields from joins
    private String villageName;
    private String localBodyName;
    private String surveyNumber; // Combined resvno/resbdno
    private Integer zoneId;
}