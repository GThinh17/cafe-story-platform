package com.cafestory.service;

import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogRecommendationScore;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.TrendWindowType;
import com.cafestory.service.serviceImplement.BlogRecommendationScoreBatchWriterImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BlogRecommendationScoreBatchWriterImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private BlogRecommendationScoreBatchWriterImpl writer;

    @BeforeEach
    void setUp() {
        writer = new BlogRecommendationScoreBatchWriterImpl(jdbcTemplate);
    }

    @Test
    void upsertAll_success_emptyBatchDoesNotCallJdbc_TC001() {
        writer.upsertAll(List.of(), LocalDateTime.now());

        verify(jdbcTemplate, never()).batchUpdate(anyString(), org.mockito.ArgumentMatchers.any(SqlParameterSource[].class));
    }

    @Test
    void upsertAll_success_mapsAllScoresIntoSingleJdbcBatch_TC002() {
        UUID existingId = UUID.randomUUID();
        BlogRecommendationScore generatedIdScore = score(null, 1, 0.81);
        BlogRecommendationScore existingIdScore = score(existingId, 2, 0.72);
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 19, 18, 0);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<SqlParameterSource[]> batchCaptor = ArgumentCaptor.forClass(SqlParameterSource[].class);

        writer.upsertAll(List.of(generatedIdScore, existingIdScore), createdAt);

        verify(jdbcTemplate).batchUpdate(sqlCaptor.capture(), batchCaptor.capture());
        assertThat(sqlCaptor.getValue())
                .contains("on conflict", "formula_version")
                .doesNotContain("trend_window_type");
        assertThat(batchCaptor.getValue()).hasSize(2);
        assertThat(batchCaptor.getValue()[0].getValue("id")).isInstanceOf(UUID.class);
        assertThat(batchCaptor.getValue()[0].getValue("userId"))
                .isEqualTo(generatedIdScore.getUser().getUserId());
        assertThat(batchCaptor.getValue()[0].getValue("blogId"))
                .isEqualTo(generatedIdScore.getBlog().getId());
        assertThat(batchCaptor.getValue()[0].getValue("windowType")).isEqualTo("HOUR_24");
        assertThat(batchCaptor.getValue()[0].getValue("feedScore")).isEqualTo(0.81);
        assertThat(batchCaptor.getValue()[0].getValue("formulaVersion")).isEqualTo("EXPERT_V1");
        assertThat(batchCaptor.getValue()[0].getValue("createdAt")).isEqualTo(createdAt);
        assertThat(batchCaptor.getValue()[1].getValue("id")).isEqualTo(existingId);
        assertThat(batchCaptor.getValue()[1].getValue("rankPosition")).isEqualTo(2);
    }

    private BlogRecommendationScore score(UUID id, int rankPosition, double feedScore) {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());

        BlogRecommendationScore score = new BlogRecommendationScore();
        score.setId(id);
        score.setUser(user);
        score.setBlog(blog);
        score.setWindowType(TrendWindowType.HOUR_24);
        score.setContextRegionId(UUID.randomUUID());
        score.setFeedScore(feedScore);
        score.setFormulaVersion("EXPERT_V1");
        score.setRelationshipScore(0.8);
        score.setInterestScore(0.7);
        score.setEngagementScore(0.6);
        score.setQualityScore(1.0);
        score.setLocationScore(1.0);
        score.setDiversityScore(1.0);
        score.setUnseenScore(1.0);
        score.setTrendingScore(0.0);
        score.setFreshnessScore(0.9);
        score.setSameRegionScore(0.0);
        score.setFollowedUserScore(0.0);
        score.setFollowedPageScore(0.0);
        score.setActivityScore(0.0);
        score.setOwnAuthorScore(0.0);
        score.setReviewerScore(0.0);
        score.setReportPenalty(0.0);
        score.setSeenPenalty(0.0);
        score.setRepetitionPenalty(0.0);
        score.setRankPosition(rankPosition);
        score.setReason("formulaVersion=EXPERT_V1");
        score.setComputedAt(LocalDateTime.of(2026, 7, 19, 17, 59));
        return score;
    }
}
