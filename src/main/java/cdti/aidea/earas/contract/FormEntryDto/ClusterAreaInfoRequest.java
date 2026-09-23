package cdti.aidea.earas.contract.FormEntryDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusterAreaInfoRequest {

    private Long clusterId;

    private BigDecimal clusterArea;

    private Integer clusterNo;

    private Integer districtId;
    private String districtName;

    private Integer talukId;
    private String talukName;

    private Integer blockId;
    private String blockName;

    private Integer zoneId;
    private String zoneName;

    private String landType;

    private String agriYear;
}