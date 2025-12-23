package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterApprovalResponseDTO {

    private Long approvalLogId;
    private Boolean approved;
    private UUID approverId;
    private LocalDateTime approvedDate;
    private String status;
}
