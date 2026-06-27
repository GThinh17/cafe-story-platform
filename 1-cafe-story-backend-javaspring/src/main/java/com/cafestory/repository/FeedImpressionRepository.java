package com.cafestory.repository;

import com.cafestory.entity.FeedImpression;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface FeedImpressionRepository extends JpaRepository<FeedImpression, UUID> {

    @Query("""
            select distinct impression.blog.id
            from FeedImpression impression
            where impression.user.userId = :userId
            and impression.blog.id in :blogIds
            and impression.shownAt >= :shownAfter
            """)
    List<UUID> findSeenBlogIds(
            @Param("userId") UUID userId,
            @Param("blogIds") Collection<UUID> blogIds,
            @Param("shownAfter") LocalDateTime shownAfter);
}
