package com.cafestory.security;

import com.cafestory.entity.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final String jwtSecret;
    private final long accessTokenSeconds;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.access-token-seconds}") long accessTokenSeconds) {
        this.objectMapper = objectMapper;
        this.jwtSecret = jwtSecret;
        this.accessTokenSeconds = accessTokenSeconds;
    }

    public String createAccessToken(User user, List<String> roles) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenSeconds);

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", user.getUserId().toString());
        payload.put("username", user.getUserName());
        payload.put("roles", roles);
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());

        String unsignedToken = base64Json(header) + "." + base64Json(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    public JwtClaims validateAccessToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw unauthorized("Invalid access token");
            }

            String unsignedToken = parts[0] + "." + parts[1];
            byte[] expectedSignature = signBytes(unsignedToken);
            byte[] actualSignature = BASE64_URL_DECODER.decode(parts[2]);
            if (!MessageDigest.isEqual(expectedSignature, actualSignature)) {
                throw unauthorized("Invalid access token signature");
            }

            Map<String, Object> payload = objectMapper.readValue(
                    BASE64_URL_DECODER.decode(parts[1]),
                    new TypeReference<>() {
                    });
            Instant expiresAt = Instant.ofEpochSecond(((Number) payload.get("exp")).longValue());
            if (Instant.now().isAfter(expiresAt)) {
                throw unauthorized("Access token expired");
            }

            UUID userId = UUID.fromString(String.valueOf(payload.get("sub")));
            String username = String.valueOf(payload.get("username"));
            List<String> roles = ((List<?>) payload.getOrDefault("roles", List.of()))
                    .stream()
                    .map(String::valueOf)
                    .toList();
            Instant issuedAt = Instant.ofEpochSecond(((Number) payload.get("iat")).longValue());
            return new JwtClaims(userId, username, roles, issuedAt, expiresAt);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw unauthorized("Invalid access token");
        }
    }

    public long getAccessTokenSeconds() {
        return accessTokenSeconds;
    }

    private String base64Json(Map<String, Object> value) {
        try {
            return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to write JWT JSON", ex);
        }
    }

    private String sign(String unsignedToken) {
        return BASE64_URL_ENCODER.encodeToString(signBytes(unsignedToken));
    }

    private byte[] signBytes(String unsignedToken) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign JWT", ex);
        }
    }

    private ResponseStatusException unauthorized(String reason) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, reason);
    }
}
