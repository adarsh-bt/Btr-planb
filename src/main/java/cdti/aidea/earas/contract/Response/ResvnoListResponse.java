package cdti.aidea.earas.contract.Response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResvnoListResponse {
    private UUID keyPlotId;
    private String lbcode;
    private List<Integer> resvnos;
}
