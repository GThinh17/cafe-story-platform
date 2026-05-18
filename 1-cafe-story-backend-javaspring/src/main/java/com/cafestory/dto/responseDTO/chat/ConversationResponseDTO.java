package com.cafestory.dto.responseDTO.chat;

import com.cafestory.entity.enums.ConversationType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ConversationResponseDTO {

    private UUID id;

    private ConversationType type;

    private String groupName;

    private String groupAvatar;

    private UUID latestMessageId;

    private String latestMessagePreview;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<ChatMemberResponseDTO> members;
}
