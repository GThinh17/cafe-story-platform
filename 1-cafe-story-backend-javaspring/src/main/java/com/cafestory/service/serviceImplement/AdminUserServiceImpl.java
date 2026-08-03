package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminUserRolesUpdateRequestDTO;
import com.cafestory.dto.requestDTO.AdminUserStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.AdminUserResponseDTO;
import com.cafestory.entity.Role;
import com.cafestory.entity.User;
import com.cafestory.entity.UserRoleAssignment;
import com.cafestory.entity.enums.UserRole;
import com.cafestory.mapper.AdminUserMapper;
import com.cafestory.repository.RoleRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.repository.UserRoleAssignmentRepository;
import com.cafestory.service.serviceInterface.AdminUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final RoleRepository roleRepository;
    private final AdminUserMapper adminUserMapper;

    public AdminUserServiceImpl(
            UserRepository userRepository,
            UserRoleAssignmentRepository userRoleAssignmentRepository,
            RoleRepository roleRepository,
            AdminUserMapper adminUserMapper) {
        this.userRepository = userRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.roleRepository = roleRepository;
        this.adminUserMapper = adminUserMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserResponseDTO> getUsers(
            String search,
            Boolean accountStatus,
            UserRole role,
            Pageable pageable) {
        String normalizedSearch = normalize(search);
        String roleName = role == null ? null : role.name();
        Page<User> users = userRepository.findAdminUsers(normalizedSearch, accountStatus, roleName, pageable);
        List<UUID> userIds = users.stream().map(User::getUserId).toList();
        Map<UUID, List<UserRoleAssignment>> assignmentsByUserId = userIds.isEmpty()
                ? Map.of()
                : userRoleAssignmentRepository.findByUserUserIdIn(userIds).stream()
                        .collect(Collectors.groupingBy(assignment -> assignment.getUser().getUserId()));
        return users.map(user -> adminUserMapper.toAdminUserResponseDTO(
                user,
                assignmentsByUserId.getOrDefault(user.getUserId(), List.of())));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponseDTO getUser(UUID userId) {
        return toResponse(findUser(userId));
    }

    @Override
    @Transactional
    public AdminUserResponseDTO updateUserStatus(UUID userId, AdminUserStatusUpdateRequestDTO request) {
        User user = findUser(userId);
        user.setAccountStatus(request.getAccountStatus());
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public AdminUserResponseDTO updateUserRoles(UUID userId, AdminUserRolesUpdateRequestDTO request) {
        User user = findUser(userId);
        List<Role> roles = request.getRoles().stream()
                .map(role -> roleRepository.findByName(role.name())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Role is not configured: " + role.name())))
                .toList();

        userRoleAssignmentRepository.deleteByUserUserId(userId);

        roles.forEach(roleEntity -> {
            UserRoleAssignment assignment = new UserRoleAssignment();
            assignment.setUser(user);
            assignment.setRole(roleEntity);
            userRoleAssignmentRepository.save(assignment);
        });

        return toResponse(user);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private AdminUserResponseDTO toResponse(User user) {
        return adminUserMapper.toAdminUserResponseDTO(
                user,
                userRoleAssignmentRepository.findByUserUserId(user.getUserId()));
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
