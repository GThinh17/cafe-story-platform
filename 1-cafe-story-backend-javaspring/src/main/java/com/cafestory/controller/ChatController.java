package com.cafestory.controller;

import com.cafestory.dto.requestDTO.AddMemberRequestDTO;
import com.cafestory.dto.requestDTO.CreateCafePageConversationRequestDTO;
import com.cafestory.dto.requestDTO.CreateDirectConversationRequestDTO;
import com.cafestory.dto.requestDTO.CreateGroupConversationRequestDTO;
import com.cafestory.dto.requestDTO.SendMessageRequestDTO;
import com.cafestory.dto.requestDTO.UpdateGroupInfoRequestDTO;
import com.cafestory.dto.responseDTO.ChatMessageResponseDTO;
import com.cafestory.dto.responseDTO.ConversationResponseDTO;
import com.cafestory.service.serviceInterface.ChatService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/conversations/direct")
    public ConversationResponseDTO createOrGetDirectConversation(
            @Valid @RequestBody CreateDirectConversationRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        request.setFirstUserId(requireUserId(principal));
        return chatService.createOrGetDirectConversation(request);
    }

    @PostMapping("/conversations/cafe-page")
    public ConversationResponseDTO createOrGetCafePageConversation(
            @Valid @RequestBody CreateCafePageConversationRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        request.setUserId(requireUserId(principal));
        return chatService.createOrGetCafePageConversation(request);
    }

    @PostMapping("/conversations/group")
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponseDTO createGroupConversation(
            @Valid @RequestBody CreateGroupConversationRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        request.setCreatorUserId(requireUserId(principal));
        return chatService.createGroupConversation(request);
    }

    @GetMapping("/conversations")
    public List<ConversationResponseDTO> getUserConversations(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return chatService.getUserConversations(requireUserId(principal));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public List<ChatMessageResponseDTO> getMessagesByConversationId(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return chatService.getMessagesByConversationId(conversationId, requireUserId(principal), page, size);
    }

    @PostMapping("/conversations/{conversationId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessageResponseDTO sendMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendMessageRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        request.setSenderId(requireUserId(principal));
        return chatService.sendMessage(conversationId, request);
    }

    @PostMapping("/conversations/{conversationId}/members")
    public ConversationResponseDTO addMember(
            @PathVariable UUID conversationId,
            @Valid @RequestBody AddMemberRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return chatService.addMember(conversationId, requireUserId(principal), request.getMemberUserId());
    }

    @DeleteMapping("/conversations/{conversationId}/members/{memberUserId}")
    public ConversationResponseDTO removeMember(
            @PathVariable UUID conversationId,
            @PathVariable UUID memberUserId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return chatService.removeMember(conversationId, requireUserId(principal), memberUserId);
    }

    @PostMapping("/conversations/{conversationId}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveGroup(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        chatService.leaveGroup(conversationId, requireUserId(principal));
    }

    @PatchMapping("/conversations/{conversationId}/group")
    public ConversationResponseDTO updateGroupInfo(
            @PathVariable UUID conversationId,
            @Valid @RequestBody UpdateGroupInfoRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        request.setActorUserId(requireUserId(principal));
        return chatService.updateGroupInfo(conversationId, request);
    }
}
