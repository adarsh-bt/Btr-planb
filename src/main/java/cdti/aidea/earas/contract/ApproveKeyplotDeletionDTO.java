package cdti.aidea.earas.contract;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveKeyplotDeletionDTO {
    private UUID requestId;
    private UUID adminId;
    private String adminRemark;
}
