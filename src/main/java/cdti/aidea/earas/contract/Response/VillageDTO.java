package cdti.aidea.earas.contract.Response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.ArrayList;
import java.util.LinkedHashSet;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class VillageDTO {

    private Long villageMappingId;
    private Integer villageId;
    private String villageName;

    private List<BlockDTO> blocks = new ArrayList<>();

    public VillageDTO(Long villageMappingId, Integer villageId, String villageName) {
        this.villageMappingId = villageMappingId;
        this.villageId = villageId;
        this.villageName = villageName;
    }
}