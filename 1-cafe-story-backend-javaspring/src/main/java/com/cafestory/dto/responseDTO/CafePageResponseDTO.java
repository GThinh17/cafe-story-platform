package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.PageStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CafePageResponseDTO {
    private UUID id;
    private UUID ownerUserId;
    private UUID regionId;
    private String name;
    private String address;
    private String description;
    private String avatarUrl;
    private String coverUrl;
    private PageStatus status;
    private Integer likeCount;
    private Integer followerCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
