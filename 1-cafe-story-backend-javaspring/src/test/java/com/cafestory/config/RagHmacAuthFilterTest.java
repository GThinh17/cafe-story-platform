package com.cafestory.config;

import com.cafestory.until.security.RagHmacAuthFilter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
        request.addHeader(RagHmacAuthFilter.SIGNATURE_HEADER,
                sign("GET", "/api/internal/rag/snapshot", "sourceType=blog", timestamp));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
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
        request.addHeader(RagHmacAuthFilter.SIGNATURE_HEADER, "deadbeef");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void staleTimestampReturnsUnauthorized() throws Exception {
        String staleTimestamp = String.valueOf(Instant.now().getEpochSecond() - WINDOW_SECONDS - 60);
        MockHttpServletRequest request = internalRequest("sourceType=blog", staleTimestamp);
        request.addHeader(RagHmacAuthFilter.SIGNATURE_HEADER,
                sign("GET", "/api/internal/rag/snapshot", "sourceType=blog", staleTimestamp));
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

    private String sign(String method, String path, String query, String timestamp) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String payload = method + "\n" + path + "\n" + query + "\n" + timestamp;
        return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }
}
