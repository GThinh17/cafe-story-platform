package com.cafestory.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BlogRecommendationScoreTest {
    @Test
    void prePersist_success_initializesLegacyAndComponentDefaults_TC001() {
        BlogRecommendationScore score = new BlogRecommendationScore();
        score.setFormulaVersion(null);
        score.setRelationshipScore(null);
        score.setInterestScore(null);
        score.setEngagementScore(null);
        score.setQualityScore(null);
        score.setLocationScore(null);
        score.setDiversityScore(null);
        score.setUnseenScore(null);
        score.setActivityScore(null);
        score.setOwnAuthorScore(null);
        score.setReviewerScore(null);
        score.setSeenPenalty(null);
        score.setRepetitionPenalty(null);

        score.prePersist();

        assertThat(score.getCreatedAt()).isNotNull();
        assertThat(score.getComputedAt()).isEqualTo(score.getCreatedAt());
        assertThat(score.getFormulaVersion()).isEqualTo("LEGACY_V1");
        assertThat(score.getRelationshipScore()).isZero();
        assertThat(score.getInterestScore()).isZero();
        assertThat(score.getEngagementScore()).isZero();
        assertThat(score.getQualityScore()).isZero();
        assertThat(score.getLocationScore()).isZero();
        assertThat(score.getDiversityScore()).isZero();
        assertThat(score.getUnseenScore()).isZero();
        assertThat(score.getActivityScore()).isZero();
        assertThat(score.getOwnAuthorScore()).isZero();
        assertThat(score.getReviewerScore()).isZero();
        assertThat(score.getSeenPenalty()).isZero();
        assertThat(score.getRepetitionPenalty()).isZero();
    }

    @Test
    void prePersist_success_preservesExplicitValues_TC002() {
        LocalDateTime computedAt = LocalDateTime.of(2026, 7, 19, 10, 0);
        BlogRecommendationScore score = new BlogRecommendationScore();
        score.setComputedAt(computedAt);
        score.setFormulaVersion("EXPERT_V1");
        score.setRelationshipScore(0.1);
        score.setInterestScore(0.2);
        score.setEngagementScore(0.3);
        score.setQualityScore(0.4);
        score.setLocationScore(0.5);
        score.setDiversityScore(0.6);
        score.setUnseenScore(0.7);

        score.prePersist();

        assertThat(score.getComputedAt()).isEqualTo(computedAt);
        assertThat(score.getFormulaVersion()).isEqualTo("EXPERT_V1");
        assertThat(score.getRelationshipScore()).isEqualTo(0.1);
        assertThat(score.getInterestScore()).isEqualTo(0.2);
        assertThat(score.getEngagementScore()).isEqualTo(0.3);
        assertThat(score.getQualityScore()).isEqualTo(0.4);
        assertThat(score.getLocationScore()).isEqualTo(0.5);
        assertThat(score.getDiversityScore()).isEqualTo(0.6);
        assertThat(score.getUnseenScore()).isEqualTo(0.7);
    }
}
