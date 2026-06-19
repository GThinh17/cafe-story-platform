package com.cafestory.repository;

import com.cafestory.entity.BlogTaggedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BlogTaggedUserRepository extends JpaRepository<BlogTaggedUser, UUID> {
    List<BlogTaggedUser> findByBlogIdOrderByCreatedAtAsc(UUID blogId);

    @Query("""
            select tag
            from BlogTaggedUser tag
            join fetch tag.blog
            join fetch tag.taggedUser
            where tag.blog.id in :blogIds
            order by tag.blog.id asc, tag.createdAt asc
            """)
    List<BlogTaggedUser> findByBlogIdInWithTaggedUser(@Param("blogIds") List<UUID> blogIds);

    List<BlogTaggedUser> findByBlogId(UUID blogId);

    void deleteByBlogId(UUID blogId);
}
