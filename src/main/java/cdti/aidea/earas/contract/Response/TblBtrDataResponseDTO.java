package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TblBtrDataResponseDTO {
    private Long id;
    private String bcode;
    private Integer resvno;
    private String resbdno;
    private String lbcode;
    private String ltype;
    private Integer lsgcode;
    private Double totCent;
    private String landuse;
    private Double nhect;
    private Double nare;
    private Double nsqm;
}
