package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class BtrMainResponse<T> {

  private String status;
  private String message;
  private T data;
  private long totalCount;
  private double totalArea;
  private double totalWetArea;
  private double totalDryArea;
}
