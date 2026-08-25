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
import jakarta.persistence.Index;
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
        indexes = {
                @Index(
                        name = "idx_blog_recommendation_scores_latest",
                        columnList = "user_id, window_type, context_region_id, computed_at, rank_position"),
                @Index(name = "idx_blog_recommendation_scores_blog", columnList = "blog_id")
        },
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
    @Column(name = "formula_version", nullable = false, length = 32)
    private String formulaVersion = "LEGACY_V1";

    @NotNull
    @Column(name = "relationship_score", nullable = false)
    private Double relationshipScore = 0.0;

    @NotNull
    @Column(name = "interest_score", nullable = false)
    private Double interestScore = 0.0;

    @NotNull
    @Column(name = "engagement_score", nullable = false)
    private Double engagementScore = 0.0;

    @NotNull
    @Column(name = "quality_score", nullable = false)
    private Double qualityScore = 0.0;

    @NotNull
    @Column(name = "location_score", nullable = false)
    private Double locationScore = 0.0;

    @NotNull
    @Column(name = "diversity_score", nullable = false)
    private Double diversityScore = 0.0;

    @NotNull
    @Column(name = "unseen_score", nullable = false)
    private Double unseenScore = 0.0;

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
    @Column(name = "activity_score", nullable = false)
    private Double activityScore = 0.0;

    @NotNull
    @Column(name = "own_author_score", nullable = false)
    private Double ownAuthorScore = 0.0;

    @NotNull
    @Column(name = "reviewer_score", nullable = false)
    private Double reviewerScore = 0.0;

    @NotNull
    @Column(name = "report_penalty", nullable = false)
    private Double reportPenalty;

    @NotNull
    @Column(name = "seen_penalty", nullable = false)
    private Double seenPenalty = 0.0;

    @NotNull
    @Column(name = "repetition_penalty", nullable = false)
    private Double repetitionPenalty = 0.0;

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
        if (activityScore == null) {
            activityScore = 0.0;
        }
        if (formulaVersion == null || formulaVersion.isBlank()) {
            formulaVersion = "LEGACY_V1";
        }
        relationshipScore = valueOrZero(relationshipScore);
        interestScore = valueOrZero(interestScore);
        engagementScore = valueOrZero(engagementScore);
        qualityScore = valueOrZero(qualityScore);
        locationScore = valueOrZero(locationScore);
        diversityScore = valueOrZero(diversityScore);
        unseenScore = valueOrZero(unseenScore);
        if (ownAuthorScore == null) {
            ownAuthorScore = 0.0;
        }
        if (reviewerScore == null) {
            reviewerScore = 0.0;
        }
        if (seenPenalty == null) {
            seenPenalty = 0.0;
        }
        if (repetitionPenalty == null) {
            repetitionPenalty = 0.0;
        }
    }

    private Double valueOrZero(Double value) {
        return value == null ? 0.0 : value;
    }
}
