package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.BlogEventType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BlogEventRequest {

    private UUID userId;

    @NotNull(message = "Event type is mandatory")
    private BlogEventType eventType;

    private Double weight;
}
