package com.cafestory.service.serviceImplement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.concurrent.CompletableFuture;

/**
 * Fire-and-forget trigger để Python RAG service reindex một nguồn dữ liệu ngay
 * sau khi admin thay đổi formula/threshold. Nếu Python không phản hồi, log warn
 * và bỏ qua — scheduler định kỳ vẫn bắt được thay đổi ở lần chạy sau.
 *
 * <p>HMAC scheme trùng {@code RagHmacAuthFilter} bên Python: sign
 * {@code method\npath\n\ntimestamp} (query rỗng).
 */
@Service
public class RagReindexClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RagReindexClient.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String SIGNATURE_HEADER = "X-RAG-Signature";
    private static final String TIMESTAMP_HEADER = "X-RAG-Timestamp";
    private static final String REINDEX_DB_PATH = "/api/internal/rag/reindex-db";

    private final RestClient restClient;
    private final String secret;

    public RagReindexClient(
            @Value("${rag.python.base-url:http://localhost:8036}") String pythonBaseUrl,
            @Value("${rag.internal.secret}") String secret) {
        this.restClient = RestClient.builder().baseUrl(pythonBaseUrl).build();
        this.secret = secret;
    }

    /** Kích hoạt reindex DB (bao gồm formula) async — không chặn admin response. */
    public void triggerDbReindex() {
        CompletableFuture.runAsync(() -> {
            if (secret == null || secret.isBlank()) {
                LOGGER.warn("rag.internal.secret rỗng → bỏ qua trigger reindex");
                return;
            }
            try {
                long timestamp = Instant.now().getEpochSecond();
                String signature = sign("POST", REINDEX_DB_PATH, "", String.valueOf(timestamp));
                restClient.post()
                        .uri(REINDEX_DB_PATH)
                        .header(TIMESTAMP_HEADER, String.valueOf(timestamp))
                        .header(SIGNATURE_HEADER, signature)
                        .retrieve()
                        .toBodilessEntity();
                LOGGER.info("triggered RAG db reindex sau admin formula update");
            } catch (Exception ex) {
                LOGGER.warn(
                        "RAG reindex trigger fail ({}), scheduler định kỳ sẽ catch sau",
                        ex.getMessage());
            }
        });
    }

    private String sign(String method, String path, String query, String timestamp) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            String payload = method + "\n" + path + "\n" + query + "\n" + timestamp;
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Không tính được HMAC cho RAG reindex", ex);
        }
    }
}
