package com.cafestory.entity;

import com.cafestory.entity.enums.ReportTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "report_reasons")
public class ReportReason {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Size(max = 80)
    @Column(name = "code", nullable = false, unique = true, length = 80)
    private String code;

    @NotBlank
    @Size(max = 160)
    @Column(name = "label_vi", nullable = false, length = 160)
    private String labelVi;

    @Column(name = "description_vi", columnDefinition = "TEXT")
    private String descriptionVi;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 40)
    private ReportTargetType targetType;

    @NotNull
    @Column(name = "severity", nullable = false)
    private Integer severity = 1;

    @NotNull
    @Column(name = "requires_description", nullable = false)
    @ColumnDefault("false")
    private Boolean requiresDescription = false;

    @NotNull
    @Column(name = "is_active", nullable = false)
    @ColumnDefault("true")
    private Boolean active = true;

    @NotNull
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (severity == null) {
            severity = 1;
        }
        if (requiresDescription == null) {
            requiresDescription = false;
        }
        if (active == null) {
            active = true;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
