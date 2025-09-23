package cdti.aidea.earas.contract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationErrorResponse {
  private Integer resvno;
  private String resbdno;
  private String message;
}
