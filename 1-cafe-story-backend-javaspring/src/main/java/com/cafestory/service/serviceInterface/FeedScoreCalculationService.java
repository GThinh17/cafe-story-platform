package com.cafestory.service.serviceInterface;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface FeedScoreCalculationService {
    String FORMULA_VERSION = "EXPERT_V1";

    double calculateRelationshipScore(boolean ownAuthor, boolean followedSource, boolean positivelyInteracted);

    double calculateInterestScore(Set<String> userInterestTags, Set<String> blogTags);

    double calculateEngagementScore(long views, long likes, long comments, long replies, long shares, long saves);

    double calculateFreshnessScore(LocalDateTime createdAt, LocalDateTime scoredAt);

    double calculateQualityScore(long activeReports);

    double calculateLocationScore(String userCity, String sourceCity);

    double calculateUnseenScore(boolean seenInLastSevenDays);

    double calculateDiversityScore(UUID sourceKey, List<UUID> previouslySelectedSourceKeys);

    double calculateStaticScore(ScoreComponents components);

    double calculateFinalScore(ScoreComponents components);

    record ScoreComponents(
            double relationship,
            double interest,
            double engagement,
            double freshness,
            double quality,
            double location,
            double diversity,
            double unseen) {
    }
}
