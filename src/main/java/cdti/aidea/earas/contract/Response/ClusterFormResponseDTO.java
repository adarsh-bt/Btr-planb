package cdti.aidea.earas.contract.Response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ClusterFormResponseDTO {
    private Long clusterId;
    private Long plotId;
    private String plotLabel;
}
