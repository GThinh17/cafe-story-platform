package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.AdminAssistantConversationCreateRequestDTO;
import com.cafestory.dto.requestDTO.AdminAssistantMessageRequestDTO;
import com.cafestory.dto.responseDTO.AdminAssistantChatResponseDTO;
import com.cafestory.dto.responseDTO.AdminAssistantConversationResponseDTO;
import com.cafestory.dto.responseDTO.AdminAssistantDraftActionResponseDTO;
import com.cafestory.dto.responseDTO.AdminAssistantMessageResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

public interface AdminAssistantService {

    AdminAssistantConversationResponseDTO createConversation(UUID adminUserId, AdminAssistantConversationCreateRequestDTO request);

    Page<AdminAssistantConversationResponseDTO> getConversations(UUID adminUserId, Pageable pageable);

    Page<AdminAssistantMessageResponseDTO> getMessages(UUID adminUserId, UUID conversationId, Pageable pageable);

    AdminAssistantChatResponseDTO sendMessage(UUID adminUserId, UUID conversationId, AdminAssistantMessageRequestDTO request);

    SseEmitter streamMessage(UUID adminUserId, UUID conversationId, AdminAssistantMessageRequestDTO request);

    AdminAssistantDraftActionResponseDTO getDraftAction(UUID adminUserId, UUID draftActionId);

    AdminAssistantDraftActionResponseDTO executeDraftAction(UUID adminUserId, UUID draftActionId);
}
