package cdti.aidea.earas.contract.Response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RevTalukResponse {
    private Long revTalukId;
    private String revTalukNameEn;
    private String revTalukNameMal;
    private Integer distId;
    private Boolean isActive;
    private Integer lsgCode;
    private Integer censusCode2001;
    private String censusCode2011;
    private String talukCodeApi;
    private UUID createdBy;
    private UUID updatedBy;
}