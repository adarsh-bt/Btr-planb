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
  private String plotno;
  private String geocoordinate;
  private Double area;
  private LocalDate selectedDate;

}
