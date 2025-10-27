package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationResponse {
  private Long id;
  private Integer resvno;
  private String resbdno;
  private Double totalcent;
  private String message;
  private Double remainingArea;
  private List<String> availableSubdivisions; // New field for subdivision options
}
