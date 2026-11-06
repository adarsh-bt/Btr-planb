package cdti.aidea.earas.contract.RequestsDTOs;
import cdti.aidea.earas.model.Btr_models.KeyplotDeletionRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyplotDeletionRequestResponseDTO {

    private UUID requestId;
    private String requestNumber;
    private UUID keyplotId;
    private Long zoneId;
    private Long btrId;
    private String agriYear;
    private String reason;
    private String status;
    private UUID requestedBy;
    private OffsetDateTime requestedAt;
    private UUID reviewedBy;
    private OffsetDateTime reviewedAt;
    private String adminRemark;
    private Map<String, Object> impactSnapshot;
    private OffsetDateTime executionStartedAt;
    private OffsetDateTime completedAt;
    private String failureReason;
    private UUID correlationId;
    private Long version;

    /**
     * Maps a KeyplotDeletionRequest entity to KeyplotDeletionRequestResponseDTO.
     */
    public static KeyplotDeletionRequestResponseDTO fromEntity(KeyplotDeletionRequest entity) {
        if (entity == null) {
            return null;
        }

        return KeyplotDeletionRequestResponseDTO.builder()
                .requestId(entity.getRequestId())
                .requestNumber(entity.getRequestNumber())
                .keyplotId(entity.getKeyplotId())
                .zoneId(entity.getZoneId())
                .btrId(entity.getBtrId())
                .agriYear(entity.getAgriYear())
                .reason(entity.getReason())
                .status(entity.getStatus())
                .requestedBy(entity.getRequestedBy())
                .requestedAt(entity.getRequestedAt())
                .reviewedBy(entity.getReviewedBy())
                .reviewedAt(entity.getReviewedAt())
                .adminRemark(entity.getAdminRemark())
                .impactSnapshot(entity.getImpactSnapshot())
                .executionStartedAt(entity.getExecutionStartedAt())
                .completedAt(entity.getCompletedAt())
                .failureReason(entity.getFailureReason())
                .correlationId(entity.getCorrelationId())
                .version(entity.getVersion())
                .build();
    }
}