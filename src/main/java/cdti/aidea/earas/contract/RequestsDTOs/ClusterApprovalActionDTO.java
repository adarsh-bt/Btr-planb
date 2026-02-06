package cdti.aidea.earas.contract.RequestsDTOs;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterApprovalActionDTO {

    @NotNull
    private Long approvalLogId;

    @NotNull
    private Boolean approve;

    private Boolean is_Reject;

    @NotNull
    private UUID approver_id;
    private String remarks;
    private Boolean is_edit;
}

