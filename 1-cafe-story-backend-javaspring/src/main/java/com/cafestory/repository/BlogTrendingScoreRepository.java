package com.cafestory.repository;

import com.cafestory.entity.BlogTrendingScore;
import com.cafestory.entity.enums.TrendWindowType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BlogTrendingScoreRepository extends JpaRepository<BlogTrendingScore, UUID> {
    @EntityGraph(attributePaths = {"blog", "blog.author", "blog.page"})
    List<BlogTrendingScore> findByWindowTypeAndComputedAtOrderByRankPositionAsc(
            TrendWindowType windowType,
            LocalDateTime computedAt,
            Pageable pageable);

    @EntityGraph(attributePaths = {"blog", "blog.author", "blog.page"})
    List<BlogTrendingScore> findByWindowTypeAndComputedAtOrderByRankPositionAsc(
            TrendWindowType windowType,
            LocalDateTime computedAt);

    void deleteByWindowTypeAndComputedAt(TrendWindowType windowType, LocalDateTime computedAt);

    @Query("select max(s.computedAt) from BlogTrendingScore s where s.windowType = :windowType")
    LocalDateTime findLatestComputedAt(@Param("windowType") TrendWindowType windowType);
}
