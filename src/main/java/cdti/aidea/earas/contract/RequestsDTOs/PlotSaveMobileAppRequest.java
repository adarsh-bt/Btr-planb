package cdti.aidea.earas.contract.RequestsDTOs;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlotSaveMobileAppRequest {
//    private Long id;
//    private Long plot_id;
    private UUID keyplotId;
    private Integer zoneid;
    private Integer village;
    private String bcode;
    private String clusterlabel;
    private Integer svNo; // resvno
    private String subNo; // resbdno
    private Double actual;
    private Double area;
   private UUID userId;
}
