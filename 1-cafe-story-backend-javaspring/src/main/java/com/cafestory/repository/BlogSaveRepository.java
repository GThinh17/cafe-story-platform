package com.cafestory.repository;

import com.cafestory.entity.BlogSave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    interface BlogCountRow {
        UUID getBlogId();

        Long getCount();
    }
}
