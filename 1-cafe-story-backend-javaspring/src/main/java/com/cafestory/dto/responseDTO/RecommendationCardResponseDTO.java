package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.RecommendationTargetType;
import lombok.Data;

import java.util.UUID;

@Data
public class RecommendationCardResponseDTO {
    private UUID targetId;
    private RecommendationTargetType targetType;
    private String avatar;
    private String username;
    private String fullName;
    private String reason;
    private String city;
}
