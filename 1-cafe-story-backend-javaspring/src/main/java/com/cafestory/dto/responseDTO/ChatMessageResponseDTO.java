package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.MessageStatus;
import com.cafestory.entity.enums.MessageType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ChatMessageResponseDTO {

    private UUID id;

    private UUID conversationId;

    private UUID senderId;

    private MessageType type;

    private String text;

    private List<String> imageUrls;

    private String stickerUrl;

    private String stickerId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private MessageStatus status;

    private boolean isRead;

    private LocalDateTime readAt;
}
