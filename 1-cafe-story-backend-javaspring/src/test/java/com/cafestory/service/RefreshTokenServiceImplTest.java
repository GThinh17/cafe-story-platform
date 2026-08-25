package com.cafestory.service;

import com.cafestory.entity.RefreshToken;
import com.cafestory.entity.User;
import com.cafestory.repository.RefreshTokenRepository;
import com.cafestory.service.serviceImplement.RefreshTokenServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void createRefreshToken_success_savesHashOnly_TC001() {
        User user = user();

        String rawToken = service().createRefreshToken(user);

        assertThat(rawToken).isNotBlank();
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getTokenHash()).isNotEqualTo(rawToken);
        assertThat(captor.getValue().getTokenHash()).hasSize(64);
        assertThat(captor.getValue().getUser()).isEqualTo(user);
    }

    @Test
    void validateRefreshToken_success_TC002() {
        RefreshToken refreshToken = validRefreshToken();
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(refreshToken));

        RefreshToken result = service().validateRefreshToken("raw-refresh-token");

        assertThat(result).isEqualTo(refreshToken);
    }

    @Test
    void validateRefreshToken_fail_expired_TC003() {
        RefreshToken refreshToken = validRefreshToken();
        refreshToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> service().validateRefreshToken("raw-refresh-token"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void validateRefreshToken_fail_revoked_TC004() {
        RefreshToken refreshToken = validRefreshToken();
        refreshToken.setRevokedAt(LocalDateTime.now());
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> service().validateRefreshToken("raw-refresh-token"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void revokeRefreshToken_success_TC005() {
        RefreshToken refreshToken = validRefreshToken();
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(refreshToken));

        service().revokeRefreshToken("raw-refresh-token");

        assertThat(refreshToken.getRevokedAt()).isNotNull();
        verify(refreshTokenRepository).save(refreshToken);
    }

    @Test
    void validateRefreshToken_fail_rawTokenMissing_TC006() {
        RefreshTokenServiceImpl service = service();

        assertThatThrownBy(() -> service.validateRefreshToken(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Refresh token is required");
        assertThatThrownBy(() -> service.validateRefreshToken("   "))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Refresh token is required");
    }

    @Test
    void validateRefreshToken_fail_tokenNotFound_TC007() {
        RefreshTokenServiceImpl service = service();
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validateRefreshToken("raw-refresh-token"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Refresh token is invalid");
    }

    @Test
    void revokeRefreshToken_success_blankTokenIsIgnored_TC008() {
        RefreshTokenServiceImpl service = service();

        service.revokeRefreshToken(null);
        service.revokeRefreshToken("   ");

        verify(refreshTokenRepository, org.mockito.Mockito.never()).findByTokenHash(anyString());
    }

    @Test
    void revokeRefreshToken_success_alreadyRevokedTokenIsNotSavedAgain_TC009() {
        RefreshToken refreshToken = validRefreshToken();
        refreshToken.setRevokedAt(LocalDateTime.now().minusDays(1));
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(refreshToken));

        service().revokeRefreshToken("raw-refresh-token");

        verify(refreshTokenRepository, org.mockito.Mockito.never()).save(refreshToken);
    }

    @Test
    void getRefreshTokenSeconds_success_exposesConfiguredLifetime_TC010() {
        assertThat(service().getRefreshTokenSeconds()).isEqualTo(604800L);
    }

    private RefreshTokenServiceImpl service() {
        return new RefreshTokenServiceImpl(refreshTokenRepository, 604800);
    }

    private RefreshToken validRefreshToken() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(UUID.randomUUID());
        refreshToken.setUser(user());
        refreshToken.setTokenHash("hash");
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        return refreshToken;
    }

    private User user() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("luan123");
        user.setUserEmail("luan123@example.com");
        return user;
    }
}
