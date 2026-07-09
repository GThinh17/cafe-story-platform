package com.cafestory.until.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

@Component
public class RagHmacAuthFilter extends OncePerRequestFilter {

    public static final String SIGNATURE_HEADER = "X-RAG-Signature";
    public static final String TIMESTAMP_HEADER = "X-RAG-Timestamp";
    private static final String INTERNAL_PATH_PREFIX = "/api/internal/";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String secret;
    private final long timestampWindowSeconds;

    public RagHmacAuthFilter(
            @Value("${rag.internal.secret}") String secret,
            @Value("${rag.internal.timestamp-window-seconds}") long timestampWindowSeconds) {
        this.secret = secret;
        this.timestampWindowSeconds = timestampWindowSeconds;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(INTERNAL_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (secret == null || secret.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "RAG internal secret is not configured");
            return;
        }

        String timestamp = request.getHeader(TIMESTAMP_HEADER);
        String signature = request.getHeader(SIGNATURE_HEADER);
        if (timestamp == null || timestamp.isBlank() || signature == null || signature.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing RAG signature headers");
            return;
        }

        if (!isTimestampWithinWindow(timestamp)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "RAG timestamp outside allowed window");
            return;
        }

        String expectedSignature = computeSignature(
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString() == null ? "" : request.getQueryString(),
                timestamp);
        if (!constantTimeEquals(expectedSignature, signature)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid RAG signature");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isTimestampWithinWindow(String timestamp) {
        try {
            long requestEpochSeconds = Long.parseLong(timestamp);
            long nowEpochSeconds = Instant.now().getEpochSecond();
            return Math.abs(nowEpochSeconds - requestEpochSeconds) <= timestampWindowSeconds;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private String computeSignature(String method, String path, String query, String timestamp) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            String payload = method + "\n" + path + "\n" + query + "\n" + timestamp;
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to compute RAG HMAC signature", ex);
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }
}
