package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourDiaryPurgeResponseDTO {
    private Long clusterId;
    private int deletedCount;
    private List<TourDiarySnapshotDTO> archivedTourDetails;
}