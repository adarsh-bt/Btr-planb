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
@Table(name = "tbl_zone_localbody_mapping")
public class TblZoneLocalbodyMapping {
    @Id
    private Integer zoneLocalbodyMappingId;
    private Integer zone;          // zone id
    private Integer localbody;     // localbody id
    private Boolean isValid;
}
