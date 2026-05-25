package com.cafestory.service;

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
import com.cafestory.service.serviceImplement.AdminUserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AdminUserMapper adminUserMapper;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    @Test
    void getUsers_success_withFilters_TC001() {
        User user = user();
        AdminUserResponseDTO response = response(user.getUserId());
        PageRequest pageable = PageRequest.of(0, 20);
        List<UserRoleAssignment> assignments = List.of(roleAssignment(user, role(UserRole.USER)));

        when(userRepository.findAdminUsers("luan", true, UserRole.USER.name(), pageable))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));
        when(userRoleAssignmentRepository.findByUserUserId(user.getUserId())).thenReturn(assignments);
        when(adminUserMapper.toAdminUserResponseDTO(user, assignments)).thenReturn(response);

        Page<AdminUserResponseDTO> result =
                adminUserService.getUsers(" luan ", true, UserRole.USER, pageable);

        assertThat(result.getContent()).containsExactly(response);
        verify(userRepository).findAdminUsers("luan", true, UserRole.USER.name(), pageable);
    }

    @Test
    void getUser_fail_notFound_TC002() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.getUser(userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateUserStatus_success_TC003() {
        User user = user();
        AdminUserStatusUpdateRequestDTO request = new AdminUserStatusUpdateRequestDTO();
        request.setAccountStatus(false);
        AdminUserResponseDTO response = response(user.getUserId());
        response.setAccountStatus(false);

        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userRoleAssignmentRepository.findByUserUserId(user.getUserId())).thenReturn(List.of());
        when(adminUserMapper.toAdminUserResponseDTO(user, List.of())).thenReturn(response);

        AdminUserResponseDTO result = adminUserService.updateUserStatus(user.getUserId(), request);

        assertThat(user.getAccountStatus()).isFalse();
        assertThat(result.getAccountStatus()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void updateUserRoles_success_replacesRoles_TC004() {
        User user = user();
        Role adminRole = role(UserRole.ADMIN);
        AdminUserRolesUpdateRequestDTO request = new AdminUserRolesUpdateRequestDTO();
        request.setRoles(Set.of(UserRole.ADMIN));
        AdminUserResponseDTO response = response(user.getUserId());
        response.setRoles(List.of(UserRole.ADMIN));

        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(roleRepository.findByName(UserRole.ADMIN.name())).thenReturn(Optional.of(adminRole));
        when(userRoleAssignmentRepository.findByUserUserId(user.getUserId())).thenReturn(List.of(roleAssignment(user, adminRole)));
        when(adminUserMapper.toAdminUserResponseDTO(any(User.class), any())).thenReturn(response);

        AdminUserResponseDTO result = adminUserService.updateUserRoles(user.getUserId(), request);

        assertThat(result.getRoles()).containsExactly(UserRole.ADMIN);
        verify(userRoleAssignmentRepository).deleteByUserUserId(user.getUserId());
        verify(userRoleAssignmentRepository).save(any(UserRoleAssignment.class));
    }

    @Test
    void updateUserRoles_fail_roleNotConfigured_TC005() {
        User user = user();
        AdminUserRolesUpdateRequestDTO request = new AdminUserRolesUpdateRequestDTO();
        request.setRoles(Set.of(UserRole.REVIEWER));

        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(roleRepository.findByName(UserRole.REVIEWER.name())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.updateUserRoles(user.getUserId(), request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(userRoleAssignmentRepository, never()).deleteByUserUserId(user.getUserId());
        verify(userRoleAssignmentRepository, never()).save(any(UserRoleAssignment.class));
    }

    private User user() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("luan123");
        user.setUserFullName("Nguyen Van Luan");
        user.setUserEmail("luan123@example.com");
        user.setAccountStatus(true);
        user.setUserLike(0);
        user.setUserFollower(0);
        return user;
    }

    private Role role(UserRole userRole) {
        Role role = new Role();
        role.setId(userRole.ordinal() + 1);
        role.setName(userRole.name());
        return role;
    }

    private UserRoleAssignment roleAssignment(User user, Role role) {
        UserRoleAssignment assignment = new UserRoleAssignment();
        assignment.setUser(user);
        assignment.setRole(role);
        return assignment;
    }

    private AdminUserResponseDTO response(UUID userId) {
        AdminUserResponseDTO response = new AdminUserResponseDTO();
        response.setUserId(userId);
        response.setUserName("luan123");
        response.setUserFullName("Nguyen Van Luan");
        response.setUserEmail("luan123@example.com");
        response.setAccountStatus(true);
        response.setRoles(List.of(UserRole.USER));
        return response;
    }
}
