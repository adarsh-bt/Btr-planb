package cdti.aidea.earas.contract.Response;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TblZoneSeasonScheduleDTO {
    private Long scheduleId;
    private Integer zoneId;
    private Long seasonId;
    private String clusterType;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate extendedDate;
    private Integer year;
    private UUID uuid;
    private Boolean isActive;
    private String remark;
    private String zoneNameEn;
    private String seasonName;
    private Long frameId;
    private String frameName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
