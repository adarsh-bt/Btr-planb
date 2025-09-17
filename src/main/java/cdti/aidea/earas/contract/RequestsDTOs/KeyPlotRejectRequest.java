package cdti.aidea.earas.contract.RequestsDTOs;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyPlotRejectRequest {
    private UUID userid;
    private String reason;
    private Long zone_id;
    private String reason_for_cluster;
}
