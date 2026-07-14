package com.cafestory.config;

import com.cafestory.until.security.RagHmacAuthFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class RagHmacAuthFilterTest {

    private static final String SECRET = "test-rag-secret";
    private static final long WINDOW_SECONDS = 300;

    private RagHmacAuthFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new RagHmacAuthFilter(SECRET, WINDOW_SECONDS);
        filterChain = mock(FilterChain.class);
    }

    @Test
    void validSignaturePassesThrough() throws Exception {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        MockHttpServletRequest request = internalRequest("sourceType=blog", timestamp);
        request.addHeader(RagHmacAuthFilter.BODY_HASH_HEADER, bodyHash(""));
        request.addHeader(RagHmacAuthFilter.SIGNATURE_HEADER,
                sign("GET", "/api/internal/rag/snapshot", "sourceType=blog", timestamp, bodyHash("")));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(any(ServletRequest.class), eq(response));
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void validPostSignaturePassesCachedBodyThrough() throws Exception {
        String body = "{\"limit\":5,\"userJwt\":\"token-a\"}";
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        MockHttpServletRequest request = postInternalRequest(body, timestamp);
        String bodyHash = bodyHash(body);
        request.addHeader(RagHmacAuthFilter.BODY_HASH_HEADER, bodyHash);
        request.addHeader(RagHmacAuthFilter.SIGNATURE_HEADER,
                sign("POST", "/api/internal/user-context/blog-moderation", "", timestamp, bodyHash));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(filterChain, times(1)).doFilter(requestCaptor.capture(), eq(response));
        assertThat(new String(requestCaptor.getValue().getInputStream().readAllBytes(), StandardCharsets.UTF_8))
                .isEqualTo(body);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void missingHeadersReturnsUnauthorized() throws Exception {
        MockHttpServletRequest request = internalRequest("sourceType=blog", null);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void invalidSignatureReturnsUnauthorized() throws Exception {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        MockHttpServletRequest request = internalRequest("sourceType=blog", timestamp);
        request.addHeader(RagHmacAuthFilter.BODY_HASH_HEADER, bodyHash(""));
        request.addHeader(RagHmacAuthFilter.SIGNATURE_HEADER, "deadbeef");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void changedPostBodyReturnsUnauthorized() throws Exception {
        String signedBody = "{\"limit\":5,\"userJwt\":\"token-a\"}";
        String tamperedBody = "{\"limit\":5,\"userJwt\":\"token-b\"}";
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String signedBodyHash = bodyHash(signedBody);
        MockHttpServletRequest request = postInternalRequest(tamperedBody, timestamp);
        request.addHeader(RagHmacAuthFilter.BODY_HASH_HEADER, signedBodyHash);
        request.addHeader(RagHmacAuthFilter.SIGNATURE_HEADER,
                sign("POST", "/api/internal/user-context/blog-moderation", "", timestamp, signedBodyHash));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), eq(response));
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void staleTimestampReturnsUnauthorized() throws Exception {
        String staleTimestamp = String.valueOf(Instant.now().getEpochSecond() - WINDOW_SECONDS - 60);
        MockHttpServletRequest request = internalRequest("sourceType=blog", staleTimestamp);
        request.addHeader(RagHmacAuthFilter.BODY_HASH_HEADER, bodyHash(""));
        request.addHeader(RagHmacAuthFilter.SIGNATURE_HEADER,
                sign("GET", "/api/internal/rag/snapshot", "sourceType=blog", staleTimestamp, bodyHash("")));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void nonInternalPathIsNotFiltered() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/blogs");
        request.setRequestURI("/api/blogs");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void blankSecretReturnsUnauthorized() throws Exception {
        RagHmacAuthFilter unconfiguredFilter = new RagHmacAuthFilter("", WINDOW_SECONDS);
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        MockHttpServletRequest request = internalRequest("sourceType=blog", timestamp);
        request.addHeader(RagHmacAuthFilter.BODY_HASH_HEADER, bodyHash(""));
        request.addHeader(RagHmacAuthFilter.SIGNATURE_HEADER, "anything");
        MockHttpServletResponse response = new MockHttpServletResponse();

        unconfiguredFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    private MockHttpServletRequest internalRequest(String query, String timestamp) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/internal/rag/snapshot");
        request.setRequestURI("/api/internal/rag/snapshot");
        request.setQueryString(query);
        if (timestamp != null) {
            request.addHeader(RagHmacAuthFilter.TIMESTAMP_HEADER, timestamp);
        }
        return request;
    }

    private MockHttpServletRequest postInternalRequest(String body, String timestamp) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/internal/user-context/blog-moderation");
        request.setRequestURI("/api/internal/user-context/blog-moderation");
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        if (timestamp != null) {
            request.addHeader(RagHmacAuthFilter.TIMESTAMP_HEADER, timestamp);
        }
        return request;
    }

    private String sign(String method, String path, String query, String timestamp, String bodyHash) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String payload = method + "\n" + path + "\n" + query + "\n" + timestamp + "\n" + bodyHash;
        return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }

    private String bodyHash(String body) throws Exception {
        return HexFormat.of().formatHex(
                java.security.MessageDigest.getInstance("SHA-256")
                        .digest(body.getBytes(StandardCharsets.UTF_8)));
    }
}
