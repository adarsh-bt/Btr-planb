package cdti.aidea.earas.contract.RequestsDTOs;
import java.util.UUID;

import cdti.aidea.earas.contract.UserAccessDTOs.AssignedUserResponse;
import cdti.aidea.earas.contract.UserAccessDTOs.UserLoginDetailsResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ZoneUserAssignDto {
    private Integer zoneId;
    private String zoneName;
    private Integer districtId;
    private Integer talukId;
    private Boolean isAssigned;
    @JsonIgnore
    private UUID assignedUserLoginId;// internal use only
    private AssignedUserResponse assignedUserId;  // API response

    public ZoneUserAssignDto(
            Integer zoneId,
            String zoneName,
            Integer districtId,
            Integer talukId,
            Boolean isAssigned,
            UUID assignedUserLoginId
    ) {
        this.zoneId = zoneId;
        this.zoneName = zoneName;
        this.districtId = districtId;
        this.talukId = talukId;
        this.isAssigned = isAssigned;
        this.assignedUserLoginId = assignedUserLoginId;
    }
}