package com.cafestory.until.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

@Component
public class RagHmacAuthFilter extends OncePerRequestFilter {

    public static final String SIGNATURE_HEADER = "X-RAG-Signature";
    public static final String TIMESTAMP_HEADER = "X-RAG-Timestamp";
    public static final String BODY_HASH_HEADER = "X-RAG-Body-SHA256";
    private static final String INTERNAL_PATH_PREFIX = "/api/internal/";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String secret;
    private final long timestampWindowSeconds;

    public RagHmacAuthFilter(
            @Value("${rag.internal.secret}") String secret,
            @Value("${rag.internal.timestamp-window-seconds:300}") long timestampWindowSeconds) {
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
        String providedBodyHash = request.getHeader(BODY_HASH_HEADER);
        if (timestamp == null || timestamp.isBlank()
                || signature == null || signature.isBlank()
                || providedBodyHash == null || providedBodyHash.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing RAG signature headers");
            return;
        }

        if (!isTimestampWithinWindow(timestamp)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "RAG timestamp outside allowed window");
            return;
        }

        CachedBodyRequest cachedRequest = new CachedBodyRequest(request);
        String bodyHash = sha256Hex(cachedRequest.body());
        if (!constantTimeEquals(bodyHash, providedBodyHash)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid RAG body hash");
            return;
        }

        String expectedSignature = computeSignature(
                cachedRequest.getMethod(),
                cachedRequest.getRequestURI(),
                cachedRequest.getQueryString() == null ? "" : cachedRequest.getQueryString(),
                timestamp,
                bodyHash);
        if (!constantTimeEquals(expectedSignature, signature)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid RAG signature");
            return;
        }

        filterChain.doFilter(cachedRequest, response);
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

    private String computeSignature(String method, String path, String query, String timestamp, String bodyHash) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            String payload = method + "\n" + path + "\n" + query + "\n" + timestamp + "\n" + bodyHash;
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to compute RAG HMAC signature", ex);
        }
    }

    private String sha256Hex(byte[] body) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(body));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to compute RAG body hash", ex);
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }

    private static class CachedBodyRequest extends HttpServletRequestWrapper {

        private final byte[] body;

        CachedBodyRequest(HttpServletRequest request) throws IOException {
            super(request);
            this.body = request.getInputStream().readAllBytes();
        }

        byte[] body() {
            return body;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream input = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return input.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                    throw new UnsupportedOperationException("Async reads are not supported");
                }

                @Override
                public int read() {
                    return input.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
