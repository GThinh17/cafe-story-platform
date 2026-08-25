package com.cafestory.service;

import com.cafestory.service.serviceImplement.FeedScoreCalculationServiceImpl;
import com.cafestory.service.serviceInterface.FeedScoreCalculationService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class FeedScoreCalculationServiceImplTest {
    private final FeedScoreCalculationService service = new FeedScoreCalculationServiceImpl();

    @Test
    void calculateRelationshipScore_coversAllRules_TC001() {
        assertThat(service.calculateRelationshipScore(true, false, false)).isEqualTo(1.0);
        assertThat(service.calculateRelationshipScore(false, true, true)).isEqualTo(1.0);
        assertThat(service.calculateRelationshipScore(false, true, false)).isEqualTo(0.8);
        assertThat(service.calculateRelationshipScore(false, false, true)).isEqualTo(0.5);
        assertThat(service.calculateRelationshipScore(false, false, false)).isZero();
    }

    @Test
    void calculateInterestScore_usesNormalizedJaccardAndFallback_TC002() {
        assertThat(service.calculateInterestScore(Set.of(" Coffee ", "LATTE"), Set.of("coffee", "brew")))
                .isCloseTo(1.0 / 3.0, within(0.000001));
        assertThat(service.calculateInterestScore(Set.of(), Set.of("coffee"))).isEqualTo(0.5);
        assertThat(service.calculateInterestScore(Set.of("coffee"), Set.of())).isEqualTo(0.5);
        assertThat(service.calculateInterestScore(null, Set.of("coffee"))).isEqualTo(0.5);
        assertThat(service.calculateInterestScore(Set.of("coffee"), null)).isEqualTo(0.5);
        assertThat(service.calculateInterestScore(Set.of("coffee"), Set.of("tea"))).isZero();
    }

    @Test
    void calculateEngagementScore_saturatesAndClampsNegativeCounts_TC003() {
        assertThat(service.calculateEngagementScore(0, 0, 0, 0, 0, 0)).isZero();
        assertThat(service.calculateEngagementScore(100, 10, 5, 5, 5, 4))
                .isCloseTo(120.0 / 220.0, within(0.000001));
        assertThat(service.calculateEngagementScore(1_000_000, 0, 0, 0, 0, 0)).isGreaterThan(0.99);
        assertThat(service.calculateEngagementScore(-1, -1, -1, -1, -1, -1)).isZero();
    }

    @Test
    void calculateFreshnessScore_appliesFortyEightHourDecayAndBoundaries_TC004() {
        LocalDateTime scoredAt = LocalDateTime.of(2026, 7, 19, 12, 0);
        assertThat(service.calculateFreshnessScore(scoredAt, scoredAt)).isEqualTo(1.0);
        assertThat(service.calculateFreshnessScore(scoredAt.minusHours(48), scoredAt))
                .isCloseTo(Math.exp(-1.0), within(0.000001));
        assertThat(service.calculateFreshnessScore(scoredAt.plusHours(2), scoredAt)).isEqualTo(1.0);
        assertThat(service.calculateFreshnessScore(null, scoredAt)).isZero();
        assertThat(service.calculateFreshnessScore(scoredAt, null)).isZero();
    }

    @Test
    void calculateQualityLocationAndUnseen_followBusinessRules_TC005() {
        assertThat(service.calculateQualityScore(0)).isEqualTo(1.0);
        assertThat(service.calculateQualityScore(4)).isEqualTo(0.5);
        assertThat(service.calculateQualityScore(-1)).isEqualTo(1.0);
        assertThat(service.calculateLocationScore(" Ho Chi Minh ", "ho chi minh")).isEqualTo(1.0);
        assertThat(service.calculateLocationScore("Hue", "Da Nang")).isZero();
        assertThat(service.calculateLocationScore(null, "Hue")).isEqualTo(0.5);
        assertThat(service.calculateLocationScore(" ", "Hue")).isEqualTo(0.5);
        assertThat(service.calculateUnseenScore(false)).isEqualTo(1.0);
        assertThat(service.calculateUnseenScore(true)).isZero();
    }

    @Test
    void calculateDiversityScore_usesOnlyTwoPreviousSources_TC006() {
        UUID source = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        assertThat(service.calculateDiversityScore(source, List.of())).isEqualTo(1.0);
        assertThat(service.calculateDiversityScore(source, null)).isEqualTo(1.0);
        assertThat(service.calculateDiversityScore(null, List.of(source))).isEqualTo(1.0);
        assertThat(service.calculateDiversityScore(source, List.of(source, other))).isEqualTo(0.5);
        assertThat(service.calculateDiversityScore(source, List.of(source, source))).isZero();
        assertThat(service.calculateDiversityScore(source, List.of(source, other, other))).isEqualTo(1.0);
    }

    @Test
    void calculateFinalScore_matchesDocumentExampleAndWeightSum_TC007() {
        FeedScoreCalculationService.ScoreComponents example = new FeedScoreCalculationService.ScoreComponents(
                0.8, 0.6, 0.6, 0.779, 1.0, 1.0, 0.5, 1.0);
        assertThat(service.calculateFinalScore(example)).isCloseTo(0.75185, within(0.000001));

        FeedScoreCalculationService.ScoreComponents allOne = new FeedScoreCalculationService.ScoreComponents(
                1, 1, 1, 1, 1, 1, 1, 1);
        assertThat(service.calculateStaticScore(allOne)).isCloseTo(0.95, within(0.000000000001));
        assertThat(service.calculateFinalScore(allOne)).isEqualTo(1.0);
    }

    @Test
    void calculateScores_clampsComponentsAndHandlesNull_TC008() {
        FeedScoreCalculationService.ScoreComponents outsideRange = new FeedScoreCalculationService.ScoreComponents(
                2, -1, Double.NaN, 2, -1, 2, 2, -1);
        assertThat(service.calculateStaticScore(outsideRange)).isBetween(0.0, 0.95);
        assertThat(service.calculateFinalScore(outsideRange)).isBetween(0.0, 1.0);
        assertThat(service.calculateStaticScore(null)).isZero();
        assertThat(service.calculateFinalScore(null)).isZero();
    }
}
