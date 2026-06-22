package cdti.aidea.earas.contract.RequestsDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkAllocationApproveDTO {

        private Long approvalLogId;
        private Boolean approve;
        private UUID approver_id;
        private String remarks;
        private Boolean is_edit;

}
