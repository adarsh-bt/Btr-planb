package cdti.aidea.earas.contract;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RevenueTalukDto {
  private Integer revenueTalukId;
  private String
      revenueTalukName; // currently English; switch based on lang if Malayalam field exists
}
