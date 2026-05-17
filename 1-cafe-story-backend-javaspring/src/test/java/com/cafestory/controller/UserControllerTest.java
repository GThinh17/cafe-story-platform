package com.cafestory.controller;

import com.cafestory.dto.requestDTO.UserCreateDTO;
import com.cafestory.dto.requestDTO.UserUpdateDTO;
import com.cafestory.dto.responseDTO.UserResponseDTO;
import com.cafestory.service.serviceInterface.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void createUser_success_TC001() {
        UserCreateDTO request = createUserRequest();
        UserResponseDTO response = userResponse();

        when(userService.createUser(request)).thenReturn(response);

        UserResponseDTO result = userController.createUser(request);

        assertThat(result).isEqualTo(response);
        verify(userService).createUser(request);
    }

    @Test
    void getAllUsers_success_TC002() {
        List<UserResponseDTO> response = List.of(userResponse());

        when(userService.getAllUsers()).thenReturn(response);

        List<UserResponseDTO> result = userController.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result).isEqualTo(response);
        verify(userService).getAllUsers();
    }

    @Test
    void getUserById_success_TC003() {
        UUID userId = UUID.randomUUID();
        UserResponseDTO response = userResponse();

        when(userService.getUserById(userId)).thenReturn(response);

        UserResponseDTO result = userController.getUserById(userId);

        assertThat(result).isEqualTo(response);
        verify(userService).getUserById(userId);
    }

    @Test
    void updateUser_success_TC004() {
        UUID userId = UUID.randomUUID();
        UserUpdateDTO request = new UserUpdateDTO();
        request.setUserFullName("Updated User");
        UserResponseDTO response = userResponse();
        response.setUserFullName("Updated User");

        when(userService.updateUser(userId, request)).thenReturn(response);

        UserResponseDTO result = userController.updateUser(userId, request);

        assertThat(result.getUserFullName()).isEqualTo("Updated User");
        verify(userService).updateUser(userId, request);
    }

    @Test
    void deleteUser_success_TC005() {
        UUID userId = UUID.randomUUID();

        userController.deleteUser(userId);

        verify(userService).deleteUser(userId);
    }

    private UserCreateDTO createUserRequest() {
        UserCreateDTO request = new UserCreateDTO();
        request.setUserName("luan123");
        request.setUserFullName("Nguyen Van Luan");
        request.setUserPassword("123456");
        request.setUserEmail("luan123@example.com");
        request.setUserPhone(987654321L);
        request.setUserAvatar("https://example.com/avatar.png");
        return request;
    }

    private UserResponseDTO userResponse() {
        UserResponseDTO response = new UserResponseDTO();
        response.setUserId(UUID.randomUUID());
        response.setUserName("luan123");
        response.setUserFullName("Nguyen Van Luan");
        response.setUserEmail("luan123@example.com");
        response.setUserPhone(987654321L);
        response.setUserAvatar("https://example.com/avatar.png");
        response.setUserLike(0);
        response.setUserFollower(0);
        response.setAccountStatus(true);
        return response;
    }
}
