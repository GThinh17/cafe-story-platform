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
            select distinct save.blog.id
            from BlogSave save
            where save.user.userId = :userId
            and save.blog.id in :blogIds
            and save.createdAt >= :startAt
            and save.createdAt < :endAt
            """)
    List<UUID> findRecentlySavedBlogIds(
            @Param("userId") UUID userId,
            @Param("blogIds") Collection<UUID> blogIds,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);

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

    // Chống quét toàn bảng: cũ = findAll() toàn bộ blog_saves rồi lọc bằng Java,
    // mới = 1 query gom theo tác giả, trả luôn cả tổng và tổng gần đây.
    @Query("""
            select b.author.userId as authorUserId,
                   count(save) as totalCount,
                   sum(case when save.createdAt >= :recentStart and save.createdAt < :recentEnd
                            then 1L else 0L end) as recentCount
            from BlogSave save
            join save.blog b
            where b.status = com.cafestory.entity.enums.PostStatus.PUBLISHED
            and b.author is not null
            group by b.author.userId
            """)
    List<AuthorEngagementCountRow> countByBlogAuthor(
            @Param("recentStart") LocalDateTime recentStart,
            @Param("recentEnd") LocalDateTime recentEnd);
}
