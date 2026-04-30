package cdti.aidea.earas.contract.Response;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DesTalukResponse {
    private Integer desTalukId;
    private String desTalukNameEn;
    private String desTalukNameMal;
    private int distId;
    private boolean isActive;
    private UUID addedBy;

}