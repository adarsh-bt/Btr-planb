package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterFormRowDTO {
    private Long id;
    private Long plot_id;
    private Double actual;
    private Integer svNo;    // resvno
    private String subNo;   // resbdno
    private Double area;
    private String bcode;
    private String village;

}