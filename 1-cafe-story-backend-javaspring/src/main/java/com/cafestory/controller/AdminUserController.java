package com.cafestory.controller;

import com.cafestory.dto.requestDTO.AdminUserRolesUpdateRequestDTO;
import com.cafestory.dto.requestDTO.AdminUserStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.AdminUserResponseDTO;
import com.cafestory.entity.enums.UserRole;
import com.cafestory.service.serviceInterface.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public Page<AdminUserResponseDTO> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean accountStatus,
            @RequestParam(required = false) UserRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 100),
                Sort.by(Sort.Direction.ASC, "userName"));
        return adminUserService.getUsers(search, accountStatus, role, pageable);
    }

    @GetMapping("/{userId}")
    public AdminUserResponseDTO getUser(@PathVariable UUID userId) {
        return adminUserService.getUser(userId);
    }

    @PatchMapping("/{userId}/status")
    public AdminUserResponseDTO updateUserStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminUserStatusUpdateRequestDTO request) {
        return adminUserService.updateUserStatus(userId, request);
    }

    @PatchMapping("/{userId}/roles")
    public AdminUserResponseDTO updateUserRoles(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminUserRolesUpdateRequestDTO request) {
        return adminUserService.updateUserRoles(userId, request);
    }
}
