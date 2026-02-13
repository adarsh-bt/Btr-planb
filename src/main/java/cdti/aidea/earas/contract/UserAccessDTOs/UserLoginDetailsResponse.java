package cdti.aidea.earas.contract.UserAccessDTOs;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginDetailsResponse {
    private UUID userId;
    private String name;
    private Long designationId;
    private String designation;

    private Set<Long> roleId;
    private Set<String> roles;

    private Long districtId;
    private String districtName;

    private Long talukId;
    private String talukName;
}
