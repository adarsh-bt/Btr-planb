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

    // Added: cluster totals for included locations.
    private EstimationPlots noOfClusters;

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

        // Changed: previous combined field retained for reference.
        // private BigDecimal forestArea;
        // Added: separate wet/dry exclusions.
        private BigDecimal forestAreaWet;
        private BigDecimal forestAreaDry;
        // Changed: previous combined field retained for reference.
        // private BigDecimal plantationArea;
        // Added: separate wet/dry exclusions.
        private BigDecimal plantationAreaWet;
        private BigDecimal plantationAreaDry;
        // Changed: previous combined field retained for reference.
        // private BigDecimal areaOfWaterBodies;
        // Added: separate wet/dry exclusions.
        private BigDecimal areaOfWaterBodiesWet;
        private BigDecimal areaOfWaterBodiesDry;
        // Changed: previous combined field retained for reference.
        // private BigDecimal others;
        // Added: separate wet/dry exclusions.
        private BigDecimal othersWet;
        private BigDecimal othersDry;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DistrictEstimation {

        private EstimationPlots noOfPlots;

        private AreaAvailable areaInCents;

        // Added: cluster counts per location.
        private EstimationPlots noOfClusters;
    }
}
