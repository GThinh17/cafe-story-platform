package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.util.UUID;

@Data
public class ExploreCafePageSearchResultResponseDTO {
    private UUID id;
    private String name;
    private String avatarUrl;
    private String address;
    private String regionCity;
}
