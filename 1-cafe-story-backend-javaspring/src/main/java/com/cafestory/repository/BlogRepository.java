package com.cafestory.repository;

import com.cafestory.entity.Blog;
import com.cafestory.entity.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BlogRepository extends JpaRepository<Blog, UUID> {
    @Override
    @EntityGraph(attributePaths = {"author", "page"})
    List<Blog> findAll();

    @EntityGraph(attributePaths = {"author", "page"})
    List<Blog> findByAuthorUserId(UUID authorUserId);

    @Query("""
            select b
            from Blog b
            where b.page.id = :pageId
            """)
    @EntityGraph(attributePaths = {"author", "page"})
    List<Blog> findByPageId(@Param("pageId") UUID pageId);

    @EntityGraph(attributePaths = {"author", "page"})
    List<Blog> findByStatus(PostStatus status);

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
