package com.cafestory.controller;

import com.cafestory.controller.ChatSocketController;
import com.cafestory.dto.requestDTO.SendMessageRequestDTO;
import com.cafestory.dto.requestDTO.SocketConversationRequestDTO;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Test
    void sendMessage_fail_emitsMessageFailedThenRethrows_TC004() {
        ChatSocketController controller = new ChatSocketController(chatService, messagingTemplate);
        UUID conversationId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        SendMessageRequestDTO request = new SendMessageRequestDTO();
        request.setSenderId(senderId);
        request.setType(MessageType.TEXT);
        request.setText("hello ws");
        when(chatService.sendMessage(conversationId, request))
                .thenThrow(new IllegalStateException("Conversation not found"));

        assertThatThrownBy(() -> controller.sendMessage(conversationId, request))
                .isInstanceOf(IllegalStateException.class);

        ArgumentCaptor<SocketEventResponseDTO> eventCaptor = ArgumentCaptor.forClass(SocketEventResponseDTO.class);
        verify(messagingTemplate).convertAndSendToUser(
                eq(senderId.toString()), eq("/queue/chat"), eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEvent()).isEqualTo("message_failed");
        assertThat(eventCaptor.getValue().getPayload()).isEqualTo("Conversation not found");
    }

    @Test
    void joinConversation_success_broadcastsJoinEvent_TC005() {
        ChatSocketController controller = new ChatSocketController(chatService, messagingTemplate);
        SocketConversationRequestDTO request = conversationRequest();

        controller.joinConversation(request);

        assertThat(capturedTopicEvent("/topic/conversations/" + request.getConversationId()).getEvent())
                .isEqualTo("join_conversation");
    }

    @Test
    void leaveConversation_success_broadcastsLeaveEvent_TC006() {
        ChatSocketController controller = new ChatSocketController(chatService, messagingTemplate);
        SocketConversationRequestDTO request = conversationRequest();

        controller.leaveConversation(request);

        assertThat(capturedTopicEvent("/topic/conversations/" + request.getConversationId()).getEvent())
                .isEqualTo("leave_conversation");
    }

    @Test
    void typingStop_success_broadcastsTypingStop_TC007() {
        ChatSocketController controller = new ChatSocketController(chatService, messagingTemplate);
        TypingRequestDTO request = new TypingRequestDTO();
        request.setConversationId(UUID.randomUUID());
        request.setUserId(UUID.randomUUID());

        controller.typingStop(request);

        assertThat(capturedTopicEvent("/topic/conversations/" + request.getConversationId()).getEvent())
                .isEqualTo("typing_stop");
    }

    @Test
    void handleSocketError_success_broadcastsFailureOnErrorQueue_TC008() {
        ChatSocketController controller = new ChatSocketController(chatService, messagingTemplate);

        controller.handleSocketError(new IllegalStateException("socket broken"));

        SocketEventResponseDTO event = capturedTopicEvent("/queue/chat/errors");
        assertThat(event.getEvent()).isEqualTo("message_failed");
        assertThat(event.getPayload()).isEqualTo("socket broken");
    }

    private SocketEventResponseDTO capturedTopicEvent(String destination) {
        ArgumentCaptor<SocketEventResponseDTO> eventCaptor = ArgumentCaptor.forClass(SocketEventResponseDTO.class);
        verify(messagingTemplate).convertAndSend(eq(destination), eventCaptor.capture());
        return eventCaptor.getValue();
    }

    private SocketConversationRequestDTO conversationRequest() {
        SocketConversationRequestDTO request = new SocketConversationRequestDTO();
        request.setConversationId(UUID.randomUUID());
        request.setUserId(UUID.randomUUID());
        return request;
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
