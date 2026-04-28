package cdti.aidea.earas.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TblLocalBodyRequest {
    private Integer localbodyId;
    private String localbodyCode;
    private Short distId;
    private String localbodyNameEn;
    private String localbodyNameMal;
    private Short localbodyType;
    private String codeApi;
    private Boolean isActive;
    private String lsgCode;
    private UUID userId;
}
