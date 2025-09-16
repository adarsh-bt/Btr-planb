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
@Table(name = "keyplots_limit_log")
public class KeyplotsLimitLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keyplots_limit", nullable = false)
    private Long keyplotsLimit;

    @Column(name = "is_edited")
    private Boolean isEdited = false;

    @Column(name = "edit_permitter")
    private UUID editPermitter;

    @Column(name = "added_by")
    private UUID addedBy;

    private String remarks;

    @Column(name = "agri_start_year", nullable = false)
    private LocalDate agriStartYear;

    @Column(name = "agri_end_year", nullable = false)
    private LocalDate agriEndYear;

    private Boolean in_active = true;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}