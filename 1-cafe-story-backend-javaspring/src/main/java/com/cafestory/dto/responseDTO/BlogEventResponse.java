package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.BlogEventType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BlogEventResponse {
    private UUID id;
    private UUID blogId;
    private UUID userId;
    private BlogEventType eventType;
    private Double weight;
    private LocalDateTime createdAt;
}
