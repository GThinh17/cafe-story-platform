package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AddMemberRequestDTO {

    private UUID actorUserId;

    @NotNull(message = "Member user id is mandatory")
    private UUID memberUserId;
}
