package com.cafestory.validation;

import com.cafestory.entity.User;
import com.cafestory.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class UserValidator {

    private final UserRepository userRepository;

    public UserValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User validateUserExists(UUID userId) {
        validateUserIdNotNull(userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public void validateUserActive(User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is required");
        }
        if (!Boolean.TRUE.equals(user.getAccountStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is inactive");
        }
    }

    private void validateUserIdNotNull(UUID userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User id is required");
        }
    }
}
