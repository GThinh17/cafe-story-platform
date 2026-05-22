package com.cafestory.dto.requestDTO.chat;

import lombok.Data;

import java.util.UUID;

@Data
public class MemberActionRequest {

    private UUID actorUserId;
}
