package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BlogInteractionRequestDTO {

    @NotNull(message = "User id is mandatory")
    private UUID userId;
}
