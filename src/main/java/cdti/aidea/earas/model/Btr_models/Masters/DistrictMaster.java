package cdti.aidea.earas.model.Btr_models.Masters;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Builder
@Setter
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@Entity(name = "tbl_master_district")
public class DistrictMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dist_id")
    private Integer distId;

    @Column(name = "dist_name_en")
    private String distNameEn;

    @Column(name = "dist_name_mal")
    private String distNameMal;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "dist_lsg_code")
    private Integer dist_lsg_code;

    @Column(name = "dist_code")
    private String dist_code;

    @Column(name = "census_code_2011")
    private String census_code_2011;

    @Column(name = "census_code_2001")
    private String census_code_2001;

    @Column(name = "des_dist_code")
    private Integer des_dist_code;

    @Column(name = "created_by")
    private UUID addedBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
