package cdti.aidea.earas.contract.FormEntryDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class ClusterIdsRequest {
    private List<Long> clusterIds;
}
