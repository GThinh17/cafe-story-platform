package com.cafestory.dto.responseDTO.chat;

import com.cafestory.entity.enums.MemberRole;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ChatMemberResponseDTO {

    private UUID id;

    private UUID userId;

    private MemberRole role;

    private LocalDateTime joinedAt;
}
