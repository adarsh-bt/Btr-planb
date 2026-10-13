package cdti.aidea.earas.contract.RequestsDTOs;

import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TblWorkAllocationDTO {
    private Long id;
    private Integer zoneId;
    private String lbcode;

    @Digits(integer = 10, fraction = 2)
    private BigDecimal villageWetArea;
    @Digits(integer = 10, fraction = 2)
    private BigDecimal villageDryArea;
    @Digits(integer = 10, fraction = 2)
    private BigDecimal villageTotalArea;
//  Forest Area  Dry area
    @Digits(integer = 10, fraction = 2)
    private BigDecimal forestAreaA;
//  Forest Area  Wet Area
    @Digits(integer = 10, fraction = 2)
    private BigDecimal forestAreaB;
//    Plantation Area Wet
    @Digits(integer = 10, fraction = 2)
    private BigDecimal forestAreaC;
// Plantation Area Dry
    @Digits(integer = 10, fraction = 2)
    private BigDecimal areaUnderPlant;
//    Other area Dry
    @Digits(integer = 10, fraction = 2)
    private BigDecimal forestExcludeUnclutivate;
//    Oter area dry
    @Digits(integer = 10, fraction = 2)
    private BigDecimal forestExcludeNotUnclutivate;
//    Area of Water bodies Dry
    @Digits(integer = 10, fraction = 2)
    private BigDecimal kayalExcludeArea;
//    Area of Water Bodies Wet
@Digits(integer = 10, fraction = 2)
private BigDecimal otherExcludeFWet;
    @Digits(integer = 10, fraction = 2)
    private BigDecimal totalWetCluster;
    @Digits(integer = 10, fraction = 2)
    private BigDecimal totalDryCluster;
    @Digits(integer = 10, fraction = 2)
    private BigDecimal otherExcludeFTotal;
    @Digits(integer = 10, fraction = 2)
    private BigDecimal noOfPlotsWet;
    @Digits(integer = 10, fraction = 2)
    private BigDecimal noOfPlotsDry;
    @Digits(integer = 10, fraction = 2)
    private BigDecimal noOfPlotsTotal;
    @Digits(integer = 10, fraction = 2)
    private BigDecimal totalAreaWet;
    @Digits(integer = 10, fraction = 2)
    private BigDecimal totalAreaDry;
    @Digits(integer = 10, fraction = 2)
    private BigDecimal totalAreaForEstimation;

    private String remarks;
    private String adminRemarks;
    private UUID userId;
    private LocalDate created;
    private LocalDate updated;
    private Boolean isActive;
    private LocalDate agriStart;
    private LocalDate agriEnd;
    private Boolean isEdit;
    private String agriYear;
    private Long approveId;
    private String status;

    private String VerifiedStatus;
    private LocalDate VerifiedDate;
    private String VerifiedBy;
    private String VerifiedRemarks;
}
