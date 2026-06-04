package com.cafestory.repository;

import com.cafestory.entity.BlogTaggedUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BlogTaggedUserRepository extends JpaRepository<BlogTaggedUser, UUID> {
    List<BlogTaggedUser> findByBlogIdOrderByCreatedAtAsc(UUID blogId);

    List<BlogTaggedUser> findByBlogId(UUID blogId);

    void deleteByBlogId(UUID blogId);
}
