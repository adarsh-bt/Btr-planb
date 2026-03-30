package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.RequestsDTOs.ClusterEditRequestDTO;
import cdti.aidea.earas.model.Btr_models.ClusterEditAllowed;
import cdti.aidea.earas.model.Btr_models.ClusterMaster;
import cdti.aidea.earas.model.Btr_models.EditRequestStatus;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import cdti.aidea.earas.repository.Btr_repo.ClusterEditAllowedRepository;
import cdti.aidea.earas.repository.Btr_repo.ClusterMasterRepository;
import cdti.aidea.earas.repository.Btr_repo.TblMasterZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClusterEditAllowedService {

    private final ClusterEditAllowedRepository editAllowedRepository;
    private final ClusterMasterRepository clusterMasterRepository;
    private final TblMasterZoneRepository masterZoneRepository;

    public ClusterEditAllowed createClusterEditRequest(ClusterEditRequestDTO dto) {

        // 🔹 clusterId validation
        if (dto.getClusterId() == null) {
            throw new RuntimeException("clusterId is required");
        }

        // 🔹 zoneId validation
        if (dto.getZoneId() == null) {
            throw new RuntimeException("zoneId is required");
        }

        // 🔹 status validation
        if (dto.getStatus() == null || dto.getStatus().isBlank()) {
            throw new RuntimeException("status is required");
        }

        // 🔹 APPROVED → approvedBy must be present
        if ("APPROVED".equalsIgnoreCase(dto.getStatus()) && dto.getApprovedBy() == null) {
            throw new RuntimeException("approvedBy is required when status is APPROVED");
        }

        //Fetch Cluster
        ClusterMaster cluster = clusterMasterRepository.findById(dto.getClusterId())
                .orElseThrow(() -> new RuntimeException("Cluster not found"));
        //Fetch Zone
        TblMasterZone zone = masterZoneRepository.findByZoneId(dto.getZoneId())
                .orElseThrow(() -> new RuntimeException("Zone not found"));
        System.out.println(dto);
        //Map to DTO
        ClusterEditAllowed entity = new ClusterEditAllowed();
        entity.setClusterMaster(cluster);
        entity.setZone(zone);
        entity.setRemarks(dto.getRemarks());
        entity.setRequestedBy(dto.getRequestedBy());
        entity.setApprovedBy(dto.getApprovedBy());
        entity.setTotalArea(BigDecimal.valueOf(dto.getTotalArea()));
        //Enum handling
        if (dto.getStatus() != null) {
            try {
                entity.setStatus(EditRequestStatus.valueOf(dto.getStatus().toUpperCase()));
                // System.out.println(dto.getStatus());
            } catch (Exception e) {
               // entity.setStatus(EditRequestStatus.PENDING);
                throw new RuntimeException("Invalid status value. Allowed: PENDING, APPROVED, REJECTED");
            }
        }
        entity.setIsActive(true);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        // ⚠️ Only set approvedAt if approved
        if (entity.getStatus() == EditRequestStatus.APPROVED) {
            entity.setApprovedAt(LocalDateTime.now());
        }

       // return editAllowedRepository.save(entity);
        ClusterEditAllowed saved = editAllowedRepository.save(entity);

        cluster.setIs_editable(true);
        cluster.setStatus("On Going"); // match your DB value exactly
        cluster.setUpdatedAt(LocalDateTime.now());

        clusterMasterRepository.save(cluster);

        return saved;
    }

}