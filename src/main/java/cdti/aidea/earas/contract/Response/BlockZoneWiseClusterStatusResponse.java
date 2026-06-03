package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlockZoneWiseClusterStatusResponse {
    private Long zoneId;

    private Integer blockId;

    private String blockName;

    private Long wetCompleted;

    private Long wetOngoing;

    private Long wetNotStarted;

    private Long wetUnderView;

    private Long dryCompleted;

    private Long dryOngoing;

    private Long dryNotStarted;

    private Long dryUnderView;
}
