package cdti.aidea.earas.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class WorkAllocationProgressResponse {

    private EstimationPlots noOfPlots;

    private AreaAvailable areaAvailableForEstimation;

    private List<LocationDetails> locations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationDetails {

        private Integer id;

        private String name;

        private Integer blockId;

        private String blockName;

        private Integer panchayathId;

        private String panchayathName;

        private VillageRecords villageRecords;

        private ExcludedArea excludedArea;

        private DistrictEstimation areaAvailableForEstimation;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstimationPlots {

        private BigDecimal wet;
        private BigDecimal dry;
        private BigDecimal total;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AreaAvailable {

        private BigDecimal wet;
        private BigDecimal dry;
        private BigDecimal total;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VillageRecords {

        private BigDecimal wet;
        private BigDecimal dry;
        private BigDecimal total;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExcludedArea {

        private BigDecimal forestArea;
        private BigDecimal plantationArea;
        private BigDecimal areaOfWaterBodies;
        private BigDecimal others;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DistrictEstimation {

        private EstimationPlots noOfPlots;

        private AreaAvailable areaInCents;
    }
}
