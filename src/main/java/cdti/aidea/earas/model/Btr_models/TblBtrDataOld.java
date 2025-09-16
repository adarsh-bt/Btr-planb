package cdti.aidea.earas.model.Btr_models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_btr_dataskilliold")
public class TblBtrDataOld {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private Integer resvno;
    private Integer resbdno;
//    private String resbdno;
    private String ltype;
    @Column(name = "bcode_str")
    private String bcode;
    private Double area;
    private Integer lsgcode;
    private String lbcode;
}
