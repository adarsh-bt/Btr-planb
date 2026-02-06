package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class AdminZoneResponse {
    private Integer zoneId;
    private Integer dist_id;
    private String zoneName;
    private Integer zone_type_id;
    private String zone_type_name;

    private List<AdminZoneSeasonResponse> seasons;
}







