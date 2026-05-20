package com.cafestory.controller;

import com.cafestory.dto.requestDTO.LoginRequest;
import com.cafestory.dto.requestDTO.RegisterRequest;
import com.cafestory.dto.responseDTO.AuthResponse;
import com.cafestory.dto.responseDTO.AuthUserResponse;
import com.cafestory.service.serviceInterface.AuthService;
import com.cafestory.service.serviceInterface.RefreshTokenService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import com.cafestory.until.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authService, jwtService, refreshTokenService, false, "Lax");
    }

    @Test
    void register_success_TC001() {
        RegisterRequest request = registerRequest();
        AuthResponse response = authResponse(null, null);

        when(authService.register(request)).thenReturn(response);

        AuthResponse result = authController.register(request);

        assertThat(result).isEqualTo(response);
        verify(authService).register(request);
    }

    @Test
    void login_success_setsHttpOnlyCookies_TC002() {
        LoginRequest request = loginRequest();
        AuthResponse response = authResponse("access-token", "refresh-token");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        when(authService.login(request)).thenReturn(response);
        when(jwtService.getAccessTokenSeconds()).thenReturn(900L);
        when(refreshTokenService.getRefreshTokenSeconds()).thenReturn(604800L);

        AuthResponse result = authController.login(request, servletResponse);

        assertThat(result).isEqualTo(response);
        assertThat(servletResponse.getHeaders("Set-Cookie"))
                .anyMatch(cookie -> cookie.contains("access_token=access-token")
                        && cookie.contains("HttpOnly")
                        && cookie.contains("Path=/")
                        && cookie.contains("SameSite=Lax"))
                .anyMatch(cookie -> cookie.contains("refresh_token=refresh-token")
                        && cookie.contains("HttpOnly")
                        && cookie.contains("Path=/api/auth")
                        && cookie.contains("SameSite=Lax"));
    }

    @Test
    void me_success_TC003() {
        UUID userId = UUID.randomUUID();
        AuthResponse response = authResponse(null, null);

        when(authService.getCurrentUser(userId)).thenReturn(response);

        AuthResponse result = authController.me(new AuthenticatedUserPrincipal(userId, "luan123", List.of("USER")));

        assertThat(result).isEqualTo(response);
        verify(authService).getCurrentUser(userId);
    }

    @Test
    void refresh_success_setsAccessCookie_TC004() {
        AuthResponse response = authResponse("new-access-token", null);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        when(authService.refresh("refresh-token")).thenReturn(response);
        when(jwtService.getAccessTokenSeconds()).thenReturn(900L);

        AuthResponse result = authController.refresh("refresh-token", servletResponse);

        assertThat(result).isEqualTo(response);
        assertThat(servletResponse.getHeaders("Set-Cookie"))
                .anyMatch(cookie -> cookie.contains("access_token=new-access-token")
                        && cookie.contains("HttpOnly")
                        && cookie.contains("Path=/"));
    }

    @Test
    void logout_success_deletesCookies_TC005() {
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        authController.logout("refresh-token", servletResponse);

        verify(authService).logout("refresh-token");
        assertThat(servletResponse.getHeaders("Set-Cookie"))
                .anyMatch(cookie -> cookie.contains("access_token=")
                        && cookie.contains("Max-Age=0")
                        && cookie.contains("Path=/"))
                .anyMatch(cookie -> cookie.contains("refresh_token=")
                        && cookie.contains("Max-Age=0")
                        && cookie.contains("Path=/api/auth"));
    }

    private RegisterRequest registerRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUserName("luan123");
        request.setPassword("123456");
        request.setUserEmail("luan123@example.com");
        return request;
    }

    private LoginRequest loginRequest() {
        LoginRequest request = new LoginRequest();
        request.setIdentifier("luan123@example.com");
        request.setPassword("123456");
        return request;
    }

    private AuthResponse authResponse(String accessToken, String refreshToken) {
        AuthUserResponse user = new AuthUserResponse();
        user.setUserId(UUID.randomUUID());
        user.setUserName("luan123");
        user.setUserEmail("luan123@example.com");
        user.setRoles(List.of("USER"));

        AuthResponse response = new AuthResponse();
        response.setUser(user);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        return response;
    }
}
