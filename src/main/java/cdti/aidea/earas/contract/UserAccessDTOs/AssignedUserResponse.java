package cdti.aidea.earas.contract.UserAccessDTOs;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignedUserResponse {
    private UUID userId;
    private String name;
    private Long designationId;
    private String designation;
    private Set<Long> roleId;
    private Set<String> roles;
}