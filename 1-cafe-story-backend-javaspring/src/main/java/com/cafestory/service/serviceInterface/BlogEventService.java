package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.BlogEventResponse;
import com.cafestory.entity.enums.BlogEventType;

import java.util.UUID;

public interface BlogEventService {
    BlogEventResponse recordEvent(UUID blogId, UUID userId, BlogEventType eventType, Double weight);
}
