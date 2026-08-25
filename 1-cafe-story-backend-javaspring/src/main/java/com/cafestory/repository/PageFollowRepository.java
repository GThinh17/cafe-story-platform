package com.cafestory.repository;

import com.cafestory.entity.PageFollow;
import com.cafestory.entity.enums.PageMemberStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PageFollowRepository extends JpaRepository<PageFollow, UUID> {
    boolean existsByUserUserIdAndCafePageId(UUID userId, UUID cafePageId);

    @Query("""
            select follow.cafePage.id
            from PageFollow follow
            where follow.user.userId = :userId
            and follow.cafePage.id in :cafePageIds
            """)
    List<UUID> findFollowedCafePageIds(
            @Param("userId") UUID userId,
            @Param("cafePageIds") List<UUID> cafePageIds);

    Optional<PageFollow> findByUserUserIdAndCafePageId(UUID userId, UUID cafePageId);

    long countByUserUserId(UUID userId);

    @EntityGraph(attributePaths = {"user", "user.region", "cafePage"})
    List<PageFollow> findByCafePageId(UUID cafePageId);

    @EntityGraph(attributePaths = {"user", "cafePage", "cafePage.region", "cafePage.owner"})
    List<PageFollow> findByUserUserId(UUID userId);

    @Query("""
            select count(pf) > 0
            from PageFollow pf
            where pf.user.userId = :actorUserId
            and (
                pf.cafePage.owner.userId = :taggedUserId
                or exists (
                    select pm
                    from PageMember pm
                    where pm.cafePage = pf.cafePage
                    and pm.user.userId = :taggedUserId
                    and pm.status = :status
                )
            )
            """)
    boolean existsTaggableUserFromFollowedPages(
            @Param("actorUserId") UUID actorUserId,
            @Param("taggedUserId") UUID taggedUserId,
            @Param("status") PageMemberStatus status);
}
