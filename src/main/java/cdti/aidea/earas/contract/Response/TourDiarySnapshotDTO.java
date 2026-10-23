package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourDiarySnapshotDTO {
    private Long id;
    private Long clusterId;
    private Integer clusterNo;
    private String landType;
    private Long zoneId;
    private Long talukId;
    private UUID userId;
    private LocalDate actionDate;
    private String status;
    private String entryType;
    private String reportEntryType;
    private String remark;
    private String adminRemark;
    private BigDecimal distance;
    private BigDecimal hours;
    private Long cropId;
    private Integer seasonNo;
    private LocalDate agriStartYear;
    private LocalDate agriEndYear;
    private LocalDateTime createdAt;


}