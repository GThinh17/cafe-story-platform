package com.cafestory.entity;

import com.cafestory.entity.enums.TrendWindowType;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(
        name = "blog_recommendation_scores",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "blog_id", "window_type", "context_region_id"}))
public class BlogRecommendationScore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "window_type", nullable = false)
    private TrendWindowType windowType;

    @Column(name = "context_region_id")
    private UUID contextRegionId;

    @NotNull
    @Column(name = "feed_score", nullable = false)
    private Double feedScore;

    @NotNull
    @Column(name = "trending_score", nullable = false)
    private Double trendingScore;

    @NotNull
    @Column(name = "followed_page_score", nullable = false)
    private Double followedPageScore;

    @NotNull
    @Column(name = "followed_user_score", nullable = false)
    private Double followedUserScore;

    @NotNull
    @Column(name = "same_region_score", nullable = false)
    private Double sameRegionScore;

    @NotNull
    @Column(name = "freshness_score", nullable = false)
    private Double freshnessScore;

    @NotNull
    @Column(name = "report_penalty", nullable = false)
    private Double reportPenalty;

    @NotNull
    @Column(name = "rank_position", nullable = false)
    private Integer rankPosition;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @NotNull
    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        if (computedAt == null) {
            computedAt = createdAt;
        }
    }
}
