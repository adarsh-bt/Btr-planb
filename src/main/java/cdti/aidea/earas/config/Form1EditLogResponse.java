package cdti.aidea.earas.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Form1EditLogResponse {
    private UUID id;
    private UUID form1StatusDetailsId;
    private String status;
    private UUID requestedBy;
    private UUID approvedBy;
    private String requestedRemark;
    private String approvedRemark;
}
