package cdti.aidea.earas.contract;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueVillageDto {
  private Integer revenueVillageId;
  private String revenueVillageName;
  private List<String> blockCodes;
  private Integer lsgCode;
}
