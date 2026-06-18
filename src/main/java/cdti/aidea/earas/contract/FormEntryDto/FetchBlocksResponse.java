package cdti.aidea.earas.contract.FormEntryDto;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FetchBlocksResponse {
    private Integer blockId;
    private String blockName;
    private Integer localBodyId;
    private String localBodyName;
    private Long zoneId;
    private String zoneName;
    private Long clusterId;
    private LocalDateTime createdAt;
    private String landType;
}
