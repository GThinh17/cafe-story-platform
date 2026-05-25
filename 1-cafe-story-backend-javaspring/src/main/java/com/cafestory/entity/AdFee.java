package com.cafestory.entity;

import com.cafestory.entity.enums.AdFeeType;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "ad_fees")
public class AdFee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ad_fee_id", updatable = false, nullable = false)
    private UUID adFeeId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false, unique = true)
    private AdFeeType feeType;

    @NotNull
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull
    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "VND";

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
        if (currency == null || currency.isBlank()) {
            currency = "VND";
        }
        if (status == null) {
            status = true;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
