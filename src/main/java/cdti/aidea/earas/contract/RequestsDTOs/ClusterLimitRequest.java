package cdti.aidea.earas.contract.RequestsDTOs;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterLimitRequest {

    private Long id; // For update
    private BigDecimal clusterMin;
    private BigDecimal clusterMax;
    private BigDecimal tsoLimit;
    private UUID addedBy;
    private String remarks;
    private LocalDate agriStartYear;
    private LocalDate agriEndYear;
    private Boolean inActive;
}
