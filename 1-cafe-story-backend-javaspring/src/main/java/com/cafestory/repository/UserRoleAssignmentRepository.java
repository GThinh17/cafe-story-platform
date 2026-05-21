package com.cafestory.repository;

import com.cafestory.entity.UserRoleAssignment;
import com.cafestory.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRoleAssignmentRepository extends JpaRepository<UserRoleAssignment, UserRoleId> {

    boolean existsByUserUserIdAndRoleName(UUID userId, String roleName);

    List<UserRoleAssignment> findByUserUserId(UUID userId);
}
