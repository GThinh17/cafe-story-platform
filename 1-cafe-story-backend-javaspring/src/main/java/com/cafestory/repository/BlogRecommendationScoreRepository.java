package com.cafestory.repository;

import com.cafestory.entity.BlogRecommendationScore;
import com.cafestory.entity.enums.TrendWindowType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BlogRecommendationScoreRepository extends JpaRepository<BlogRecommendationScore, UUID> {

    @Query("""
            select max(s.computedAt)
            from BlogRecommendationScore s
            where s.user.userId = :userId
              and s.windowType = :windowType
              and ((:contextRegionId is null and s.contextRegionId is null)
                   or s.contextRegionId = :contextRegionId)
            """)
    LocalDateTime findLatestComputedAt(
            @Param("userId") UUID userId,
            @Param("windowType") TrendWindowType windowType,
            @Param("contextRegionId") UUID contextRegionId);

    @Query("""
            select s
            from BlogRecommendationScore s
            where s.user.userId = :userId
              and s.windowType = :windowType
              and s.computedAt = :computedAt
              and ((:contextRegionId is null and s.contextRegionId is null)
                   or s.contextRegionId = :contextRegionId)
            order by s.rankPosition asc
            """)
    List<BlogRecommendationScore> findLatestPage(
            @Param("userId") UUID userId,
            @Param("windowType") TrendWindowType windowType,
            @Param("contextRegionId") UUID contextRegionId,
            @Param("computedAt") LocalDateTime computedAt,
            Pageable pageable);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            delete from BlogRecommendationScore s
            where s.user.userId = :userId
              and s.windowType = :windowType
              and ((:contextRegionId is null and s.contextRegionId is null)
                   or s.contextRegionId = :contextRegionId)
            """)
    void deleteByUserWindowAndContextRegion(
            @Param("userId") UUID userId,
            @Param("windowType") TrendWindowType windowType,
            @Param("contextRegionId") UUID contextRegionId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            insert into blog_recommendation_scores (
                id,
                user_id,
                blog_id,
                window_type,
                context_region_id,
                feed_score,
                trending_score,
                freshness_score,
                same_region_score,
                followed_user_score,
                followed_page_score,
                report_penalty,
                rank_position,
                reason,
                computed_at,
                created_at
            )
            values (
                :id,
                :userId,
                :blogId,
                :windowType,
                :contextRegionId,
                :feedScore,
                :trendingScore,
                :freshnessScore,
                :sameRegionScore,
                :followedUserScore,
                :followedPageScore,
                :reportPenalty,
                :rankPosition,
                :reason,
                :computedAt,
                :createdAt
            )
            on conflict (user_id, blog_id, window_type, context_region_id)
            do update set
                feed_score = excluded.feed_score,
                trending_score = excluded.trending_score,
                freshness_score = excluded.freshness_score,
                same_region_score = excluded.same_region_score,
                followed_user_score = excluded.followed_user_score,
                followed_page_score = excluded.followed_page_score,
                report_penalty = excluded.report_penalty,
                rank_position = excluded.rank_position,
                reason = excluded.reason,
                computed_at = excluded.computed_at
            """, nativeQuery = true)
    void upsertRecommendationScore(
            @Param("id") UUID id,
            @Param("userId") UUID userId,
            @Param("blogId") UUID blogId,
            @Param("windowType") String windowType,
            @Param("contextRegionId") UUID contextRegionId,
            @Param("feedScore") Double feedScore,
            @Param("trendingScore") Double trendingScore,
            @Param("freshnessScore") Double freshnessScore,
            @Param("sameRegionScore") Double sameRegionScore,
            @Param("followedUserScore") Double followedUserScore,
            @Param("followedPageScore") Double followedPageScore,
            @Param("reportPenalty") Double reportPenalty,
            @Param("rankPosition") Integer rankPosition,
            @Param("reason") String reason,
            @Param("computedAt") LocalDateTime computedAt,
            @Param("createdAt") LocalDateTime createdAt);
}
