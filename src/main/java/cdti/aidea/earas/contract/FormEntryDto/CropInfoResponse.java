package cdti.aidea.earas.contract.FormEntryDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CropInfoResponse {
  private UUID id;
  private Long cropId; // mappingId
  private String cropNameEn;
  private String cropGrowthStage;
  private Long seasonId;
  private String season;
  private Long cropTypeId;
  private String cropType;
  private Long unitId;
  private Double cropArea;
  private Boolean isIrrigated;
  private Double centPerTree;
  private String unit;
}
