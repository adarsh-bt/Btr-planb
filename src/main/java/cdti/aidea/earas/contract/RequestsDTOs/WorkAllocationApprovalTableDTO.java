package cdti.aidea.earas.contract.RequestsDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkAllocationApprovalTableDTO {
    private Long approvalId;
    private String status;

    private Integer zoneId;
    private String zoneName;

    private Integer talukId;
    private String talukName;

    private Integer districtId;
    private String districtName;

    private UUID requestedBy;
    private UUID approvedBy;
    private String remarks;


    private LocalDateTime createdAt;
    private LocalDateTime approvedDate;
}