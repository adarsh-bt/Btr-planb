package cdti.aidea.earas.contract.RequestsDTOs;

import cdti.aidea.earas.model.Btr_models.EditRequestStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class ClusterEditRequestDTO {
    private Long clusterId;
    private Integer zoneId;
    private String remarks;
    private UUID requestedBy;
    private UUID approvedBy;
    private double totalArea;
    private String status;
}
