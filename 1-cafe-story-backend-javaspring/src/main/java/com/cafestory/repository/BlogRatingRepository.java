package com.cafestory.repository;

import com.cafestory.entity.BlogRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlogRatingRepository extends JpaRepository<BlogRating, UUID> {
    Optional<BlogRating> findByUserUserIdAndBlogId(UUID userId, UUID blogId);

    List<BlogRating> findByBlogId(UUID blogId);

    List<BlogRating> findByUserUserId(UUID userId);

    long countByBlogId(UUID blogId);

    @Query("select avg(r.rating) from BlogRating r where r.blog.id = :blogId")
    Double findAverageRatingByBlogId(@Param("blogId") UUID blogId);
}
