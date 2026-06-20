package com.cafestory.entity;

import com.cafestory.entity.enums.ReviewerBadge;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "reviewer_formula")
public class ReviewerFormula {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Scoring weights
    @Column(name = "like_weight", nullable = false)
    private int likeWeight = 1;

    @Column(name = "comment_weight", nullable = false)
    private int commentWeight = 5;

    @Column(name = "share_weight", nullable = false)
    private int shareWeight = 3;

    // Payout amounts
    @Column(name = "like_payout_amount", nullable = false)
    private long likePayoutAmount = 100L;

    @Column(name = "comment_payout_amount", nullable = false)
    private long commentPayoutAmount = 500L;

    @Column(name = "share_payout_amount", nullable = false)
    private long sharePayoutAmount = 300L;

    // Badge multipliers
    @Column(name = "iron_multiplier", nullable = false, precision = 5, scale = 2)
    private BigDecimal ironMultiplier = BigDecimal.ONE;

    @Column(name = "bronze_multiplier", nullable = false, precision = 5, scale = 2)
    private BigDecimal bronzeMultiplier = new BigDecimal("1.20");

    @Column(name = "silver_multiplier", nullable = false, precision = 5, scale = 2)
    private BigDecimal silverMultiplier = new BigDecimal("1.50");

    @Column(name = "gold_multiplier", nullable = false, precision = 5, scale = 2)
    private BigDecimal goldMultiplier = new BigDecimal("2.00");

    @Column(name = "diamond_multiplier", nullable = false, precision = 5, scale = 2)
    private BigDecimal diamondMultiplier = new BigDecimal("3.00");

    @Column(name = "active", nullable = false)
    private boolean active = false;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public BigDecimal getMultiplierForBadge(ReviewerBadge badge) {
        return switch (badge) {
            case IRON -> ironMultiplier;
            case BRONZE -> bronzeMultiplier;
            case SILVER -> silverMultiplier;
            case GOLD -> goldMultiplier;
            case DIAMOND -> diamondMultiplier;
        };
    }
}
