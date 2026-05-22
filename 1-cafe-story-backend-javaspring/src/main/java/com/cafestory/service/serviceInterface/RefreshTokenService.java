package com.cafestory.service.serviceInterface;

import com.cafestory.entity.RefreshToken;
import com.cafestory.entity.User;

public interface RefreshTokenService {
    String createRefreshToken(User user);

    RefreshToken validateRefreshToken(String rawToken);

    void revokeRefreshToken(String rawToken);

    long getRefreshTokenSeconds();
}
