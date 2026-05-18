package com.cafestory.validation;

import com.cafestory.entity.User;
import com.cafestory.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserValidator userValidator;

    @Test
    void validateUserExists_success_TC001() {
        User user = user();

        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));

        User result = userValidator.validateUserExists(user.getUserId());

        assertThat(result).isEqualTo(user);
    }

    @Test
    void validateUserExists_fail_nullUserId_TC002() {
        assertThatThrownBy(() -> userValidator.validateUserExists(null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void validateUserExists_fail_userNotFound_TC003() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userValidator.validateUserExists(userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void validateUserActive_success_TC004() {
        User user = user();

        userValidator.validateUserActive(user);

        assertThat(user.getAccountStatus()).isTrue();
    }

    @Test
    void validateUserActive_fail_nullUser_TC005() {
        assertThatThrownBy(() -> userValidator.validateUserActive(null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("User is required"));
    }

    @Test
    void validateUserActive_fail_inactiveUser_TC006() {
        User user = user();
        user.setAccountStatus(false);

        assertThatThrownBy(() -> userValidator.validateUserActive(user))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("User account is inactive"));
    }

    private User user() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("luan123");
        user.setUserFullName("Nguyen Van Luan");
        user.setUserPassword("123456");
        user.setUserEmail("luan123@example.com");
        user.setAccountStatus(true);
        return user;
    }
}
