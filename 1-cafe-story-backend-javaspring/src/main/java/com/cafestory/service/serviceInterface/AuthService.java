package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.LoginRequest;
import com.cafestory.dto.requestDTO.RegisterRequest;
import com.cafestory.dto.responseDTO.AuthResponse;
import com.cafestory.dto.responseDTO.UsernameSuggestionResponse;

import java.util.UUID;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse getCurrentUser(UUID userId);

    UsernameSuggestionResponse suggestUserNames(String fullName);

    AuthResponse refresh(String refreshToken);

    void logout(String refreshToken);
}
