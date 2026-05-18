package com.cafestory.entity;

import com.cafestory.entity.enums.ReviewerBadge;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(
        name = "reviewer_badges",
        uniqueConstraints = @UniqueConstraint(columnNames = {"reviewer_id", "badge_month"}))
public class ReviewerBadgeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @NotNull
    @Column(name = "badge_month", nullable = false)
    private String month;

    @Column(name = "score", nullable = false)
    private long score;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "badge", nullable = false)
    private ReviewerBadge badge;

    @Column(name = "like_count", nullable = false)
    private long likeCount;

    @Column(name = "share_count", nullable = false)
    private long shareCount;

    @Column(name = "comment_count", nullable = false)
    private long commentCount;

    @NotNull
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
