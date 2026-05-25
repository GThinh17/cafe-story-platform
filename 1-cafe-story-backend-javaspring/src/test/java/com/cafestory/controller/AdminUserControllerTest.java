package com.cafestory.controller;

import com.cafestory.config.GlobalResponseAdvice;
import com.cafestory.dto.requestDTO.AdminUserRolesUpdateRequestDTO;
import com.cafestory.dto.requestDTO.AdminUserStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.AdminUserResponseDTO;
import com.cafestory.entity.enums.UserRole;
import com.cafestory.service.serviceInterface.AdminUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private AdminUserService adminUserService;

    @InjectMocks
    private AdminUserController adminUserController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(adminUserController)
                .setControllerAdvice(new GlobalResponseAdvice(objectMapper))
                .build();
    }

    @Test
    void getUsers_success_TC001() {
        AdminUserResponseDTO response = response();
        when(adminUserService.getUsers(eq("luan"), eq(true), eq(UserRole.USER), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        Page<AdminUserResponseDTO> result =
                adminUserController.getUsers("luan", true, UserRole.USER, 0, 20);

        assertThat(result.getContent()).containsExactly(response);
        verify(adminUserService).getUsers(eq("luan"), eq(true), eq(UserRole.USER), any(Pageable.class));
    }

    @Test
    void getUser_fail_notFound_TC002() throws Exception {
        UUID userId = UUID.randomUUID();
        when(adminUserService.getUser(userId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(get("/api/admin/users/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void updateUserStatus_success_TC003() {
        UUID userId = UUID.randomUUID();
        AdminUserStatusUpdateRequestDTO request = new AdminUserStatusUpdateRequestDTO();
        request.setAccountStatus(false);
        AdminUserResponseDTO response = response();
        response.setAccountStatus(false);
        when(adminUserService.updateUserStatus(userId, request)).thenReturn(response);

        AdminUserResponseDTO result = adminUserController.updateUserStatus(userId, request);

        assertThat(result.getAccountStatus()).isFalse();
        verify(adminUserService).updateUserStatus(userId, request);
    }

    @Test
    void updateUserStatus_fail_invalidRequest_TC004() throws Exception {
        mockMvc.perform(patch("/api/admin/users/{userId}/status", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserRoles_success_TC005() {
        UUID userId = UUID.randomUUID();
        AdminUserRolesUpdateRequestDTO request = new AdminUserRolesUpdateRequestDTO();
        request.setRoles(Set.of(UserRole.USER, UserRole.REVIEWER));
        AdminUserResponseDTO response = response();
        response.setRoles(List.of(UserRole.USER, UserRole.REVIEWER));
        when(adminUserService.updateUserRoles(userId, request)).thenReturn(response);

        AdminUserResponseDTO result = adminUserController.updateUserRoles(userId, request);

        assertThat(result.getRoles()).containsExactly(UserRole.USER, UserRole.REVIEWER);
        verify(adminUserService).updateUserRoles(userId, request);
    }

    private AdminUserResponseDTO response() {
        AdminUserResponseDTO response = new AdminUserResponseDTO();
        response.setUserId(UUID.randomUUID());
        response.setUserName("luan123");
        response.setUserFullName("Nguyen Van Luan");
        response.setUserEmail("luan123@example.com");
        response.setAccountStatus(true);
        response.setRoles(List.of(UserRole.USER));
        return response;
    }
}
