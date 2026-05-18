package com.cafestory.controller;

import com.cafestory.dto.requestDTO.chat.AddMemberRequest;
import com.cafestory.dto.requestDTO.chat.CreateDirectConversationRequest;
import com.cafestory.dto.requestDTO.chat.CreateGroupConversationRequest;
import com.cafestory.dto.requestDTO.chat.MemberActionRequest;
import com.cafestory.dto.requestDTO.chat.SendMessageRequest;
import com.cafestory.dto.requestDTO.chat.UpdateGroupInfoRequest;
import com.cafestory.dto.responseDTO.chat.ChatMessageResponseDTO;
import com.cafestory.dto.responseDTO.chat.ConversationResponseDTO;
import com.cafestory.service.serviceInterface.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/conversations/direct")
    public ConversationResponseDTO createOrGetDirectConversation(
            @Valid @RequestBody CreateDirectConversationRequest request) {
        return chatService.createOrGetDirectConversation(request);
    }

    @PostMapping("/conversations/group")
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponseDTO createGroupConversation(
            @Valid @RequestBody CreateGroupConversationRequest request) {
        return chatService.createGroupConversation(request);
    }

    @GetMapping("/users/{userId}/conversations")
    public List<ConversationResponseDTO> getUserConversations(@PathVariable UUID userId) {
        return chatService.getUserConversations(userId);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public List<ChatMessageResponseDTO> getMessagesByConversationId(
            @PathVariable UUID conversationId,
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return chatService.getMessagesByConversationId(conversationId, userId, page, size);
    }

    @PostMapping("/conversations/{conversationId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessageResponseDTO sendMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendMessageRequest request) {
        return chatService.sendMessage(conversationId, request);
    }

    @PostMapping("/conversations/{conversationId}/members")
    public ConversationResponseDTO addMember(
            @PathVariable UUID conversationId,
            @Valid @RequestBody AddMemberRequest request) {
        return chatService.addMember(conversationId, request.getActorUserId(), request.getMemberUserId());
    }

    @DeleteMapping("/conversations/{conversationId}/members/{memberUserId}")
    public ConversationResponseDTO removeMember(
            @PathVariable UUID conversationId,
            @PathVariable UUID memberUserId,
            @Valid @RequestBody MemberActionRequest request) {
        return chatService.removeMember(conversationId, request.getActorUserId(), memberUserId);
    }

    @PostMapping("/conversations/{conversationId}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveGroup(
            @PathVariable UUID conversationId,
            @Valid @RequestBody MemberActionRequest request) {
        chatService.leaveGroup(conversationId, request.getActorUserId());
    }

    @PatchMapping("/conversations/{conversationId}/group")
    public ConversationResponseDTO updateGroupInfo(
            @PathVariable UUID conversationId,
            @Valid @RequestBody UpdateGroupInfoRequest request) {
        return chatService.updateGroupInfo(conversationId, request);
    }
}
