package cdti.aidea.earas.contract.FormEntryDto;

import cdti.aidea.earas.model.Btr_models.KeyPlots;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CropReplaceClusterResponse {
    private Long nextClusterId;
    private UUID plotId;
    private String message;


}
