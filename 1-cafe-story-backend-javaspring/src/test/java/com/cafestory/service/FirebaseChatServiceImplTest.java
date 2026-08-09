package com.cafestory.service;

import com.cafestory.dto.responseDTO.ChatMessageResponseDTO;
import com.cafestory.dto.responseDTO.ConversationResponseDTO;
import com.cafestory.service.serviceImplement.FirebaseChatServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Kiểm thử {@link FirebaseChatServiceImpl}.
 *
 * <p>Firebase Realtime Database được thay bằng một {@link HttpServer} chạy trên
 * cổng loopback ngẫu nhiên: bài kiểm thử vừa kiểm được đường dẫn REST mà lớp
 * dựng ra, vừa mô phỏng được lỗi 5xx mà không gọi dịch vụ thật.
 */
class FirebaseChatServiceImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<String> requestedPaths = new CopyOnWriteArrayList<>();
    private final AtomicInteger responseStatus = new AtomicInteger(200);

    private HttpServer firebaseServer;
    private String databaseUrl;

    @BeforeEach
    void setUp() throws IOException {
        firebaseServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        firebaseServer.createContext("/", exchange -> {
            requestedPaths.add(exchange.getRequestURI().toString());
            exchange.getRequestBody().readAllBytes();
            byte[] payload = "{}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(responseStatus.get(), payload.length);
            try (OutputStream body = exchange.getResponseBody()) {
                body.write(payload);
            }
        });
        firebaseServer.start();
        databaseUrl = "http://127.0.0.1:" + firebaseServer.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        firebaseServer.stop(0);
    }

    @Test
    void saveConversation_success_writesConversationNode_TC001() {
        FirebaseChatServiceImpl service = service(databaseUrl + "/", "token 1");
        ConversationResponseDTO conversation = conversation();

        service.saveConversation(conversation);

        assertThat(requestedPaths).hasSize(1);
        assertThat(requestedPaths.get(0))
                .isEqualTo("/conversations/" + conversation.getId() + ".json?auth=token+1");
    }

    @Test
    void saveConversation_success_noAuthTokenMeansNoQueryString_TC002() {
        FirebaseChatServiceImpl service = service(databaseUrl, "   ");
        ConversationResponseDTO conversation = conversation();

        service.saveConversation(conversation);

        assertThat(requestedPaths.get(0)).doesNotContain("auth=");
    }

    @Test
    void saveConversation_success_blankDatabaseUrlSkipsRemoteCall_TC003() {
        FirebaseChatServiceImpl service = service("", null);

        service.saveConversation(conversation());

        assertThat(requestedPaths).isEmpty();
    }

    @Test
    void saveConversation_success_nullDatabaseUrlSkipsRemoteCall_TC004() {
        FirebaseChatServiceImpl service = service(null, null);

        service.saveConversation(conversation());

        assertThat(requestedPaths).isEmpty();
    }

    @Test
    void saveConversation_fail_missingConversationId_TC005() {
        FirebaseChatServiceImpl service = service(databaseUrl, null);
        ConversationResponseDTO withoutId = new ConversationResponseDTO();

        assertThatThrownBy(() -> service.saveConversation(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Firebase write failed");
        assertThatThrownBy(() -> service.saveConversation(withoutId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_GATEWAY));
    }

    @Test
    void saveMessage_success_writesMessageAndConversationIndex_TC006() {
        FirebaseChatServiceImpl service = service(databaseUrl, null);
        ChatMessageResponseDTO message = message();

        service.saveMessage(message);

        assertThat(requestedPaths).containsExactly(
                "/messages/" + message.getId() + ".json",
                "/conversation_messages/" + message.getConversationId() + "/" + message.getId() + ".json");
    }

    @Test
    void saveMessage_fail_missingMessageId_TC007() {
        FirebaseChatServiceImpl service = service(databaseUrl, null);
        ChatMessageResponseDTO withoutId = new ChatMessageResponseDTO();

        assertThatThrownBy(() -> service.saveMessage(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Firebase write failed");
        assertThatThrownBy(() -> service.saveMessage(withoutId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Firebase write failed");
    }

    @Test
    void updateLatestMessage_success_writesBothNodes_TC008() {
        FirebaseChatServiceImpl service = service(databaseUrl, null);
        ConversationResponseDTO conversation = conversation();
        ChatMessageResponseDTO message = message();

        service.updateLatestMessage(conversation, message);

        assertThat(requestedPaths).containsExactly(
                "/conversations/" + conversation.getId() + "/latestMessage.json",
                "/conversations/" + conversation.getId() + ".json");
    }

    @Test
    void updateLatestMessage_fail_missingArguments_TC009() {
        FirebaseChatServiceImpl service = service(databaseUrl, null);
        ConversationResponseDTO conversation = conversation();
        ChatMessageResponseDTO message = message();

        assertThatThrownBy(() -> service.updateLatestMessage(null, message))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Firebase write failed");
        assertThatThrownBy(() -> service.updateLatestMessage(conversation, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Firebase write failed");
        assertThatThrownBy(() ->
                service.updateLatestMessage(new ConversationResponseDTO(), message))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Firebase write failed");
        assertThatThrownBy(() ->
                service.updateLatestMessage(conversation, new ChatMessageResponseDTO()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Firebase write failed");
    }

    @Test
    void saveConversation_fail_remoteReturnsServerError_TC010() {
        responseStatus.set(500);
        FirebaseChatServiceImpl service = service(databaseUrl, null);
        ConversationResponseDTO conversation = conversation();

        assertThatThrownBy(() -> service.saveConversation(conversation))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Firebase write failed");
    }

    @Test
    void saveConversation_fail_remoteIsUnreachable_TC011() {
        // Cổng đã đóng: HttpClient ném IOException, lớp phải quy về BAD_GATEWAY.
        int closedPort = firebaseServer.getAddress().getPort();
        firebaseServer.stop(0);
        FirebaseChatServiceImpl service = service("http://127.0.0.1:" + closedPort, null);
        ConversationResponseDTO conversation = conversation();

        assertThatThrownBy(() -> service.saveConversation(conversation))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Firebase write failed");

        // tearDown gọi stop lần nữa là vô hại.
    }

    private FirebaseChatServiceImpl service(String url, String token) {
        return new FirebaseChatServiceImpl(objectMapper, url, token);
    }

    private ConversationResponseDTO conversation() {
        ConversationResponseDTO conversation = new ConversationResponseDTO();
        conversation.setId(UUID.randomUUID());
        return conversation;
    }

    private ChatMessageResponseDTO message() {
        ChatMessageResponseDTO message = new ChatMessageResponseDTO();
        message.setId(UUID.randomUUID());
        message.setConversationId(UUID.randomUUID());
        message.setText("xin chao");
        return message;
    }
}
