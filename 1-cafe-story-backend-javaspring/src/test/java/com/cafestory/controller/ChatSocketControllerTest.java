package com.cafestory.controller;

import com.cafestory.controller.ChatSocketController;
import com.cafestory.dto.requestDTO.SendMessageRequestDTO;
import com.cafestory.dto.requestDTO.TypingRequestDTO;
import com.cafestory.dto.responseDTO.ChatMessageResponseDTO;
import com.cafestory.dto.responseDTO.SocketEventResponseDTO;
import com.cafestory.entity.enums.MessageStatus;
import com.cafestory.entity.enums.MessageType;
import com.cafestory.service.serviceInterface.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatSocketControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void sendMessage_success_emitsMessageSentAckOnly_TC001() {
        ChatSocketController controller = new ChatSocketController(chatService, messagingTemplate);
        UUID conversationId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        SendMessageRequestDTO request = new SendMessageRequestDTO();
        request.setSenderId(senderId);
        request.setType(MessageType.TEXT);
        request.setText("hello ws");
        ChatMessageResponseDTO message = message(conversationId, senderId);

        when(chatService.sendMessage(conversationId, request)).thenReturn(message);

        controller.sendMessage(conversationId, request);

        ArgumentCaptor<SocketEventResponseDTO> eventCaptor = ArgumentCaptor.forClass(SocketEventResponseDTO.class);
        verify(messagingTemplate).convertAndSendToUser(
                eq(senderId.toString()),
                eq("/queue/chat"),
                eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEvent()).isEqualTo("message_sent");
    }

    @Test
    void typingStart_success_emitsTypingStart_TC002() {
        ChatSocketController controller = new ChatSocketController(chatService, messagingTemplate);
        UUID conversationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TypingRequestDTO request = new TypingRequestDTO();
        request.setConversationId(conversationId);
        request.setUserId(userId);

        controller.typingStart(request);

        ArgumentCaptor<SocketEventResponseDTO> eventCaptor = ArgumentCaptor.forClass(SocketEventResponseDTO.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/conversations/" + conversationId), eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEvent()).isEqualTo("typing_start");
        assertThat(eventCaptor.getValue().getUserId()).isEqualTo(userId);
    }

    @Test
    void socketEventResponse_serializesFrontendCompatibleFields_TC003() throws Exception {
        UUID conversationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SocketEventResponseDTO event = new SocketEventResponseDTO("receive_message", conversationId, userId, "payload");

        String json = new ObjectMapper().writeValueAsString(event);

        assertThat(json).contains("\"type\":\"receive_message\"");
        assertThat(json).contains("\"data\":\"payload\"");
        assertThat(json).contains("\"conversationId\":\"" + conversationId + "\"");
        assertThat(json).doesNotContain("\"event\":");
        assertThat(json).doesNotContain("\"payload\":");
    }

    private ChatMessageResponseDTO message(UUID conversationId, UUID senderId) {
        ChatMessageResponseDTO message = new ChatMessageResponseDTO();
        message.setId(UUID.randomUUID());
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setType(MessageType.TEXT);
        message.setText("hello ws");
        message.setStatus(MessageStatus.SENT);
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(message.getCreatedAt());
        return message;
    }
}
