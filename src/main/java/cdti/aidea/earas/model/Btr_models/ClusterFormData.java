package cdti.aidea.earas.model.Btr_models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cluster_details")
public class ClusterFormData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clu_detail_id")
    private Long cluDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id", nullable = false)
    private ClusterMaster clusterMaster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plot_id", nullable = false)
    private TblBtrData plot;

    @Column(name = "plot_label")
    private String plotLabel;

    @Column(name = "enumerated_area")
    private Double enumeratedArea;

    private UUID createdBy;

    private Boolean status;

    @Column(name = "created_at", updatable = false,columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt = LocalDateTime.now();;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt = LocalDateTime.now();

    private String remark;
}