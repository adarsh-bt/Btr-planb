package cdti.aidea.earas.contract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueVillageDto {
    private Integer revenueVillageId;
    private String revenueVillageName;
    private List<String> blockCodes;
    private Integer lsgCode;
}
