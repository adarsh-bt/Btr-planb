package cdti.aidea.earas.model.Btr_models.Masters;

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
@Table(name = "tbl_master_taluk_revenue")
public class RevTaluk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rev_taluk_id")
    private Long revTalukId;

    @Column(name = "rev_taluk_name_en")
    private String revTalukNameEn;

    @Column(name = "rev_taluk_name_mal")
    private String revTalukNameMal;

    @Column(name = "dist_id")
    private Integer distId;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "lsg_code")
    private Integer lsgCode;

    @Column(name = "census_code_2001")
    private Integer censusCode2001;

    @Column(name = "census_code_2011")
    private String censusCode2011;

    @Column(name = "taluk_code_api")
    private String talukCodeApi;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}