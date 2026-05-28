package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.PageStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CafePageRankingResponseDTO {
    private UUID id;
    private UUID ownerUserId;
    private UUID regionId;
    private String regionCity;
    private String regionProvince;
    private String regionArea;
    private String name;
    private String address;
    private String description;
    private String avatarUrl;
    private String coverUrl;
    private PageStatus status;
    private Integer likeCount;
    private Integer followerCount;
    private Boolean pageActive;
    private Double rankingScore;
    private Integer rankPosition;
    private LocalDateTime createdAt;
}
