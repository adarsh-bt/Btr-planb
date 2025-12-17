package cdti.aidea.earas.model.Btr_models;

import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_work_allocation")
public class TblWorkAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many records can belong to one zone
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private TblMasterZone zone;

    @Column(length = 100)
    private String lbcode;

    @Column(precision = 18, scale = 2)
    private BigDecimal villageWetArea;

    @Column(precision = 18, scale = 2)
    private BigDecimal villageDryArea;

    @Column(precision = 18, scale = 2)
    private BigDecimal villageTotalArea;

    @Column(precision = 18, scale = 2,name = "forest_area_a")
    private BigDecimal forestAreaA;

    @Column(precision = 18, scale = 2,name = "forest_area_b")
    private BigDecimal forestAreaB;

    @Column(precision = 18, scale = 2,name = "forest_area_c")
    private BigDecimal forestAreaC;

    @Column(precision = 18, scale = 2)
    private BigDecimal areaUnderPlant;

    @Column(precision = 18, scale = 2)
    private BigDecimal forestExcludeUnclutivate;

    @Column(precision = 18, scale = 2)
    private BigDecimal forestExcludeNotUnclutivate;

    @Column(precision = 18, scale = 2)
    private BigDecimal kayalExcludeArea;

    @Column(precision = 18, scale = 2, name = "other_exclude_f_wet")
    private BigDecimal otherExcludeFWet;

    @Column(precision = 18, scale = 2, name = "other_excluded_f_dry")
    private BigDecimal otherExcludedFDry;

    @Column(precision = 18, scale = 2, name = "other_exclude_f_total")
    private BigDecimal otherExcludeFTotal;

    @Column(precision = 18, scale = 2)
    private BigDecimal noOfPlotsWet;

    @Column(precision = 18, scale = 2)
    private BigDecimal noOfPlotsDry;

    @Column(precision = 18, scale = 2)
    private BigDecimal noOfPlotsTotal;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalAreaWet;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalAreaDry;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalAreaForEstimation;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    private UUID userId;

    private LocalDate created;
    private LocalDate updated;

    private Boolean isActive = true;

    private LocalDate agriStart;
    private LocalDate agriEnd;
}
