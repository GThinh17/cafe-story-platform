package com.cafestory.repository;

import com.cafestory.entity.BlogEvent;
import com.cafestory.entity.enums.BlogEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BlogEventRepository extends JpaRepository<BlogEvent, UUID> {
    long countByBlogIdAndEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            UUID blogId,
            BlogEventType eventType,
            LocalDateTime startAt,
            LocalDateTime endAt);

    @Query("""
            select event.blog.id as blogId, count(event) as eventCount
            from BlogEvent event
            where event.blog.id in :blogIds
            and event.eventType = :eventType
            and event.createdAt >= :startAt
            and event.createdAt < :endAt
            group by event.blog.id
            """)
    List<BlogEventCountRow> countByBlogIdsAndEventTypeAndCreatedAtBetween(
            @Param("blogIds") Collection<UUID> blogIds,
            @Param("eventType") BlogEventType eventType,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);

    List<BlogEvent> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime startAt, LocalDateTime endAt);

    interface BlogEventCountRow {
        UUID getBlogId();

        Long getEventCount();
    }
}
