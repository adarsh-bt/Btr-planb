package cdti.aidea.earas.model.Btr_models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_btr_data")
public class TblBtrData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer dcode;
    private Integer tcode;
    private Integer vcode;
    private String bcode;

    private Integer resvno;
    private String resbdno;

    private String lbtype;
    private String lbcode;
    private String govpriv;
    private String ltype;
    private String landuse;
    private Double nhect;
    private Double nare;
    private Double nsqm;
    private Double east;
    private Double west;
    private Double north;
    private Double south;
    private Integer lsgcode;
    private Double totCent;

    @Column(name = "land_owner_address")
    private String address;
    @Column(name = "house_number")
    private Integer houseno;
    @Column(name = "main_number")
    private Integer mainno;
    @Column(name = "sub_main_number")
    private String subno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "non_btr_type_id")
    private TblNonBtr btrtype;

//    private String remarks;
//    private Double area;
//    private LocalDateTime insertionTime;
//    private LocalDateTime updationTime;
//
//    private LocalDate agreStartYear;
//    private LocalDate agreEndYear;
//    private Boolean reject;
//
//    private String reson;
//    private String localboy;
//
//    @Column(columnDefinition = "jsonb")
//    private String addonNotes;
//
//    private Integer fmp;
}
