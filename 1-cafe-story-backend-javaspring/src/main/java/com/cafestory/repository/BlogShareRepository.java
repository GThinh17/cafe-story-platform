package com.cafestory.repository;

import com.cafestory.entity.BlogShare;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.Collection;

public interface BlogShareRepository extends JpaRepository<BlogShare, UUID> {
    @EntityGraph(attributePaths = {"user", "actorCafePage"})
    List<BlogShare> findByBlogId(UUID blogId);

    @EntityGraph(attributePaths = {"user", "actorCafePage"})
    List<BlogShare> findByUserUserId(UUID userId);

    // Nothing stops a user from sharing the same blog twice, so this returns a list
    // rather than an Optional — unsharing clears every one of them.
    List<BlogShare> findByUserUserIdAndBlogId(UUID userId, UUID blogId);

    @Query("""
            select distinct share.blog.id
            from BlogShare share
            where share.user.userId = :userId
            and share.blog.id in :blogIds
            and share.createdAt >= :startAt
            and share.createdAt < :endAt
            """)
    List<UUID> findRecentlySharedBlogIds(
            @Param("userId") UUID userId,
            @Param("blogIds") Collection<UUID> blogIds,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);

    long countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("""
            select count(share)
            from BlogShare share
            join share.blog b
            where b.status = com.cafestory.entity.enums.PostStatus.PUBLISHED
            and b.author.userId = :authorUserId
            and share.user.userId <> b.author.userId
            and share.createdAt >= :startAt
            and share.createdAt < :endAt
            """)
    long countByBlogAuthorUserIdBetween(
            @Param("authorUserId") UUID authorUserId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);

    List<BlogShare> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime startDate, LocalDateTime endDate);

    long countByBlogIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(UUID blogId, LocalDateTime startDate, LocalDateTime endDate);

    @org.springframework.data.jpa.repository.Query("""
            select share.blog.id as blogId, count(share) as eventCount
            from BlogShare share
            where share.blog.id in :blogIds
            and share.createdAt >= :startAt
            and share.createdAt < :endAt
            group by share.blog.id
            """)
    List<BlogShareCountRow> countByBlogIdsAndCreatedAtBetween(
            @org.springframework.data.repository.query.Param("blogIds") Collection<UUID> blogIds,
            @org.springframework.data.repository.query.Param("startAt") LocalDateTime startAt,
            @org.springframework.data.repository.query.Param("endAt") LocalDateTime endAt);

    interface BlogShareCountRow {
        UUID getBlogId();

        Long getEventCount();
    }

    // Chống quét toàn bảng: cũ = findAll() toàn bộ blog_shares rồi lọc bằng Java,
    // mới = 1 query gom theo tác giả, trả luôn cả tổng và tổng gần đây.
    @Query("""
            select b.author.userId as authorUserId,
                   count(share) as totalCount,
                   sum(case when share.createdAt >= :recentStart and share.createdAt < :recentEnd
                            then 1L else 0L end) as recentCount
            from BlogShare share
            join share.blog b
            where b.status = com.cafestory.entity.enums.PostStatus.PUBLISHED
            and b.author is not null
            group by b.author.userId
            """)
    List<AuthorEngagementCountRow> countByBlogAuthor(
            @Param("recentStart") LocalDateTime recentStart,
            @Param("recentEnd") LocalDateTime recentEnd);

    // Engagement tác giả NHẬN được trong khoảng [startAt, endAt). Dùng cho
    // income/ranking/payout — chiều ngược với countByUserAndCreatedAtBetween.
    // Tự share bài mình không tính.
    @Query("""
            select b.author.userId as authorUserId, count(share) as eventCount
            from BlogShare share
            join share.blog b
            where b.status = com.cafestory.entity.enums.PostStatus.PUBLISHED
            and b.author is not null
            and share.user.userId <> b.author.userId
            and share.createdAt >= :startAt
            and share.createdAt < :endAt
            group by b.author.userId
            """)
    List<AuthorInteractionCountRow> countByBlogAuthorBetween(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);
}
