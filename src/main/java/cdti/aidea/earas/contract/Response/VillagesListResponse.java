package cdti.aidea.earas.contract.Response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VillagesListResponse {
    String village;
    Long villageId;
    private List<BlockCodeResponse> blocks;
}
