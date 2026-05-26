package com.cafestory.repository;

import com.cafestory.entity.Blog;
import com.cafestory.entity.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BlogRepository extends JpaRepository<Blog, UUID> {
    List<Blog> findByAuthorUserId(UUID authorUserId);

    List<Blog> findByPageId(UUID pageId);

    List<Blog> findByStatus(PostStatus status);

    long countByStatus(PostStatus status);

    @Query("""
            select b
            from Blog b
            where (:status is null or b.status = :status)
            and (:authorUserId is null or b.author.userId = :authorUserId)
            and (:pageId is null or b.pageId = :pageId)
            """)
    Page<Blog> findAdminBlogs(
            @Param("status") PostStatus status,
            @Param("authorUserId") UUID authorUserId,
            @Param("pageId") UUID pageId,
            Pageable pageable);
}
