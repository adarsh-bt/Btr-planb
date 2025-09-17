package cdti.aidea.earas.contract.FormEntryDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyPlotClusterDTO {
    private Long clusterId;
    private String landType;
    private String localBodyName;
    private Integer clusterNumber;
}