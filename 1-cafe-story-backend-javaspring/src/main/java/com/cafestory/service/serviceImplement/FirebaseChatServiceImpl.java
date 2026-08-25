package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.ChatMessageResponseDTO;
import com.cafestory.dto.responseDTO.ConversationResponseDTO;
import com.cafestory.service.serviceInterface.FirebaseChatService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FirebaseChatServiceImpl implements FirebaseChatService {

    private final Map<UUID, ConversationResponseDTO> conversations = new ConcurrentHashMap<>();
    private final Map<UUID, ChatMessageResponseDTO> messages = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String firebaseDatabaseUrl;
    private final String firebaseAuthToken;

    public FirebaseChatServiceImpl(
            ObjectMapper objectMapper,
            @Value("${firebase.database-url:${FIREBASE_DATABASE_URL:}}") String firebaseDatabaseUrl,
            @Value("${firebase.auth-token:${FIREBASE_AUTH_TOKEN:}}") String firebaseAuthToken) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.firebaseDatabaseUrl = trimTrailingSlash(firebaseDatabaseUrl);
        this.firebaseAuthToken = firebaseAuthToken;
    }

    @Override
    public void saveConversation(ConversationResponseDTO conversation) {
        if (conversation == null || conversation.getId() == null) {
            throw firebaseWriteFailed();
        }
        conversations.put(conversation.getId(), conversation);
        put("conversations/" + conversation.getId(), conversation);
    }

    @Override
    public void saveMessage(ChatMessageResponseDTO message) {
        if (message == null || message.getId() == null) {
            throw firebaseWriteFailed();
        }
        messages.put(message.getId(), message);
        put("messages/" + message.getId(), message);
        put("conversation_messages/" + message.getConversationId() + "/" + message.getId(), message);
    }

    @Override
    public void updateLatestMessage(ConversationResponseDTO conversation, ChatMessageResponseDTO message) {
        if (conversation == null || conversation.getId() == null || message == null || message.getId() == null) {
            throw firebaseWriteFailed();
        }
        conversations.put(conversation.getId(), conversation);
        messages.put(message.getId(), message);
        put("conversations/" + conversation.getId() + "/latestMessage", message);
        put("conversations/" + conversation.getId(), conversation);
    }

    private ResponseStatusException firebaseWriteFailed() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Firebase write failed");
    }

    private void put(String path, Object body) {
        if (firebaseDatabaseUrl == null || firebaseDatabaseUrl.isBlank()) {
            return;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(firebaseUri(path))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw firebaseWriteFailed();
            }
        } catch (JsonProcessingException ex) {
            throw firebaseWriteFailed();
        } catch (IOException ex) {
            throw firebaseWriteFailed();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw firebaseWriteFailed();
        }
    }

    private URI firebaseUri(String path) {
        String authQuery = "";
        if (firebaseAuthToken != null && !firebaseAuthToken.isBlank()) {
            authQuery = "?auth=" + URLEncoder.encode(firebaseAuthToken, StandardCharsets.UTF_8);
        }
        return URI.create(firebaseDatabaseUrl + "/" + path + ".json" + authQuery);
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("/+$", "");
    }
}
