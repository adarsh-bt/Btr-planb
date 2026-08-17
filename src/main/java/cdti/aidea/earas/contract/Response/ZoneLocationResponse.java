package cdti.aidea.earas.contract.Response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class ZoneLocationResponse {
    private Integer districtId;
    private String districtName;

    private Integer talukId;
    private String talukName;

    private Integer blockId;
    private String blockName;

    private Integer localbodyId;
    private String localbodyName;
    private String lbCode;

    private String zoneName;

    private BigDecimal totalClusterEnumArea;
}
