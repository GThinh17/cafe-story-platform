package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateGroupInfoRequestDTO {

    private UUID actorUserId;

    private String groupName;

    private String groupAvatar;
}
