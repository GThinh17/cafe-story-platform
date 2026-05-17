package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UserRequestDTO {
    @NotNull(message = "User id is required")
    private UUID userId;
}
