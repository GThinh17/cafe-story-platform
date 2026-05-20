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
            join fetch s.blog b
            join fetch b.author
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

    @Modifying
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
}
