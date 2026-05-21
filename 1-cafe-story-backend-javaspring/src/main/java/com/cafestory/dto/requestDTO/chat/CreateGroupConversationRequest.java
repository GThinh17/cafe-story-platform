package com.cafestory.dto.requestDTO.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateGroupConversationRequest {

    private UUID creatorUserId;

    @NotBlank(message = "Group name is mandatory")
    private String groupName;

    private String groupAvatar;

    @NotEmpty(message = "Member ids are mandatory")
    private List<UUID> memberIds;
}
