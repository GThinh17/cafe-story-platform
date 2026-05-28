package com.cafestory.controller;

import com.cafestory.dto.requestDTO.LoginRequest;
import com.cafestory.dto.requestDTO.RegisterRequest;
import com.cafestory.dto.responseDTO.AuthResponse;
import com.cafestory.dto.responseDTO.UsernameSuggestionResponse;
import com.cafestory.service.serviceInterface.AuthService;
import com.cafestory.service.serviceInterface.RefreshTokenService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import com.cafestory.until.security.JwtAuthenticationFilter;
import com.cafestory.until.security.JwtService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final AuthService authService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final boolean cookieSecure;
    private final String cookieSameSite;

    public AuthController(
            AuthService authService,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            @Value("${app.auth.cookie.secure}") boolean cookieSecure,
            @Value("${app.auth.cookie.same-site}") String cookieSameSite) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse servletResponse) {
        AuthResponse response = authService.login(request);
        addCookie(servletResponse, accessTokenCookie(response.getAccessToken()));
        addCookie(servletResponse, refreshTokenCookie(response.getRefreshToken()));
        return response;
    }

    @GetMapping("/me")
    public AuthResponse me(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return authService.getCurrentUser(principal.userId());
    }

    @GetMapping("/usernames/suggestions")
    public UsernameSuggestionResponse suggestUserNames(@RequestParam String fullName) {
        return authService.suggestUserNames(fullName);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse servletResponse) {
        AuthResponse response = authService.refresh(refreshToken);
        addCookie(servletResponse, accessTokenCookie(response.getAccessToken()));
        return response;
    }

    @PostMapping("/logout")
    public void logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse servletResponse) {
        authService.logout(refreshToken);
        addCookie(servletResponse, deleteCookie(JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE, "/"));
        addCookie(servletResponse, deleteCookie(REFRESH_TOKEN_COOKIE, "/api/auth"));
    }

    private ResponseCookie accessTokenCookie(String token) {
        return ResponseCookie.from(JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(Duration.ofSeconds(jwtService.getAccessTokenSeconds()))
                .build();
    }

    private ResponseCookie refreshTokenCookie(String token) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/api/auth")
                .maxAge(Duration.ofSeconds(refreshTokenService.getRefreshTokenSeconds()))
                .build();
    }

    private ResponseCookie deleteCookie(String name, String path) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path(path)
                .maxAge(Duration.ZERO)
                .build();
    }

    private void addCookie(HttpServletResponse servletResponse, ResponseCookie cookie) {
        servletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
