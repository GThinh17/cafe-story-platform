package com.cafestory.service.serviceImplement;

import com.cafestory.service.serviceInterface.FeedScoreCalculationService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FeedScoreCalculationServiceImpl implements FeedScoreCalculationService {
    static final double RELATIONSHIP_WEIGHT = 0.25;
    static final double INTEREST_WEIGHT = 0.20;
    static final double ENGAGEMENT_WEIGHT = 0.15;
    static final double FRESHNESS_WEIGHT = 0.15;
    static final double QUALITY_WEIGHT = 0.10;
    static final double LOCATION_WEIGHT = 0.05;
    static final double DIVERSITY_WEIGHT = 0.05;
    static final double UNSEEN_WEIGHT = 0.05;

    @Override
    public double calculateRelationshipScore(
            boolean ownAuthor,
            boolean followedSource,
            boolean positivelyInteracted) {
        if (ownAuthor || (followedSource && positivelyInteracted)) {
            return 1.0;
        }
        if (followedSource) {
            return 0.8;
        }
        return positivelyInteracted ? 0.5 : 0.0;
    }

    @Override
    public double calculateInterestScore(Set<String> userInterestTags, Set<String> blogTags) {
        Set<String> normalizedUserTags = normalizeTags(userInterestTags);
        Set<String> normalizedBlogTags = normalizeTags(blogTags);
        if (normalizedUserTags.isEmpty() || normalizedBlogTags.isEmpty()) {
            return 0.5;
        }

        Set<String> intersection = new HashSet<>(normalizedUserTags);
        intersection.retainAll(normalizedBlogTags);
        Set<String> union = new HashSet<>(normalizedUserTags);
        union.addAll(normalizedBlogTags);
        return clamp((double) intersection.size() / union.size());
    }

    @Override
    public double calculateEngagementScore(
            long views,
            long likes,
            long comments,
            long replies,
            long shares,
            long saves) {
        double raw = 0.1 * nonNegative(views)
                + 2.0 * nonNegative(likes)
                + 4.0 * nonNegative(comments)
                + 4.0 * nonNegative(replies)
                + 6.0 * nonNegative(shares)
                + 5.0 * nonNegative(saves);
        return clamp(raw / (raw + 100.0));
    }

    @Override
    public double calculateFreshnessScore(LocalDateTime createdAt, LocalDateTime scoredAt) {
        if (createdAt == null || scoredAt == null) {
            return 0.0;
        }
        double ageHours = Math.max(0.0, Duration.between(createdAt, scoredAt).toMinutes() / 60.0);
        return clamp(Math.exp(-ageHours / 48.0));
    }

    @Override
    public double calculateQualityScore(long activeReports) {
        return clamp(1.0 / (1.0 + 0.25 * nonNegative(activeReports)));
    }

    @Override
    public double calculateLocationScore(String userCity, String sourceCity) {
        if (isBlank(userCity) || isBlank(sourceCity)) {
            return 0.5;
        }
        return userCity.trim().equalsIgnoreCase(sourceCity.trim()) ? 1.0 : 0.0;
    }

    @Override
    public double calculateUnseenScore(boolean seenInLastSevenDays) {
        return seenInLastSevenDays ? 0.0 : 1.0;
    }

    @Override
    public double calculateDiversityScore(UUID sourceKey, List<UUID> previouslySelectedSourceKeys) {
        if (sourceKey == null || previouslySelectedSourceKeys == null || previouslySelectedSourceKeys.isEmpty()) {
            return 1.0;
        }
        long occurrences = previouslySelectedSourceKeys.stream()
                .skip(Math.max(0, previouslySelectedSourceKeys.size() - 2L))
                .filter(sourceKey::equals)
                .count();
        return switch ((int) Math.min(2, occurrences)) {
            case 0 -> 1.0;
            case 1 -> 0.5;
            default -> 0.0;
        };
    }

    @Override
    public double calculateStaticScore(ScoreComponents components) {
        if (components == null) {
            return 0.0;
        }
        return clamp(RELATIONSHIP_WEIGHT * clamp(components.relationship())
                + INTEREST_WEIGHT * clamp(components.interest())
                + ENGAGEMENT_WEIGHT * clamp(components.engagement())
                + FRESHNESS_WEIGHT * clamp(components.freshness())
                + QUALITY_WEIGHT * clamp(components.quality())
                + LOCATION_WEIGHT * clamp(components.location())
                + UNSEEN_WEIGHT * clamp(components.unseen()));
    }

    @Override
    public double calculateFinalScore(ScoreComponents components) {
        if (components == null) {
            return 0.0;
        }
        return clamp(calculateStaticScore(components) + DIVERSITY_WEIGHT * clamp(components.diversity()));
    }

    private Set<String> normalizeTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Set.of();
        }
        return tags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(tag -> tag.trim().toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private long nonNegative(long value) {
        return Math.max(0, value);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private double clamp(double value) {
        if (Double.isNaN(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }
}
