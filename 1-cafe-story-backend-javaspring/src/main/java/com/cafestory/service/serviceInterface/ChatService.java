package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.CreateDirectConversationRequestDTO;
import com.cafestory.dto.requestDTO.CreateCafePageConversationRequestDTO;
import com.cafestory.dto.requestDTO.CreateGroupConversationRequestDTO;
import com.cafestory.dto.requestDTO.SendMessageRequestDTO;
import com.cafestory.dto.requestDTO.UpdateGroupInfoRequestDTO;
import com.cafestory.dto.responseDTO.ChatMessageResponseDTO;
import com.cafestory.dto.responseDTO.ConversationResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ChatService {

    ConversationResponseDTO createOrGetDirectConversation(CreateDirectConversationRequestDTO request);

    ConversationResponseDTO createOrGetCafePageConversation(CreateCafePageConversationRequestDTO request);

    ConversationResponseDTO createGroupConversation(CreateGroupConversationRequestDTO request);

    List<ConversationResponseDTO> getUserConversations(UUID userId);

    List<ConversationResponseDTO> getCafePageConversations(UUID cafePageId, UUID viewerUserId);

    List<ChatMessageResponseDTO> getMessagesByConversationId(UUID conversationId, UUID userId, int page, int size);

    ChatMessageResponseDTO sendMessage(UUID conversationId, SendMessageRequestDTO request);

    ConversationResponseDTO addMember(UUID conversationId, UUID actorUserId, UUID memberUserId);

    ConversationResponseDTO removeMember(UUID conversationId, UUID actorUserId, UUID memberUserId);

    void leaveGroup(UUID conversationId, UUID actorUserId);

    ConversationResponseDTO updateGroupInfo(UUID conversationId, UpdateGroupInfoRequestDTO request);
}
