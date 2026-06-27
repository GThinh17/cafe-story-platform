package com.cafestory.repository;

import com.cafestory.entity.BlogSave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.Collection;

public interface BlogSaveRepository extends JpaRepository<BlogSave, UUID> {
    Optional<BlogSave> findByUserUserIdAndBlogId(UUID userId, UUID blogId);

    @Query("""
            select save.blog.id
            from BlogSave save
            where save.user.userId = :userId
            and save.blog.id in :blogIds
            """)
    List<UUID> findSavedBlogIdsByUserIdAndBlogIds(
            @Param("userId") UUID userId,
            @Param("blogIds") List<UUID> blogIds);

    @Query("""
            select save.blog.id as blogId, count(save.id) as count
            from BlogSave save
            where save.blog.id in :blogIds
            group by save.blog.id
            """)
    List<BlogCountRow> countSavesByBlogIds(@Param("blogIds") List<UUID> blogIds);

    List<BlogSave> findByBlogId(UUID blogId);

    List<BlogSave> findByUserUserId(UUID userId);

    long countByBlogId(UUID blogId);

    @Query("""
            select save.blog.id as blogId, count(save) as eventCount
            from BlogSave save
            where save.blog.id in :blogIds
            and save.createdAt >= :startAt
            and save.createdAt < :endAt
            group by save.blog.id
            """)
    List<BlogSaveCountRow> countByBlogIdsAndCreatedAtBetween(
            @Param("blogIds") Collection<UUID> blogIds,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);

    interface BlogCountRow {
        UUID getBlogId();

        Long getCount();
    }

    interface BlogSaveCountRow {
        UUID getBlogId();

        Long getEventCount();
    }
}
