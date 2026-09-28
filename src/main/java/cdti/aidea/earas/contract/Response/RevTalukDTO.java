package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevTalukDTO {
    private Long revTalukId;
    private String revTalukNameEn;
    private String revTalukNameMal;
    private Integer distId;
    private Boolean isActive;
    private Integer lsgCode;
    private Integer censusCode2001;
    private String censusCode2011;
    private String talukCodeApi;
    private UUID userId;
}
