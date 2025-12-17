package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResbdnoAreaResponse {
  private Integer resvno;
  private String resbdno;
  private Double area;
  private Double balance;
  private Long plotId;
}
