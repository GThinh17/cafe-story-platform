package com.cafestory.controller;

import com.cafestory.config.AdminAssistantSecurityProperties;
import com.cafestory.dto.requestDTO.AdminAssistantConversationCreateRequestDTO;
import com.cafestory.dto.requestDTO.AdminAssistantMessageRequestDTO;
import com.cafestory.dto.requestDTO.AdminAssistantToolRequestDTO;
import com.cafestory.dto.responseDTO.AdminAssistantChatResponseDTO;
import com.cafestory.dto.responseDTO.AdminAssistantConversationResponseDTO;
import com.cafestory.dto.responseDTO.AdminAssistantDraftActionResponseDTO;
import com.cafestory.dto.responseDTO.AdminAssistantMessageResponseDTO;
import com.cafestory.dto.responseDTO.AdminAssistantToolResponseDTO;
import com.cafestory.service.serviceInterface.AdminAssistantService;
import com.cafestory.service.serviceInterface.AdminAssistantToolService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/admin/assistant")
public class AdminAssistantController {

    private final AdminAssistantService assistantService;
    private final AdminAssistantToolService toolService;
    private final String toolToken;

    public AdminAssistantController(
            AdminAssistantService assistantService,
            AdminAssistantToolService toolService,
            AdminAssistantSecurityProperties securityProperties) {
        this.assistantService = assistantService;
        this.toolService = toolService;
        this.toolToken = securityProperties.getToolToken();
    }

    @PostMapping("/conversations")
    public AdminAssistantConversationResponseDTO createConversation(
            @Valid @RequestBody(required = false) AdminAssistantConversationCreateRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return assistantService.createConversation(requireUserId(principal), request);
    }

    @GetMapping("/conversations")
    public Page<AdminAssistantConversationResponseDTO> getConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return assistantService.getConversations(requireUserId(principal), pageable(page, size, "updatedAt"));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public Page<AdminAssistantMessageResponseDTO> getMessages(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return assistantService.getMessages(requireUserId(principal), conversationId, pageable(page, size, "createdAt"));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public AdminAssistantChatResponseDTO sendMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody AdminAssistantMessageRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return assistantService.sendMessage(requireUserId(principal), conversationId, request);
    }

    @PostMapping(
            value = "/conversations/{conversationId}/messages/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody AdminAssistantMessageRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return assistantService.streamMessage(requireUserId(principal), conversationId, request);
    }

    @GetMapping("/draft-actions/{draftActionId}")
    public AdminAssistantDraftActionResponseDTO getDraftAction(
            @PathVariable UUID draftActionId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return assistantService.getDraftAction(requireUserId(principal), draftActionId);
    }

    @PostMapping("/draft-actions/{draftActionId}/execute")
    public AdminAssistantDraftActionResponseDTO executeDraftAction(
            @PathVariable UUID draftActionId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return assistantService.executeDraftAction(requireUserId(principal), draftActionId);
    }

    @PostMapping("/tools/{toolName}")
    public AdminAssistantToolResponseDTO executeTool(
            @PathVariable String toolName,
            @RequestHeader(name = "X-Admin-Assistant-Tool-Token", required = false) String requestToken,
            @Valid @RequestBody(required = false) AdminAssistantToolRequestDTO request) {
        if (toolToken == null || toolToken.isBlank() || !toolToken.equals(requestToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Assistant tool token is invalid");
        }
        return toolService.executeTool(toolName, request, request == null ? null : request.getAdminUserId());
    }

    private Pageable pageable(int page, int size, String sortProperty) {
        return PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 100),
                Sort.by(Sort.Direction.DESC, sortProperty));
    }
}
