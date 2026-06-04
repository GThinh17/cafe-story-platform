package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.util.UUID;

@Data
public class BlogTaggedUserResponseDTO {
    private UUID id;
    private String userName;
}
