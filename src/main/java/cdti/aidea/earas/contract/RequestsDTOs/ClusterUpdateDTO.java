package cdti.aidea.earas.contract.RequestsDTOs;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterUpdateDTO {
    private Long clusterId;
    private Integer newClusterNumber;
}
