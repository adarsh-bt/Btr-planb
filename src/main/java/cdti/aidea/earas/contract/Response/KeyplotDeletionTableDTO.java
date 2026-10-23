package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
import lombok.Builder;
import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KeyplotDeletionTableDTO {

    private UUID requestId;
    private String requestNumber;

    // Plot details
    private UUID keyplotId;
    private Integer clusterNumber;
    private Long btrId;
    private String landType;
    private String agriYear;

    // Zone & Geographic Hierarchy
    private Integer zoneId;
    private String zoneName;
    private Integer talukId;
    private String talukName;
    private Integer districtId;
    private String districtName;

    // Status & Reason
    private String status;
    private String reason;
    private String adminRemark;

    // User tracking
    private UUID requestedBy;
    private String requestedByName;
    private OffsetDateTime requestedAt;

    private UUID reviewedBy;
    private String reviewedByName;
    private OffsetDateTime reviewedAt;

    // Snapshot summary
    private Map<String, Object> impactSnapshot;

}