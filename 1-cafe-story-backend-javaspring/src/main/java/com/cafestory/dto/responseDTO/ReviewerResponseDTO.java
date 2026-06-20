package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.ReviewerBadge;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReviewerResponseDTO {
    private UUID reviewerId;
    private UUID userId;
    private String role;
    private String avatar;
    private RegionResponseDTO region;
    private String name;
    private int follower;
    private int follow;
    private int like;
    private ReviewerBadge badge;
    private long score;
    private LocalDateTime expireDate;
}
