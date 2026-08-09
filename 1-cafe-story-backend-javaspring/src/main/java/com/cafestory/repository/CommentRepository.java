package com.cafestory.repository;

import com.cafestory.entity.Comment;
import com.cafestory.entity.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.Collection;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    @Override
    @EntityGraph(attributePaths = {"user", "actorCafePage"})
    List<Comment> findAll();

    @EntityGraph(attributePaths = {"user", "actorCafePage"})
    List<Comment> findByBlogId(UUID blogId);

    @EntityGraph(attributePaths = {"user", "actorCafePage"})
    List<Comment> findByUserUserId(UUID userId);

    @Query("""
            select distinct c.blog.id
            from Comment c
            where c.user.userId = :userId
            and c.blog.id in :blogIds
            and c.status = com.cafestory.entity.enums.PostStatus.PUBLISHED
            and c.createdAt >= :startAt
            and c.createdAt < :endAt
            """)
    List<UUID> findRecentlyCommentedBlogIds(
            @Param("userId") UUID userId,
            @Param("blogIds") Collection<UUID> blogIds,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);

    @EntityGraph(attributePaths = {"user", "actorCafePage"})
    List<Comment> findByParentCommentId(UUID parentCommentId);

    @Query("""
            select c
            from Comment c
            where c.blog.id = :blogId
            and c.id <> :targetId
            and c.status = com.cafestory.entity.enums.PostStatus.PUBLISHED
            and c.createdAt < :createdAt
            order by c.createdAt desc
            """)
    @EntityGraph(attributePaths = {"user", "actorCafePage", "parentComment"})
    List<Comment> findContextBeforeInBlog(
            @Param("blogId") UUID blogId,
            @Param("targetId") UUID targetId,
            @Param("createdAt") LocalDateTime createdAt,
            Pageable pageable);

    @Query("""
            select c
            from Comment c
            where c.blog.id = :blogId
            and c.id <> :targetId
            and c.status = com.cafestory.entity.enums.PostStatus.PUBLISHED
            and c.createdAt > :createdAt
            order by c.createdAt asc
            """)
    @EntityGraph(attributePaths = {"user", "actorCafePage", "parentComment"})
    List<Comment> findContextAfterInBlog(
            @Param("blogId") UUID blogId,
            @Param("targetId") UUID targetId,
            @Param("createdAt") LocalDateTime createdAt,
            Pageable pageable);

    long countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    List<Comment> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime startDate, LocalDateTime endDate);

    long countByBlogIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(UUID blogId, LocalDateTime startDate, LocalDateTime endDate);

    long countByStatus(PostStatus status);

    @Query("""
            select c
            from Comment c
            where (:status is null or c.status = :status)
            and (:blogId is null or c.blog.id = :blogId)
            and (:userId is null or c.user.userId = :userId)
            """)
    @EntityGraph(attributePaths = {"user", "actorCafePage"})
    Page<Comment> findAdminComments(
            @Param("status") PostStatus status,
            @Param("blogId") UUID blogId,
            @Param("userId") UUID userId,
            Pageable pageable);

    @Query("""
            select c.blog.id as blogId, count(c) as eventCount
            from Comment c
            where c.blog.id in :blogIds
            and c.parentComment is null
            and c.status = com.cafestory.entity.enums.PostStatus.PUBLISHED
            and c.createdAt >= :startAt
            and c.createdAt < :endAt
            group by c.blog.id
            """)
    List<CommentCountRow> countRootCommentsByBlogIdsAndCreatedAtBetween(
            @Param("blogIds") Collection<UUID> blogIds,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);

    @Query("""
            select c.blog.id as blogId, count(c) as eventCount
            from Comment c
            where c.blog.id in :blogIds
            and c.parentComment is not null
            and c.status = com.cafestory.entity.enums.PostStatus.PUBLISHED
            and c.createdAt >= :startAt
            and c.createdAt < :endAt
            group by c.blog.id
            """)
    List<CommentCountRow> countRepliesByBlogIdsAndCreatedAtBetween(
            @Param("blogIds") Collection<UUID> blogIds,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);

    interface CommentCountRow {
        UUID getBlogId();

        Long getEventCount();
    }

    // Chống quét toàn bảng: cũ = findAll() toàn bộ comments rồi lọc bằng Java,
    // mới = 1 query gom theo tác giả blog, trả luôn cả tổng và tổng gần đây.
    @Query("""
            select b.author.userId as authorUserId,
                   count(c) as totalCount,
                   sum(case when c.createdAt >= :recentStart and c.createdAt < :recentEnd
                            then 1L else 0L end) as recentCount
            from Comment c
            join c.blog b
            where b.status = com.cafestory.entity.enums.PostStatus.PUBLISHED
            and b.author is not null
            group by b.author.userId
            """)
    List<AuthorEngagementCountRow> countByBlogAuthor(
            @Param("recentStart") LocalDateTime recentStart,
            @Param("recentEnd") LocalDateTime recentEnd);

    // Engagement tác giả NHẬN được trong khoảng [startAt, endAt). Dùng cho
    // income/ranking/payout — chiều ngược với countByUserAndCreatedAtBetween.
    // Tác giả tự bình luận dưới bài mình không tính.
    @Query("""
            select b.author.userId as authorUserId, count(c) as eventCount
            from Comment c
            join c.blog b
            where b.status = com.cafestory.entity.enums.PostStatus.PUBLISHED
            and b.author is not null
            and c.user.userId <> b.author.userId
            and c.createdAt >= :startAt
            and c.createdAt < :endAt
            group by b.author.userId
            """)
    List<AuthorInteractionCountRow> countByBlogAuthorBetween(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);
}
