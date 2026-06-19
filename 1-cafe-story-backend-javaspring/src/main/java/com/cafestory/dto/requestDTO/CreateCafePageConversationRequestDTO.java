package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateCafePageConversationRequestDTO {

    private UUID userId;

    @NotNull(message = "Cafe page id is mandatory")
    private UUID cafePageId;
}
