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
    private Integer old_survey_number;
    private String old_subdivision_number;
    private Integer ward_number;
    private  String cl_no;
    private Integer tp_no;
    private Integer tb_subdivision_no;
    private String ownername;
    private String address;
    private Integer houseno;

}
