package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PageMemberAddRequestDTO {

    private UUID actorUserId;

    @NotNull(message = "User id is mandatory")
    private UUID userId;

    private String roleName;
}
