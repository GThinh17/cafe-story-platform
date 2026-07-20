package com.cafestory.service.serviceImplement;

import com.cafestory.entity.BlogRecommendationScore;
import com.cafestory.service.serviceInterface.BlogRecommendationScoreBatchWriter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BlogRecommendationScoreBatchWriterImpl implements BlogRecommendationScoreBatchWriter {

    private static final String UPSERT_SQL = """
            insert into blog_recommendation_scores (
                id, user_id, blog_id, window_type, context_region_id, feed_score,
                formula_version, relationship_score, interest_score, engagement_score,
                quality_score, location_score, diversity_score, unseen_score,
                trending_score, freshness_score, same_region_score, followed_user_score,
                followed_page_score, activity_score, own_author_score, reviewer_score,
                report_penalty, seen_penalty, repetition_penalty, rank_position,
                reason, computed_at, created_at
            ) values (
                :id, :userId, :blogId, :windowType, :contextRegionId, :feedScore,
                :formulaVersion, :relationshipScore, :interestScore, :engagementScore,
                :qualityScore, :locationScore, :diversityScore, :unseenScore,
                :trendingScore, :freshnessScore, :sameRegionScore, :followedUserScore,
                :followedPageScore, :activityScore, :ownAuthorScore, :reviewerScore,
                :reportPenalty, :seenPenalty, :repetitionPenalty, :rankPosition,
                :reason, :computedAt, :createdAt
            )
            on conflict (user_id, blog_id, window_type, context_region_id)
            do update set
                feed_score = excluded.feed_score,
                formula_version = excluded.formula_version,
                relationship_score = excluded.relationship_score,
                interest_score = excluded.interest_score,
                engagement_score = excluded.engagement_score,
                quality_score = excluded.quality_score,
                location_score = excluded.location_score,
                diversity_score = excluded.diversity_score,
                unseen_score = excluded.unseen_score,
                trending_score = excluded.trending_score,
                freshness_score = excluded.freshness_score,
                same_region_score = excluded.same_region_score,
                followed_user_score = excluded.followed_user_score,
                followed_page_score = excluded.followed_page_score,
                activity_score = excluded.activity_score,
                own_author_score = excluded.own_author_score,
                reviewer_score = excluded.reviewer_score,
                report_penalty = excluded.report_penalty,
                seen_penalty = excluded.seen_penalty,
                repetition_penalty = excluded.repetition_penalty,
                rank_position = excluded.rank_position,
                reason = excluded.reason,
                computed_at = excluded.computed_at
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BlogRecommendationScoreBatchWriterImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void upsertAll(List<BlogRecommendationScore> scores, LocalDateTime createdAt) {
        if (scores.isEmpty()) {
            return;
        }

        SqlParameterSource[] batch = scores.stream()
                .map(score -> parameters(score, createdAt))
                .toArray(SqlParameterSource[]::new);
        jdbcTemplate.batchUpdate(UPSERT_SQL, batch);
    }

    private SqlParameterSource parameters(BlogRecommendationScore score, LocalDateTime createdAt) {
        return new MapSqlParameterSource()
                .addValue("id", score.getId() == null ? UUID.randomUUID() : score.getId())
                .addValue("userId", score.getUser().getUserId())
                .addValue("blogId", score.getBlog().getId())
                .addValue("windowType", score.getWindowType().name())
                .addValue("contextRegionId", score.getContextRegionId())
                .addValue("feedScore", score.getFeedScore())
                .addValue("formulaVersion", score.getFormulaVersion())
                .addValue("relationshipScore", score.getRelationshipScore())
                .addValue("interestScore", score.getInterestScore())
                .addValue("engagementScore", score.getEngagementScore())
                .addValue("qualityScore", score.getQualityScore())
                .addValue("locationScore", score.getLocationScore())
                .addValue("diversityScore", score.getDiversityScore())
                .addValue("unseenScore", score.getUnseenScore())
                .addValue("trendingScore", score.getTrendingScore())
                .addValue("freshnessScore", score.getFreshnessScore())
                .addValue("sameRegionScore", score.getSameRegionScore())
                .addValue("followedUserScore", score.getFollowedUserScore())
                .addValue("followedPageScore", score.getFollowedPageScore())
                .addValue("activityScore", score.getActivityScore())
                .addValue("ownAuthorScore", score.getOwnAuthorScore())
                .addValue("reviewerScore", score.getReviewerScore())
                .addValue("reportPenalty", score.getReportPenalty())
                .addValue("seenPenalty", score.getSeenPenalty())
                .addValue("repetitionPenalty", score.getRepetitionPenalty())
                .addValue("rankPosition", score.getRankPosition())
                .addValue("reason", score.getReason())
                .addValue("computedAt", score.getComputedAt())
                .addValue("createdAt", createdAt);
    }
}
