package cdti.aidea.earas.service;

import cdti.aidea.earas.model.Btr_models.ClusterLimitLog;
import cdti.aidea.earas.repository.Btr_repo.ClusterLimitLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClusterLimitService {

    private final ClusterLimitLogRepository repository;

    @Cacheable("clusterLimit")
    public ClusterLimitLog getClusterLimit() {

        return repository.findByInActiveTrue()
                .orElseThrow(() ->
                        new RuntimeException("Cluster Limit not found"));
    }
}