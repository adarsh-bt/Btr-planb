package cdti.aidea.earas.model.Btr_models.Masters;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "land_type_classification")
public class LandTypeClassification {

    @Id
    private Long id;

    private String landType;
    private String classification;
    private String description;
    private boolean is_active;
}