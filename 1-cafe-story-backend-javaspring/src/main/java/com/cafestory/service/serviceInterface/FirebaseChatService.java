package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.ChatMessageResponseDTO;
import com.cafestory.dto.responseDTO.ConversationResponseDTO;

public interface FirebaseChatService {

    void saveConversation(ConversationResponseDTO conversation);

    void saveMessage(ChatMessageResponseDTO message);

    void updateLatestMessage(ConversationResponseDTO conversation, ChatMessageResponseDTO message);
}
