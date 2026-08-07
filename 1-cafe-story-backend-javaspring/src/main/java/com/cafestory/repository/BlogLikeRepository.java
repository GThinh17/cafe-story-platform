package com.cafestory.repository;

import com.cafestory.entity.BlogLike;
import com.cafestory.entity.enums.ActorContextType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.Collection;

public interface BlogLikeRepository extends JpaRepository<BlogLike, UUID> {
    boolean existsByUserUserIdAndBlogId(UUID userId, UUID blogId);

    Optional<BlogLike> findByUserUserIdAndBlogId(UUID userId, UUID blogId);

    @Query("""
            select count(like) > 0
            from BlogLike like
            where like.user.userId = :userId
            and like.blog.id = :blogId
            and like.actorContextType = :actorContextType
            and (
                (:actorCafePageId is null and like.actorCafePage is null)
                or like.actorCafePage.id = :actorCafePageId
            )
            """)
    boolean existsByActor(
            @Param("userId") UUID userId,
            @Param("blogId") UUID blogId,
            @Param("actorContextType") ActorContextType actorContextType,
            @Param("actorCafePageId") UUID actorCafePageId);

    @Query("""
            select like
            from BlogLike like
            where like.user.userId = :userId
            and like.blog.id = :blogId
            and like.actorContextType = :actorContextType
            and (
                (:actorCafePageId is null and like.actorCafePage is null)
                or like.actorCafePage.id = :actorCafePageId
            )
            """)
    Optional<BlogLike> findByActor(
            @Param("userId") UUID userId,
            @Param("blogId") UUID blogId,
            @Param("actorContextType") ActorContextType actorContextType,
            @Param("actorCafePageId") UUID actorCafePageId);

    @Query("""
            select like.blog.id
            from BlogLike like
            where like.user.userId = :userId
            and like.actorContextType = com.cafestory.entity.enums.ActorContextType.USER
            and like.blog.id in :blogIds
            """)
    List<UUID> findLikedBlogIdsByUserIdAndBlogIds(
            @Param("userId") UUID userId,
            @Param("blogIds") List<UUID> blogIds);

    @Query("""
            select distinct like.blog.id
            from BlogLike like
            where like.user.userId = :userId
            and like.blog.id in :blogIds
            and like.createdAt >= :startAt
            and like.createdAt < :endAt
            """)
    List<UUID> findRecentlyLikedBlogIds(
            @Param("userId") UUID userId,
            @Param("blogIds") Collection<UUID> blogIds,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);

    @EntityGraph(attributePaths = {"user", "actorCafePage"})
    List<BlogLike> findByBlogId(UUID blogId);

    @EntityGraph(attributePaths = {"user", "actorCafePage"})
    List<BlogLike> findByUserUserId(UUID userId);

    long countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    List<BlogLike> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime startDate, LocalDateTime endDate);

    long countByBlogIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(UUID blogId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("""
            select like.blog.id as blogId, count(like) as eventCount
            from BlogLike like
            where like.blog.id in :blogIds
            and like.createdAt >= :startAt
            and like.createdAt < :endAt
            group by like.blog.id
            """)
    List<BlogInteractionCountRow> countByBlogIdsAndCreatedAtBetween(
            @Param("blogIds") Collection<UUID> blogIds,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);

    interface BlogInteractionCountRow {
        UUID getBlogId();

        Long getEventCount();
    }

    // Chống quét toàn bảng: cũ = findAll() toàn bộ blog_likes rồi lọc bằng Java,
    // mới = 1 query gom theo tác giả, trả luôn cả tổng và tổng gần đây.
    @Query("""
            select b.author.userId as authorUserId,
                   count(like) as totalCount,
                   sum(case when like.createdAt >= :recentStart and like.createdAt < :recentEnd
                            then 1L else 0L end) as recentCount
            from BlogLike like
            join like.blog b
            where b.status = com.cafestory.entity.enums.PostStatus.PUBLISHED
            and b.author is not null
            group by b.author.userId
            """)
    List<AuthorEngagementCountRow> countByBlogAuthor(
            @Param("recentStart") LocalDateTime recentStart,
            @Param("recentEnd") LocalDateTime recentEnd);

    // Engagement tác giả NHẬN được trong khoảng [startAt, endAt). Dùng cho
    // income/ranking/payout — chiều ngược với countByUserAndCreatedAtBetween.
    // Tự like bài mình không tính.
    @Query("""
            select b.author.userId as authorUserId, count(like) as eventCount
            from BlogLike like
            join like.blog b
            where b.status = com.cafestory.entity.enums.PostStatus.PUBLISHED
            and b.author is not null
            and like.user.userId <> b.author.userId
            and like.createdAt >= :startAt
            and like.createdAt < :endAt
            group by b.author.userId
            """)
    List<AuthorInteractionCountRow> countByBlogAuthorBetween(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);
}
