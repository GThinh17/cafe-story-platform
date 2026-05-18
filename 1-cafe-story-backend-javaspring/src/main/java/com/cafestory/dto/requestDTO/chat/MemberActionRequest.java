package com.cafestory.dto.requestDTO.chat;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MemberActionRequest {

    @NotNull(message = "Actor user id is mandatory")
    private UUID actorUserId;
}
