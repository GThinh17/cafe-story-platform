package com.cafestory.dto.responseDTO;

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

    private String chatName;

    private String userName;

    private String chatAvatar;

    private UUID latestMessageId;

    private String latestMessagePreview;

    private String lastMessage;

    private LocalDateTime lastMessageAt;

    private boolean isRead;

    private long unreadCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<ChatMemberResponseDTO> members;
}
