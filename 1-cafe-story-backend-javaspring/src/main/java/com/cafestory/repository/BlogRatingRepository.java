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

    @Query("""
            select rating.blog.id as blogId, rating.rating as rating
            from BlogRating rating
            where rating.user.userId = :userId
            and rating.blog.id in :blogIds
            """)
    List<BlogUserRatingRow> findUserRatingsByUserIdAndBlogIds(
            @Param("userId") UUID userId,
            @Param("blogIds") List<UUID> blogIds);

    List<BlogRating> findByBlogId(UUID blogId);

    List<BlogRating> findByUserUserId(UUID userId);

    long countByBlogId(UUID blogId);

    @Query("""
            select rating.blog.id as blogId, avg(rating.rating) as averageRating, count(rating.id) as ratingCount
            from BlogRating rating
            where rating.blog.id in :blogIds
            group by rating.blog.id
            """)
    List<BlogRatingSummaryRow> findRatingSummariesByBlogIds(@Param("blogIds") List<UUID> blogIds);

    @Query("select avg(r.rating) from BlogRating r where r.blog.id = :blogId")
    Double findAverageRatingByBlogId(@Param("blogId") UUID blogId);

    interface BlogUserRatingRow {
        UUID getBlogId();

        Integer getRating();
    }

    interface BlogRatingSummaryRow {
        UUID getBlogId();

        Double getAverageRating();

        Long getRatingCount();
    }
}
