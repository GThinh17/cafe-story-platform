package com.cafestory.repository;

import com.cafestory.entity.CafePageRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CafePageRatingRepository extends JpaRepository<CafePageRating, UUID> {
    Optional<CafePageRating> findByUserUserIdAndCafePageId(UUID userId, UUID cafePageId);

    List<CafePageRating> findByCafePageId(UUID cafePageId);

    List<CafePageRating> findByUserUserId(UUID userId);

    long countByCafePageId(UUID cafePageId);

    @Query("select avg(r.rating) from CafePageRating r where r.cafePage.id = :cafePageId")
    Double findAverageRatingByCafePageId(@Param("cafePageId") UUID cafePageId);

    @Query("""
            select r.cafePage.id as cafePageId, avg(r.rating) as avgRating, count(r) as ratingCount
            from CafePageRating r
            where r.cafePage.id in :cafePageIds
            group by r.cafePage.id
            """)
    List<CafePageRatingSummaryRow> summarizeByCafePageIds(@Param("cafePageIds") java.util.Collection<UUID> cafePageIds);

    interface CafePageRatingSummaryRow {
        UUID getCafePageId();

        Double getAvgRating();

        Long getRatingCount();
    }
}
