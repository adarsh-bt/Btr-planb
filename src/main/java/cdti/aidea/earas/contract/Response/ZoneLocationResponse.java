package cdti.aidea.earas.contract.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class ZoneLocationResponse {
    private Integer districtId;
    private String districtName;

    private Integer talukId;
    private String talukName;

    private Integer blockId;
    private String blockName;

    private String zoneName;

    private Integer localbodyId;
    private String localbodyName;
    private String lbCode;
    private String landType;

    private Double totalClusterEnumArea;

}
