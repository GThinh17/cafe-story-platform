package com.cafestory.entity;

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

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "reviewer_scoring_formula")
public class ReviewerScoringFormula {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "like_weight", nullable = false)
    private int likeWeight = 1;

    @Column(name = "comment_weight", nullable = false)
    private int commentWeight = 5;

    @Column(name = "share_weight", nullable = false)
    private int shareWeight = 3;

    @Column(name = "like_payout_amount", nullable = false)
    private long likePayoutAmount = 100L;

    @Column(name = "comment_payout_amount", nullable = false)
    private long commentPayoutAmount = 500L;

    @Column(name = "share_payout_amount", nullable = false)
    private long sharePayoutAmount = 300L;

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
}
