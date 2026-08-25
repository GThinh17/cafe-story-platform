package com.cafestory.entity;

import com.cafestory.entity.enums.ExtraFeeType;
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
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "extra_fees")
public class ExtraFee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "extra_fee_id", updatable = false, nullable = false)
    private UUID extraFeeId;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false)
    private ExtraFeeType feeType;

    @Column(name = "price", nullable = false)
    private long price;

    @Column(name = "duration_months")
    private Integer durationMonths;

    @Column(name = "max_members")
    private Integer maxMembers;

    @NotNull
    @Column(name = "status", nullable = false)
    @ColumnDefault("true")
    private Boolean status = true;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (status == null) {
            status = true;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
