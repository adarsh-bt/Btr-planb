package cdti.aidea.earas.contract.RequestsDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyplotsLimitLogRequest {

    private Long id;
    private Long keyplotsLimit;
    private Boolean isEdited;
    private UUID editPermitter;
    private UUID addedBy;
    private String remarks;
//    private LocalDate agriStartYear;
//    private LocalDate agriEndYear;
//    private Boolean isActive;
}