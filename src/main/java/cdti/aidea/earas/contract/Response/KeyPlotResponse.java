package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;


@AllArgsConstructor
@Data
@NoArgsConstructor
public class KeyPlotResponse<T> {
        private String status;
        private String message;
        private String district;
        private String taluk;
        private String localbodyType;
        private String local_name;
        private String zone_name;
        private List<Map<String, Object>> data;
        private List<String> panchayth;
        private int totalCount;
        private double totalArea;
        private double totalWetArea;
        private double totalDryArea;
        private List<String> unclassifiedPanchayaths;

        // Constructor, Getters and Setters
}

