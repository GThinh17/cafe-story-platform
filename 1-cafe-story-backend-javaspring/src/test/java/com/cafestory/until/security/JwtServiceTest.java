package com.cafestory.until.security;

import com.cafestory.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Kiểm thử {@link JwtService} — phát hành và kiểm tra access token.
 *
 * <p>Chữ ký được dựng lại bằng chính thuật toán HMAC-SHA256 của lớp, nên bài
 * kiểm thử tạo được cả token hợp lệ lẫn token hết hạn mà không phải chờ.
 */
class JwtServiceTest {

    private static final String SECRET = "cafestory-jwt-secret-for-tests";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(objectMapper, SECRET, 3600L);
        user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("an");
    }

    @Test
    void createAccessToken_success_roundTripsClaims_TC001() {
        String token = jwtService.createAccessToken(user, List.of("USER", "REVIEWER"));

        JwtClaims claims = jwtService.validateAccessToken(token);

        assertThat(token.split("\\.")).hasSize(3);
        assertThat(claims.userId()).isEqualTo(user.getUserId());
        assertThat(claims.username()).isEqualTo("an");
        assertThat(claims.roles()).containsExactly("USER", "REVIEWER");
        assertThat(claims.expiresAt()).isAfter(claims.issuedAt());
    }

    @Test
    void getAccessTokenSeconds_success_exposesConfiguredLifetime_TC002() {
        assertThat(jwtService.getAccessTokenSeconds()).isEqualTo(3600L);
    }

    @Test
    void validateAccessToken_fail_tokenIsNotThreeParts_TC003() {
        assertThatThrownBy(() -> jwtService.validateAccessToken("chi-co-mot-phan"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid access token")
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void validateAccessToken_fail_signatureDoesNotMatch_TC004() {
        String token = jwtService.createAccessToken(user, List.of("USER"));
        String[] parts = token.split("\\.");
        String forged = parts[0] + "." + parts[1] + "."
                + Base64.getUrlEncoder().withoutPadding()
                .encodeToString("chu-ky-gia".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> jwtService.validateAccessToken(forged))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid access token signature");
    }

    @Test
    void validateAccessToken_fail_tokenExpired_TC005() {
        String expired = signedToken(Instant.now().minusSeconds(7200), Instant.now().minusSeconds(3600));

        assertThatThrownBy(() -> jwtService.validateAccessToken(expired))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Access token expired");
    }

    @Test
    void validateAccessToken_fail_payloadIsNotJson_TC006() {
        String header = base64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = base64("khong-phai-json");
        String unsigned = header + "." + payload;
        String token = unsigned + "." + sign(unsigned);

        assertThatThrownBy(() -> jwtService.validateAccessToken(token))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid access token");
    }

    private String signedToken(Instant issuedAt, Instant expiresAt) {
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", user.getUserId().toString());
        payload.put("username", user.getUserName());
        payload.put("roles", List.of("USER"));
        payload.put("iat", issuedAt.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());

        try {
            String unsigned = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(header))
                    + "."
                    + Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(payload));
            return unsigned + "." + sign(unsigned);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private String base64(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String unsignedToken) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
