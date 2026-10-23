package cdti.aidea.earas.config;


import cdti.aidea.earas.contract.Response.TourDiaryPurgeResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "tourClient", url = "http://localhost:8083/tour-diary")
public interface TourClient {

    @GetMapping("/api/purposes/impact-by-cluster/{clusterId}")
    TourDiaryPurgeResponseDTO getImpactByClusterId(
            @PathVariable("clusterId") Long clusterId
    );

    @DeleteMapping("/api/v1/internal/tour-diary/purge-by-cluster/{clusterId}")
    TourDiaryPurgeResponseDTO purgeByClusterId(
            @PathVariable("clusterId") Long clusterId
    );
}