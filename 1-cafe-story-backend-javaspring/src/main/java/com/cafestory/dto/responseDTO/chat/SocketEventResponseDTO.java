package com.cafestory.dto.responseDTO.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocketEventResponseDTO {

    private String event;

    private UUID conversationId;

    private UUID userId;

    private Object payload;
}
