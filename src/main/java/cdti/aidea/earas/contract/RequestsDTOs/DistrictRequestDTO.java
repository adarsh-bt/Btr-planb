package cdti.aidea.earas.contract.RequestsDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistrictRequestDTO {
    private Integer dist_id;
    private String dist_name_en;
    private String dist_name_mal;
    private boolean is_active;
    private Integer dist_lsg_code;
    private String dist_code;
    private String census_code_2011;
    private String census_code_2001;
    private Integer des_dist_code;

    private UUID addedBy;

}