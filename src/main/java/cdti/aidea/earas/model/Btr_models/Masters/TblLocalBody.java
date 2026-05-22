package cdti.aidea.earas.model.Btr_models.Masters;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.rmi.server.UID;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_master_localbody")
public class TblLocalBody {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "localbody_id")
    private Integer localbodyId;

    @Column(name = "localbody_code")
    private String localbodyCode;

    @Column(name = "dist_id")
    private Short distId;
    @Column(name = "localbody_name_en")
    private String localbodyNameEn;

    @Column(name = "localbody_name_mal")
    private String localbodyNameMal;

    @Column(name = "localbody_type")
    private Short localbodyType;
    @Column(name = "code_api")
    private String codeApi;
    @Column(name = "is_active")
    private Boolean isActive;
    @Column(name = "lsg_code")
    private String lsgCode;
    @Column(name = "created_by")
    private UUID addedBy;
    @Column(name = "updated_by")
    private UUID updatedBy;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
