package cdti.aidea.earas.model.Btr_models.Masters;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_master_zone")

public class TblMasterZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "zone_id")
    private Integer zoneId;

    @Column(name = "zone_code")
    private Integer zoneCode;

    @Column(name = "zone_name_en", nullable = false)
    private String zoneNameEn;

    @Column(name = "zone_name_mal")
    private String zoneNameMal;

    @Column(name = "des_taluk_id", nullable = false)
    private Integer desTalukId;


    @Column(name = "des_dist_id", nullable = false)
    private Integer desDistId;


    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "zone_user")
    private String zoneUser;

    @Column(name = "dist_id")
    private Integer distId;



}
