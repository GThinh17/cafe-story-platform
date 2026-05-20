package com.cafestory.repository;

import com.cafestory.entity.BlogRankingOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BlogRankingOverrideRepository extends JpaRepository<BlogRankingOverride, UUID> {

    @Query("""
            select coalesce(sum(o.boostScore), 0)
            from BlogRankingOverride o
            where o.blog.id = :blogId
              and o.startAt <= :now
              and (o.endAt is null or o.endAt >= :now)
            """)
    Double sumActiveBoostScore(@Param("blogId") UUID blogId, @Param("now") LocalDateTime now);

    @Query("""
            select count(o) > 0
            from BlogRankingOverride o
            where o.blog.id = :blogId
              and o.isPinned = true
              and o.startAt <= :now
              and (o.endAt is null or o.endAt >= :now)
            """)
    boolean existsActivePinnedOverride(@Param("blogId") UUID blogId, @Param("now") LocalDateTime now);

    List<BlogRankingOverride> findByBlogIdOrderByCreatedAtDesc(UUID blogId);
}
