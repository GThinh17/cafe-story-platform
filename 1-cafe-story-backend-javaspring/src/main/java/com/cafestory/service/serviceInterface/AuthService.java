package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.LoginRequest;
import com.cafestory.dto.requestDTO.RegisterRequest;
import com.cafestory.dto.responseDTO.AuthResponse;

import java.util.UUID;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse getCurrentUser(UUID userId);

    AuthResponse refresh(String refreshToken);

    void logout(String refreshToken);
}
