package com.cafestory.dto.responseDTO.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocketEventResponseDTO {

    @JsonProperty("type")
    private String event;

    private UUID conversationId;

    private UUID userId;

    @JsonProperty("data")
    private Object payload;
}
