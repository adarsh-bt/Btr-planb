package cdti.aidea.earas.contract.FormEntryDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FetchTalukResponse {
    private Long clusterId;
    private Long talukId;
    private String talukName;
    private LocalDateTime createdAt;
    private String landType;
    private Double enumeratedArea;
}
