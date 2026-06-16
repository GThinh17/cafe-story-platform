package com.cafestory.repository;

import com.cafestory.entity.Blog;
import com.cafestory.entity.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BlogRepository extends JpaRepository<Blog, UUID> {
    @Override
    @EntityGraph(attributePaths = {"author", "page"})
    List<Blog> findAll();

    @EntityGraph(attributePaths = {"author", "page"})
    List<Blog> findByAuthorUserId(UUID authorUserId);

    @Query("""
            select save.blog
            from BlogSave save
            where save.user.userId = :userId
            order by save.createdAt desc
            """)
    @EntityGraph(attributePaths = {"author", "page"})
    List<Blog> findSavedBlogsByUserId(@Param("userId") UUID userId);

    @Query("""
            select share.blog
            from BlogShare share
            where share.user.userId = :userId
            order by share.createdAt desc
            """)
    @EntityGraph(attributePaths = {"author", "page"})
    List<Blog> findSharedBlogsByUserId(@Param("userId") UUID userId);

    @Query("""
            select tag.blog
            from BlogTaggedUser tag
            where tag.taggedUser.userId = :userId
            order by tag.createdAt desc
            """)
    @EntityGraph(attributePaths = {"author", "page"})
    List<Blog> findTaggedBlogsByUserId(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = {"author", "page"})
    java.util.Optional<Blog> findFirstByAuthorUserIdAndStatusOrderByCreatedAtDescIdDesc(
            UUID authorUserId,
            PostStatus status);

    @Query("""
            select b
            from Blog b
            where b.page.id = :pageId
            """)
    @EntityGraph(attributePaths = {"author", "page"})
    List<Blog> findByPageId(@Param("pageId") UUID pageId);

    @EntityGraph(attributePaths = {"author", "page"})
    List<Blog> findByStatus(PostStatus status);

    @Query("""
            select b
            from Blog b
            where b.page.id = :pageId
            and b.status = com.cafestory.entity.enums.PostStatus.PUBLISHED
            and not exists (
                select 1
                from AiModerationResult moderation
                where moderation.blog.id = b.id
                and moderation.decision = com.cafestory.entity.enums.ModerationDecision.VIOLATION
            )
            order by b.createdAt desc, b.id desc
            """)
    @EntityGraph(attributePaths = {"author", "page"})
    List<Blog> findPublishedCafePageBlogsFirstPage(
            @Param("pageId") UUID pageId,
            Pageable pageable);

    @Query("""
            select b
            from Blog b
            where b.page.id = :pageId
            and b.status = com.cafestory.entity.enums.PostStatus.PUBLISHED
            and (
                b.createdAt < :afterCreatedAt
                or (b.createdAt = :afterCreatedAt and b.id < :afterId)
            )
            and not exists (
                select 1
                from AiModerationResult moderation
                where moderation.blog.id = b.id
                and moderation.decision = com.cafestory.entity.enums.ModerationDecision.VIOLATION
            )
            order by b.createdAt desc, b.id desc
            """)
    @EntityGraph(attributePaths = {"author", "page"})
    List<Blog> findPublishedCafePageBlogsAfterCursor(
            @Param("pageId") UUID pageId,
            @Param("afterCreatedAt") LocalDateTime afterCreatedAt,
            @Param("afterId") UUID afterId,
            Pageable pageable);

    long countByStatus(PostStatus status);

    @Query("""
            select b
            from Blog b
            where (:status is null or b.status = :status)
            and (:authorUserId is null or b.author.userId = :authorUserId)
            and (:pageId is null or b.page.id = :pageId)
            """)
    @EntityGraph(attributePaths = {"author", "page"})
    Page<Blog> findAdminBlogs(
            @Param("status") PostStatus status,
            @Param("authorUserId") UUID authorUserId,
            @Param("pageId") UUID pageId,
            Pageable pageable);
}
