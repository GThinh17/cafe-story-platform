package com.cafestory.repository;

import com.cafestory.entity.PageMember;
import com.cafestory.entity.PageMemberId;
import com.cafestory.entity.enums.PageMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PageMemberRepository extends JpaRepository<PageMember, PageMemberId> {
    boolean existsByCafePageIdAndUserUserId(UUID cafePageId, UUID userId);

    boolean existsByCafePageIdAndUserUserIdAndStatusAndRoleNameIn(
            UUID cafePageId,
            UUID userId,
            PageMemberStatus status,
            List<String> roleNames);

    Optional<PageMember> findByCafePageIdAndUserUserId(UUID cafePageId, UUID userId);

    List<PageMember> findByCafePageId(UUID cafePageId);

    List<PageMember> findByCafePageIdAndStatus(UUID cafePageId, PageMemberStatus status);
}
