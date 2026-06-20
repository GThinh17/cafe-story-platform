package com.cafestory.controller;

import com.cafestory.dto.requestDTO.SendMessageRequestDTO;
import com.cafestory.dto.requestDTO.SocketConversationRequestDTO;
import com.cafestory.dto.requestDTO.TypingRequestDTO;
import com.cafestory.dto.responseDTO.ChatMessageResponseDTO;
import com.cafestory.dto.responseDTO.SocketEventResponseDTO;
import com.cafestory.service.serviceInterface.ChatService;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class ChatSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/join_conversation")
    public void joinConversation(@Valid SocketConversationRequestDTO request) {
        messagingTemplate.convertAndSend(
                topic(request.getConversationId()),
                new SocketEventResponseDTO("join_conversation", request.getConversationId(), request.getUserId(), null));
    }

    @MessageMapping("/chat/leave_conversation")
    public void leaveConversation(@Valid SocketConversationRequestDTO request) {
        messagingTemplate.convertAndSend(
                topic(request.getConversationId()),
                new SocketEventResponseDTO("leave_conversation", request.getConversationId(), request.getUserId(), null));
    }

    @MessageMapping("/chat/{conversationId}/send_message")
    public void sendMessage(
            @DestinationVariable UUID conversationId,
            @Valid SendMessageRequestDTO request) {
        try {
            ChatMessageResponseDTO message = chatService.sendMessage(conversationId, request);
            messagingTemplate.convertAndSendToUser(
                    request.getSenderId().toString(),
                    "/queue/chat",
                    new SocketEventResponseDTO("message_sent", conversationId, request.getSenderId(), message));
        } catch (RuntimeException ex) {
            messagingTemplate.convertAndSendToUser(
                    request.getSenderId().toString(),
                    "/queue/chat",
                    new SocketEventResponseDTO("message_failed", conversationId, request.getSenderId(), ex.getMessage()));
            throw ex;
        }
    }

    @MessageMapping("/chat/typing_start")
    public void typingStart(@Valid TypingRequestDTO request) {
        messagingTemplate.convertAndSend(
                topic(request.getConversationId()),
                new SocketEventResponseDTO("typing_start", request.getConversationId(), request.getUserId(), null));
    }

    @MessageMapping("/chat/typing_stop")
    public void typingStop(@Valid TypingRequestDTO request) {
        messagingTemplate.convertAndSend(
                topic(request.getConversationId()),
                new SocketEventResponseDTO("typing_stop", request.getConversationId(), request.getUserId(), null));
    }

    @MessageExceptionHandler
    public void handleSocketError(Exception ex) {
        messagingTemplate.convertAndSend("/queue/chat/errors", new SocketEventResponseDTO("message_failed", null, null, ex.getMessage()));
    }

    private String topic(UUID conversationId) {
        return "/topic/conversations/" + conversationId;
    }
}
