package cdti.aidea.earas.contract.RequestsDTOs;

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

    private BigDecimal villageWetArea;
    private BigDecimal villageDryArea;
    private BigDecimal villageTotalArea;
    private BigDecimal forestAreaA;
    private BigDecimal forestAreaB;
    private BigDecimal forestAreaC;
    private BigDecimal areaUnderPlant;
    private BigDecimal forestExcludeUnclutivate;
    private BigDecimal forestExcludeNotUnclutivate;
    private BigDecimal kayalExcludeArea;
    private BigDecimal otherExcludeFWet;
    private BigDecimal otherExcludedFDry;
    private BigDecimal otherExcludeFTotal;
    private BigDecimal noOfPlotsWet;
    private BigDecimal noOfPlotsDry;
    private BigDecimal noOfPlotsTotal;
    private BigDecimal totalAreaWet;
    private BigDecimal totalAreaDry;
    private BigDecimal totalAreaForEstimation;

    private String remarks;
    private UUID userId;
    private LocalDate created;
    private LocalDate updated;
    private Boolean isActive;
    private LocalDate agriStart;
    private LocalDate agriEnd;
}
