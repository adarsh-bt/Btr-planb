package cdti.aidea.earas.model.Btr_models;

import cdti.aidea.earas.model.Btr_models.Masters.TblLocalBody;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterZone;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "keyplot_selections")
public class KeyPlots {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO) // Use AUTO or UUID-specific generator
  @Column(name = "kp_id")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "btr_id", nullable = false)
  // private TblBtrDataOld btrDataOld;
  private TblBtrData btrData;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "zone_id", nullable = false)
  private TblMasterZone zone;

  private Integer intervals;

  @Column(name = "selected_date", insertable = false, updatable = false)
  private LocalDate selectedDate;

  @Column(name = "agri_start_year")
  private LocalDate agriStartYear;

  @Column(name = "agri_end_year")
  private LocalDate agriEndYear;

  @Column(name = "is_rejected")
  private Boolean isRejected;

  private String reason;

  @Column(name = "reject_date")
  private LocalDate rejectDate;

  private Boolean status;

  @Column(name = "land_type")
  private String landType; // Expected values: "WET", "DRY"

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "localbody")
  private TblLocalBody localbody;
  // private Integer localbody;

  @Column(name = "created_by")
  private UUID created_by;

  private String owner_name;
  private String address;
  private String phone_number;
  private UUID details_updatedby;
  private String geocoordinate;

  //    @OneToMany(mappedBy = "keyPlot", cascade = CascadeType.ALL)
  //    private List<ClusterMaster> clusters;
}
