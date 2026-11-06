package cdti.aidea.earas.contract.FormEntryDto;

import cdti.aidea.earas.contract.Response.TblBtrDetailsResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvailableCcePlotDetailsResponse {
  private UUID cceAvailablePlotId;
  private UUID plotId;
  private Long clusterId;
  private Long zoneId;
  private Long btrId;
  private Long cropId;
  private Long seasonId;
  private String seasonName;
  private String cropName;
  private Double cultivatedArea;
  private Boolean isIrrigated;
  private String cceSourceType;
  private LocalDate expectedHarvestDate;
  private String farmerName;
  private String farmerAddress;
  private String farmerPhoneNumber;
  private UUID addedBy;
  private String remarks;
  private String landType;
  private LocalDate agriStartYear;
  private LocalDate agriEndYear;
  private Boolean isSelected;
  private String geoCoordinate;
  private String clusterLabel;
  private Integer totalNoCultivators;
  private Integer randomNoCultivatorSelected;
  private Integer totalNoPatches;
  private Integer randomNoPatchSelected;

  private Boolean isClusterLabelLocked;

  private KeyPlotClusterDTO clusters;
  private TblBtrDetailsResponse btrDetailsResponse;

  private Integer totalNumberOfBearing;
  private Integer totalNumberOfYoung;
  private Long mappingId;
  private Double areaYoung;
  private Double areaBearing;
  private Integer clusterNo;
  private Double patchArea;
  private List<FetchAllSurveyResponse> surveyResponses;
}
