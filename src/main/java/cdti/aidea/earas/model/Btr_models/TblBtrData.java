package cdti.aidea.earas.model.Btr_models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

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
  @Column(name = "owner_name")
  private String ownername;
    @Column(name = "tp_no")
  private Integer tpno;
    @Column(name = "tb_subdivision_no")
  private String tbsubdivisionno;

  @Column(name = "land_owner_address")
  private String address;

  @Column(name = "house_number")
//  private Integer houseno;
  private String houseno;

  @Column(name = "old_survey_number")
  private Integer oldsvno;

  @Column(name = "old_subdivision_number")
  private String oldsubno;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "non_btr_type_id")
  private TblNonBtr btrtype;

  //    private String remarks;
  //       private Double area;
     private LocalDateTime insertionTime;
     private LocalDateTime updationTime;

     private LocalDate agreStartYear;
     private LocalDate agreEndYear;
  @Column(name = "ward_number")
    private Integer wardnumber;
    private  String cl_no;

    private UUID created_by;
    private UUID updated_by;
    private Long zone;


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
