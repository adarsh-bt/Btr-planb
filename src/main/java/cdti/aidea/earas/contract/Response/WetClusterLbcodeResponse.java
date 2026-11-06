package cdti.aidea.earas.contract.Response;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WetClusterLbcodeResponse {
    private String lbcode;
}
