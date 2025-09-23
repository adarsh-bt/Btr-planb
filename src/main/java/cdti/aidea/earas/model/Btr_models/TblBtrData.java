package cdti.aidea.earas.model.Btr_models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

//    @Column(length = 500)
//    private String remarks;

    private Double east;
    private Double west;
    private Double north;
    private Double south;
    private Integer lsgcode;
//    private Double area;
//    private LocalDateTime insertionTime;
//    private LocalDateTime updationTime;
//
//    private LocalDate agreStartYear;
//    private LocalDate agreEndYear;
    private Double totCent;


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
