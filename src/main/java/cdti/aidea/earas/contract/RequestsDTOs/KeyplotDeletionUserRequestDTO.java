package cdti.aidea.earas.contract.RequestsDTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class KeyplotDeletionUserRequestDTO {

    @NotNull(message = "keyplotId is required")
    private UUID keyplotId;

    @NotNull(message = "Btr is required")
    private Long BtrId;

    @NotBlank(message = "Reason for deletion/rectification is required")
    private String reason;

    @NotNull(message = "requestedBy user UUID is required")
    private UUID requestedBy;
}