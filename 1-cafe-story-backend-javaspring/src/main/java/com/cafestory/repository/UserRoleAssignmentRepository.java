package com.cafestory.repository;

import com.cafestory.entity.UserRoleAssignment;
import com.cafestory.entity.UserRoleId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface UserRoleAssignmentRepository extends JpaRepository<UserRoleAssignment, UserRoleId> {

    boolean existsByUserUserIdAndRoleName(UUID userId, String roleName);

    @EntityGraph(attributePaths = {"role"})
    List<UserRoleAssignment> findByUserUserId(UUID userId);

    @EntityGraph(attributePaths = {"user", "role"})
    List<UserRoleAssignment> findByUserUserIdIn(Collection<UUID> userIds);

    void deleteByUserUserId(UUID userId);
}
