package com.cafestory.integration;

import com.cafestory.until.security.RagHmacAuthFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RagHmacPostgresIntegrationTest extends PostgresIntegrationTestSupport {

    private static final String SECRET = "cafestory-test-rag-secret";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void internalRagSnapshot_success_validHmacReachesRealControllerServiceAndPostgres() throws Exception {
        String path = "/api/internal/rag/snapshot";
        String query = "sourceType=reviewer&limit=5";
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String bodyHash = sha256Hex("");

        mockMvc.perform(get(path + "?" + query)
                        .header(RagHmacAuthFilter.TIMESTAMP_HEADER, timestamp)
                        .header(RagHmacAuthFilter.BODY_HASH_HEADER, bodyHash)
                        .header(RagHmacAuthFilter.SIGNATURE_HEADER,
                                signature("GET", path, query, timestamp, bodyHash)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.tombstones").isArray())
                .andExpect(jsonPath("$.data.hasMore").value(false));
    }

    @Test
    void internalRagSnapshot_fail_missingHmacHeadersRejectedBeforeController() throws Exception {
        mockMvc.perform(get("/api/internal/rag/snapshot")
                        .param("sourceType", "reviewer")
                        .param("limit", "5"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void internalRagSnapshot_fail_badBodyHashRejectedBeforeController() throws Exception {
        String path = "/api/internal/rag/snapshot";
        String query = "sourceType=reviewer&limit=5";
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String actualBodyHash = sha256Hex("");
        String wrongBodyHash = sha256Hex("tampered");

        mockMvc.perform(get(path + "?" + query)
                        .header(RagHmacAuthFilter.TIMESTAMP_HEADER, timestamp)
                        .header(RagHmacAuthFilter.BODY_HASH_HEADER, wrongBodyHash)
                        .header(RagHmacAuthFilter.SIGNATURE_HEADER,
                                signature("GET", path, query, timestamp, actualBodyHash)))
                .andExpect(status().isUnauthorized());
    }

    private String signature(String method, String path, String query, String timestamp, String bodyHash) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String payload = method + "\n" + path + "\n" + query + "\n" + timestamp + "\n" + bodyHash;
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign RAG request", ex);
        }
    }

    private String sha256Hex(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash RAG body", ex);
        }
    }
}
