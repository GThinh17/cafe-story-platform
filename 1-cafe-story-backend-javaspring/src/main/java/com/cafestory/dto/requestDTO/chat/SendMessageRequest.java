package com.cafestory.dto.requestDTO.chat;

import com.cafestory.entity.enums.MessageType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SendMessageRequest {

    private UUID senderId;

    @NotNull(message = "Message type is mandatory")
    private MessageType type;

    private String text;

    private List<String> imageUrls;

    private String stickerUrl;

    private String stickerId;
}
