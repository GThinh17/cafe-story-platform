package com.cafestory.service;

import com.cafestory.service.serviceImplement.RagReindexClient;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kiểm thử {@link RagReindexClient}.
 *
 * <p>Dịch vụ RAG bằng Python được thay bằng một {@link HttpServer} trong tiến
 * trình. Lớp này chạy nền theo kiểu fire-and-forget nên bài kiểm thử chờ
 * {@link ForkJoinPool#commonPool()} rảnh thay vì ngủ một khoảng cố định.
 */
class RagReindexClientTest {

    private static final String SECRET = "rag-internal-secret";

    private HttpServer ragServer;
    private String baseUrl;
    private final List<String> signatures = new CopyOnWriteArrayList<>();
    private final List<String> timestamps = new CopyOnWriteArrayList<>();
    private final AtomicInteger responseStatus = new AtomicInteger(202);

    @BeforeEach
    void setUp() throws IOException {
        ragServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        ragServer.createContext("/api/internal/rag/reindex-db", exchange -> {
            signatures.add(exchange.getRequestHeaders().getFirst("X-RAG-Signature"));
            timestamps.add(exchange.getRequestHeaders().getFirst("X-RAG-Timestamp"));
            exchange.getRequestBody().readAllBytes();
            byte[] payload = "{}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(responseStatus.get(), payload.length);
            try (OutputStream body = exchange.getResponseBody()) {
                body.write(payload);
            }
        });
        ragServer.start();
        baseUrl = "http://127.0.0.1:" + ragServer.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        ragServer.stop(0);
    }

    @Test
    void triggerDbReindex_success_signsRequestWithSharedSecret_TC001() {
        RagReindexClient client = new RagReindexClient(baseUrl, SECRET);

        client.triggerDbReindex();

        assertThat(ForkJoinPool.commonPool().awaitQuiescence(15, TimeUnit.SECONDS)).isTrue();
        assertThat(signatures).hasSize(1);
        assertThat(timestamps).hasSize(1);
        assertThat(signatures.get(0))
                .isEqualTo(expectedSignature("POST", "/api/internal/rag/reindex-db", "", timestamps.get(0)));
    }

    @Test
    void triggerDbReindex_success_blankSecretSkipsCallEntirely_TC002() {
        RagReindexClient noSecret = new RagReindexClient(baseUrl, "  ");
        RagReindexClient nullSecret = new RagReindexClient(baseUrl, null);

        noSecret.triggerDbReindex();
        nullSecret.triggerDbReindex();

        assertThat(ForkJoinPool.commonPool().awaitQuiescence(15, TimeUnit.SECONDS)).isTrue();
        assertThat(signatures).isEmpty();
    }

    @Test
    void triggerDbReindex_success_serverErrorIsSwallowed_TC003() {
        responseStatus.set(500);
        RagReindexClient client = new RagReindexClient(baseUrl, SECRET);

        client.triggerDbReindex();

        // Không ném ra ngoài: scheduler định kỳ sẽ bắt lại thay đổi ở lần chạy sau.
        assertThat(ForkJoinPool.commonPool().awaitQuiescence(15, TimeUnit.SECONDS)).isTrue();
        assertThat(signatures).hasSize(1);
    }

    @Test
    void triggerDbReindex_success_unreachableServiceIsSwallowed_TC004() {
        int closedPort = ragServer.getAddress().getPort();
        ragServer.stop(0);
        RagReindexClient client = new RagReindexClient("http://127.0.0.1:" + closedPort, SECRET);

        client.triggerDbReindex();

        assertThat(ForkJoinPool.commonPool().awaitQuiescence(15, TimeUnit.SECONDS)).isTrue();
        assertThat(signatures).isEmpty();
    }

    private String expectedSignature(String method, String path, String query, String timestamp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String payload = method + "\n" + path + "\n" + query + "\n" + timestamp;
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
