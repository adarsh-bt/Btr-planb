package cdti.aidea.earas.model.Btr_models;

import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_zone_season_schedule")
public class TblZoneSeasonSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", referencedColumnName = "zone_id", nullable = false)
    private TblMasterZone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", referencedColumnName = "id", nullable = false)
    private TblSeasonMaster season;

//    @Column(name = "cluster_type", nullable = false, length = 50)
//    private String clusterType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "extended_date")
    private LocalDate extendedDate;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "uuid", columnDefinition = "UUID DEFAULT gen_random_uuid()", insertable = false, updatable = false)
    private UUID uuid;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "remark", length = 255)
    private String remark;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "frame_id", referencedColumnName = "frame_id")
    private TblMasterFrame frame; // This should point to your MasterFrame entity

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;

}

