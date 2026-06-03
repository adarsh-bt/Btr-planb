package cdti.aidea.earas.contract.Response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.UUID;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeyPlotOwnerDetailsResponse {
  private UUID id;
  private String owner_name;
  private String address;
  private String phone_number;
  private Long cluster_id;
  private Long btrId;
  private Long btrTypeId;
  private String btrTypeName;
  private String plotno;
  private Integer wardNo;
  private String houseNo;
  private String CultivateName;
  private Double CultivateArea;
  private Integer TpNo;
  private String TpSubNo;
  private Integer OldSurvey;
  private String OldSubDivNo;
  private String geocoordinate;
  private Double area;
  private LocalDate selectedDate;
  private Integer distId;
  private Integer zoneId;
  private String zoneName;
}

