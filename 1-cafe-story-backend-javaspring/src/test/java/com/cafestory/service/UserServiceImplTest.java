package com.cafestory.service;

import com.cafestory.dto.requestDTO.UserCreateDTO;
import com.cafestory.dto.requestDTO.UserUpdateDTO;
import com.cafestory.dto.responseDTO.UserResponseDTO;
import com.cafestory.entity.User;
import com.cafestory.mapper.UserMapper;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceImplement.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_success_TC001() {
        UserCreateDTO request = createUserRequest();
        User user = user();
        User savedUser = user();
        UserResponseDTO response = userResponse(savedUser.getUserId());

        when(userRepository.findByUserName(request.getUserName())).thenReturn(Optional.empty());
        when(userRepository.findByUserEmail(request.getUserEmail())).thenReturn(Optional.empty());
        when(userMapper.toUser(request)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toUserResponseDTO(savedUser)).thenReturn(response);

        UserResponseDTO result = userService.createUser(request);

        assertThat(result).isEqualTo(response);
        verify(userRepository).save(user);
    }

    @Test
    void createUser_fail_duplicateUsername_TC002() {
        UserCreateDTO request = createUserRequest();
        User existingUser = user();

        when(userRepository.findByUserName(request.getUserName())).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_fail_duplicateEmail_TC003() {
        UserCreateDTO request = createUserRequest();
        User existingUser = user();

        when(userRepository.findByUserName(request.getUserName())).thenReturn(Optional.empty());
        when(userRepository.findByUserEmail(request.getUserEmail())).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getAllUsers_success_TC004() {
        User user = user();
        UserResponseDTO response = userResponse(user.getUserId());

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toUserResponseDTO(user)).thenReturn(response);

        List<UserResponseDTO> result = userService.getAllUsers();

        assertThat(result).containsExactly(response);
    }

    @Test
    void getUserById_success_TC005() {
        User user = user();
        UserResponseDTO response = userResponse(user.getUserId());

        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDTO(user)).thenReturn(response);

        UserResponseDTO result = userService.getUserById(user.getUserId());

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getUserById_fail_notFound_TC006() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateUser_success_updateAllFields_TC007() {
        User user = user();
        UserUpdateDTO request = updateUserRequest();
        UserResponseDTO response = userResponse(user.getUserId());
        response.setUserName(request.getUserName());
        response.setUserFullName(request.getUserFullName());
        response.setUserEmail(request.getUserEmail());
        response.setUserPhone(request.getUserPhone());
        response.setUserAvatar(request.getUserAvatar());
        response.setAccountStatus(request.getAccountStatus());

        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.findByUserName(request.getUserName())).thenReturn(Optional.empty());
        when(userRepository.findByUserEmail(request.getUserEmail())).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenReturn(response);

        UserResponseDTO result = userService.updateUser(user.getUserId(), request);

        assertThat(result.getUserName()).isEqualTo("updated_luan");
        assertThat(result.getUserFullName()).isEqualTo("Updated User");
        assertThat(result.getUserEmail()).isEqualTo("updated@example.com");
        assertThat(result.getUserPhone()).isEqualTo(123456789L);
        assertThat(result.getUserAvatar()).isEqualTo("https://example.com/updated.png");
        assertThat(result.getAccountStatus()).isFalse();
        assertThat(user.getUserPassword()).isEqualTo("updated-password");
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_success_sameUsernameAndEmail_TC008() {
        User user = user();
        UserUpdateDTO request = updateUserRequest();
        request.setUserName(user.getUserName());
        request.setUserEmail(user.getUserEmail());
        UserResponseDTO response = userResponse(user.getUserId());

        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.findByUserName(request.getUserName())).thenReturn(Optional.of(user));
        when(userRepository.findByUserEmail(request.getUserEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenReturn(response);

        UserResponseDTO result = userService.updateUser(user.getUserId(), request);

        assertThat(result).isEqualTo(response);
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_success_nullFields_TC009() {
        User user = user();
        UserUpdateDTO request = new UserUpdateDTO();
        UserResponseDTO response = userResponse(user.getUserId());

        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenReturn(response);

        UserResponseDTO result = userService.updateUser(user.getUserId(), request);

        assertThat(result).isEqualTo(response);
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_fail_notFound_TC010() {
        UUID userId = UUID.randomUUID();
        UserUpdateDTO request = updateUserRequest();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_fail_duplicateUsername_TC011() {
        User user = user();
        User anotherUser = user();
        anotherUser.setUserId(UUID.randomUUID());
        UserUpdateDTO request = new UserUpdateDTO();
        request.setUserName("duplicate_user");

        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.findByUserName(request.getUserName())).thenReturn(Optional.of(anotherUser));

        assertThatThrownBy(() -> userService.updateUser(user.getUserId(), request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_fail_duplicateEmail_TC012() {
        User user = user();
        User anotherUser = user();
        anotherUser.setUserId(UUID.randomUUID());
        UserUpdateDTO request = new UserUpdateDTO();
        request.setUserEmail("duplicate@example.com");

        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.findByUserEmail(request.getUserEmail())).thenReturn(Optional.of(anotherUser));

        assertThatThrownBy(() -> userService.updateUser(user.getUserId(), request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_success_TC013() {
        User user = user();

        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));

        userService.deleteUser(user.getUserId());

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_fail_notFound_TC014() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
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

    private UserUpdateDTO updateUserRequest() {
        UserUpdateDTO request = new UserUpdateDTO();
        request.setUserName("updated_luan");
        request.setUserFullName("Updated User");
        request.setUserPassword("updated-password");
        request.setUserEmail("updated@example.com");
        request.setUserPhone(123456789L);
        request.setUserAvatar("https://example.com/updated.png");
        request.setAccountStatus(false);
        return request;
    }

    private User user() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("luan123");
        user.setUserFullName("Nguyen Van Luan");
        user.setUserPassword("123456");
        user.setUserEmail("luan123@example.com");
        user.setUserPhone(987654321L);
        user.setUserAvatar("https://example.com/avatar.png");
        user.setUserLike(0);
        user.setUserFollower(0);
        user.setAccountStatus(true);
        return user;
    }

    private UserResponseDTO userResponse(UUID userId) {
        UserResponseDTO response = new UserResponseDTO();
        response.setUserId(userId);
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
