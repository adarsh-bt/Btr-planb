package cdti.aidea.earas.contract.RequestsDTOs;


import cdti.aidea.earas.contract.Response.SidePlotDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveClusterRequestDTO {
    private UUID userId;
    private UUID keyplotId;
    private Integer clusterNo;
    private List<SidePlotDTO> sidePlots;
}
