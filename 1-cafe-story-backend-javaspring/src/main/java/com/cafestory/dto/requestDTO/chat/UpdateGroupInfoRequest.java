package com.cafestory.dto.requestDTO.chat;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateGroupInfoRequest {

    private UUID actorUserId;

    private String groupName;

    private String groupAvatar;
}
