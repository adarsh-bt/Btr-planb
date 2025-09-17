package cdti.aidea.earas.contract.RequestsDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZoneAssignedRequset {

    private Integer zoneId;
    private UUID user_id;
    private UUID assigner_id;
}
