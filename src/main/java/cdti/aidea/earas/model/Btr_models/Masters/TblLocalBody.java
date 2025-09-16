package cdti.aidea.earas.model.Btr_models.Masters;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_master_localbody")
public class TblLocalBody {

        @Id
        private Integer localbodyId;

        private String localbodyCode;

        private Short distId;

        private String localbodyNameEn;

        private String localbodyNameMal;

        private Short localbodyType;

        private String codeApi;

        private Boolean isActive;

        private String lsgCode;
}
