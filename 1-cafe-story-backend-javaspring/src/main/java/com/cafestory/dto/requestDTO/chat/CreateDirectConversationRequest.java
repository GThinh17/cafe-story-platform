package com.cafestory.dto.requestDTO.chat;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateDirectConversationRequest {

    private UUID firstUserId;

    @NotNull(message = "Second user id is mandatory")
    private UUID secondUserId;
}
