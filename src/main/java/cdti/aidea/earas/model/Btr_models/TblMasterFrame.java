package cdti.aidea.earas.model.Btr_models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_master_frame")
public class TblMasterFrame {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;
    @Column(name = "schedule_id")
    private Integer scheduleId;

    @Column(name = "zone_id", nullable = false, length = 255)
    private Integer zoneId;

    @Column(name = "season_id", nullable = false)
    private Integer seasonId;

    @Column(name = "cluster_type", nullable = false)
    private String cluster_type;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;
}
