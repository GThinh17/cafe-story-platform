package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.AdminUserRolesUpdateRequestDTO;
import com.cafestory.dto.requestDTO.AdminUserStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.AdminUserResponseDTO;
import com.cafestory.entity.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminUserService {

    Page<AdminUserResponseDTO> getUsers(String search, Boolean accountStatus, UserRole role, Pageable pageable);

    AdminUserResponseDTO getUser(UUID userId);

    AdminUserResponseDTO updateUserStatus(UUID userId, AdminUserStatusUpdateRequestDTO request);

    AdminUserResponseDTO updateUserRoles(UUID userId, AdminUserRolesUpdateRequestDTO request);
}
