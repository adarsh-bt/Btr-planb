package cdti.aidea.earas.contract;

import lombok.*;
import org.apache.poi.hpsf.Decimal;

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
    private String landType;

    private String zoneName;

    private Double totalClusterEnumArea;

}
