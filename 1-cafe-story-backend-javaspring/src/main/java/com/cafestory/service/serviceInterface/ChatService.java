package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.chat.CreateDirectConversationRequest;
import com.cafestory.dto.requestDTO.chat.CreateGroupConversationRequest;
import com.cafestory.dto.requestDTO.chat.SendMessageRequest;
import com.cafestory.dto.requestDTO.chat.UpdateGroupInfoRequest;
import com.cafestory.dto.responseDTO.chat.ChatMessageResponseDTO;
import com.cafestory.dto.responseDTO.chat.ConversationResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ChatService {

    ConversationResponseDTO createOrGetDirectConversation(CreateDirectConversationRequest request);

    ConversationResponseDTO createGroupConversation(CreateGroupConversationRequest request);

    List<ConversationResponseDTO> getUserConversations(UUID userId);

    List<ChatMessageResponseDTO> getMessagesByConversationId(UUID conversationId, UUID userId, int page, int size);

    ChatMessageResponseDTO sendMessage(UUID conversationId, SendMessageRequest request);

    ConversationResponseDTO addMember(UUID conversationId, UUID actorUserId, UUID memberUserId);

    ConversationResponseDTO removeMember(UUID conversationId, UUID actorUserId, UUID memberUserId);

    void leaveGroup(UUID conversationId, UUID actorUserId);

    ConversationResponseDTO updateGroupInfo(UUID conversationId, UpdateGroupInfoRequest request);
}
