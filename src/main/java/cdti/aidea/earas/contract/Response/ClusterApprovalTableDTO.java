package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterApprovalTableDTO {

    private Long approvalId;

    private Long clusterId;
    private Integer clusterNo;
    private String clusterType;

    private Integer zoneId;
    private String zoneName;

    private Integer talukId;
    private String talukName;

    private Integer districtId;
    private String districtName;

    private BigDecimal totalArea;

    private Boolean approved;

    private UUID requestedBy;
    private UUID approvedBy;
    private String resRemarks;
    private String reqRemarks;

    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
}
