package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KeyPlotDetailsResponse {
        private UUID keyplotId;
        private String kvillageName;
        private Integer kvillageId;
        private String villageBlock;
        private String panchayath;
        private String lbcode;
        private BigDecimal clusterMax;
        private BigDecimal clusterMin;
        private BigDecimal clusterMean;
        private String syNo;
        private double areaCents;
        private String landType;
        private List<SidePlotDTO> sidePlots;
}
