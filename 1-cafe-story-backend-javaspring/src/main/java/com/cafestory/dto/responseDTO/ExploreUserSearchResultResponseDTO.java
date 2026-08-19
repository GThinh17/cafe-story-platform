package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.util.UUID;

@Data
public class ExploreUserSearchResultResponseDTO {
    private UUID userId;
    private String userName;
    private String userFullName;
    private String userAvatar;
    private String regionCity;
}
