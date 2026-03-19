package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZonePageResponse {

    private List<ZoneListResponse> zones;
    private int currentPage;
    private long totalItems;
    private int totalPages;
}