package com.cafestory.service.serviceImplement;

import com.cafestory.entity.RefreshToken;
import com.cafestory.entity.User;
import com.cafestory.repository.RefreshTokenRepository;
import com.cafestory.service.serviceInterface.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenSeconds;

    public RefreshTokenServiceImpl(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${app.jwt.refresh-token-seconds}") long refreshTokenSeconds) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenSeconds = refreshTokenSeconds;
    }

    @Override
    @Transactional
    public String createRefreshToken(User user) {
        String rawToken = randomToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashToken(rawToken));
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenSeconds));
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw unauthorized("Refresh token is required");
        }
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hashToken(rawToken))
                .orElseThrow(() -> unauthorized("Refresh token is invalid"));
        if (refreshToken.getRevokedAt() != null) {
            throw unauthorized("Refresh token was revoked");
        }
        if (LocalDateTime.now().isAfter(refreshToken.getExpiresAt())) {
            throw unauthorized("Refresh token expired");
        }
        return refreshToken;
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(hashToken(rawToken))
                .filter(refreshToken -> refreshToken.getRevokedAt() == null)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevokedAt(LocalDateTime.now());
                    refreshTokenRepository.save(refreshToken);
                });
    }

    @Override
    public long getRefreshTokenSeconds() {
        return refreshTokenSeconds;
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash refresh token", ex);
        }
    }

    private ResponseStatusException unauthorized(String reason) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, reason);
    }
}
