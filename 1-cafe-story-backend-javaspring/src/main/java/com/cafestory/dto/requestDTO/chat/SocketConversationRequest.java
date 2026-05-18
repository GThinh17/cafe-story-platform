package com.cafestory.dto.requestDTO.chat;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SocketConversationRequest {

    @NotNull(message = "Conversation id is mandatory")
    private UUID conversationId;

    @NotNull(message = "User id is mandatory")
    private UUID userId;
}
