package com.cafestory.service.serviceImplement;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.erdtman.jcs.JsonCanonicalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class AdminReportAiWebhookSigner {

    public static final String CONTRACT_VERSION_HEADER = "X-CafeStory-Contract-Version";
    public static final String CORRELATION_ID_HEADER = "X-CafeStory-Correlation-Id";
    public static final String TIMESTAMP_HEADER = "X-CafeStory-Timestamp";
    public static final String NONCE_HEADER = "X-CafeStory-Nonce";
    public static final String BODY_SHA256_HEADER = "X-CafeStory-Body-SHA256";
    public static final String SIGNATURE_HEADER = "X-CafeStory-Signature";

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int SHA256_HEX_LENGTH = 64;
    private static final int MAX_NONCE_LENGTH = 128;

    private final ObjectMapper objectMapper;
    private final String secret;
    private final long timestampWindowSeconds;
    private final long nonceTtlSeconds;
    private final Clock clock;
    private final ConcurrentMap<String, Long> responseNonceExpirations = new ConcurrentHashMap<>();

    @Autowired
    public AdminReportAiWebhookSigner(
            ObjectMapper objectMapper,
            @Value("${admin.report.ai.hmac-secret:}") String secret,
            @Value("${admin.report.ai.timestamp-window-seconds:120}") long timestampWindowSeconds,
            @Value("${admin.report.ai.nonce-ttl-seconds:300}") long nonceTtlSeconds) {
        this(objectMapper, secret, timestampWindowSeconds, nonceTtlSeconds, Clock.systemUTC());
    }

    AdminReportAiWebhookSigner(
            ObjectMapper objectMapper,
            String secret,
            long timestampWindowSeconds,
            long nonceTtlSeconds,
            Clock clock) {
        if (timestampWindowSeconds <= 0 || nonceTtlSeconds <= 0) {
            throw new IllegalArgumentException("Admin Report AI security windows must be positive");
        }
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.timestampWindowSeconds = timestampWindowSeconds;
        this.nonceTtlSeconds = nonceTtlSeconds;
        this.clock = clock;
    }

    public SignedRequest signRequest(Object payload, String contractVersion, UUID correlationId) {
        requireSecret();
        if (contractVersion == null || contractVersion.isBlank() || correlationId == null) {
            throw new IllegalArgumentException("Admin Report AI signing metadata is required");
        }
        String canonicalBody = canonicalJson(payload);
        String timestamp = String.valueOf(clock.instant().getEpochSecond());
        String nonce = UUID.randomUUID().toString();
        String bodyHash = sha256Hex(canonicalBody);
        return new SignedRequest(
                canonicalBody,
                contractVersion,
                correlationId.toString(),
                timestamp,
                nonce,
                bodyHash,
                signature(timestamp, nonce, bodyHash));
    }

    public void verifyResponse(
            String responseBody,
            HttpHeaders responseHeaders,
            String expectedContractVersion,
            UUID expectedCorrelationId) {
        requireSecret();
        if (responseHeaders == null || expectedCorrelationId == null) {
            throw new SecurityException("Admin Report AI response security metadata is missing");
        }

        String contractVersion = requiredHeader(responseHeaders, CONTRACT_VERSION_HEADER);
        String correlationId = requiredHeader(responseHeaders, CORRELATION_ID_HEADER);
        String timestamp = requiredHeader(responseHeaders, TIMESTAMP_HEADER);
        String nonce = requiredHeader(responseHeaders, NONCE_HEADER);
        String providedBodyHash = requiredHeader(responseHeaders, BODY_SHA256_HEADER);
        String providedSignature = requiredHeader(responseHeaders, SIGNATURE_HEADER);

        if (!constantTimeEquals(expectedContractVersion, contractVersion)
                || !constantTimeEquals(expectedCorrelationId.toString(), correlationId)) {
            throw new SecurityException("Admin Report AI response contract or correlation mismatch");
        }

        long nowEpochSeconds = clock.instant().getEpochSecond();
        long responseEpochSeconds = parseTimestamp(timestamp);
        if (responseEpochSeconds < nowEpochSeconds - timestampWindowSeconds
                || responseEpochSeconds > nowEpochSeconds + timestampWindowSeconds) {
            throw new SecurityException("Admin Report AI response timestamp is outside the allowed window");
        }
        if (nonce.length() > MAX_NONCE_LENGTH || !nonce.matches("[A-Za-z0-9._:-]+")) {
            throw new SecurityException("Admin Report AI response nonce is invalid");
        }
        if (!isSha256Hex(providedBodyHash) || !isSha256Hex(providedSignature)) {
            throw new SecurityException("Admin Report AI response digest format is invalid");
        }

        String actualBodyHash = sha256Hex(canonicalJson(responseBody));
        if (!constantTimeEquals(actualBodyHash, providedBodyHash)) {
            throw new SecurityException("Admin Report AI response body hash is invalid");
        }
        String expectedSignature = signature(timestamp, nonce, actualBodyHash);
        if (!constantTimeEquals(expectedSignature, providedSignature)) {
            throw new SecurityException("Admin Report AI response signature is invalid");
        }

        purgeExpiredNonces(nowEpochSeconds);
        Long previousExpiry = responseNonceExpirations.putIfAbsent(nonce, nowEpochSeconds + nonceTtlSeconds);
        if (previousExpiry != null) {
            throw new SecurityException("Admin Report AI response nonce was replayed");
        }
    }

    String canonicalJson(Object payload) {
        try {
            String serialized = payload instanceof String text
                    ? text
                    : objectMapper.writeValueAsString(payload);
            return new JsonCanonicalizer(serialized).getEncodedString();
        } catch (IOException exception) {
            throw new IllegalArgumentException("Admin Report AI payload must be valid JSON", exception);
        }
    }

    String signature(String timestamp, String nonce, String bodyHash) {
        requireSecret();
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            String signatureInput = timestamp + "\n" + nonce + "\n" + bodyHash;
            return HexFormat.of().formatHex(mac.doFinal(signatureInput.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign Admin Report AI webhook payload", exception);
        }
    }

    private long parseTimestamp(String timestamp) {
        try {
            return Long.parseLong(timestamp);
        } catch (NumberFormatException exception) {
            throw new SecurityException("Admin Report AI response timestamp is invalid", exception);
        }
    }

    private void purgeExpiredNonces(long nowEpochSeconds) {
        responseNonceExpirations.entrySet().removeIf(entry -> entry.getValue() <= nowEpochSeconds);
    }

    private String requiredHeader(HttpHeaders headers, String name) {
        String value = headers.getFirst(name);
        if (value == null || value.isBlank()) {
            throw new SecurityException("Admin Report AI response signature headers are missing");
        }
        return value;
    }

    private String sha256Hex(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private boolean isSha256Hex(String value) {
        return value.length() == SHA256_HEX_LENGTH && value.matches("[0-9a-fA-F]+");
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }

    private void requireSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Admin Report AI HMAC secret is not configured");
        }
    }

    public record SignedRequest(
            String body,
            String contractVersion,
            String correlationId,
            String timestamp,
            String nonce,
            String bodyHash,
            String signature) {

        public void apply(HttpHeaders headers) {
            headers.set(CONTRACT_VERSION_HEADER, contractVersion);
            headers.set(CORRELATION_ID_HEADER, correlationId);
            headers.set(TIMESTAMP_HEADER, timestamp);
            headers.set(NONCE_HEADER, nonce);
            headers.set(BODY_SHA256_HEADER, bodyHash);
            headers.set(SIGNATURE_HEADER, signature);
        }
    }
}
