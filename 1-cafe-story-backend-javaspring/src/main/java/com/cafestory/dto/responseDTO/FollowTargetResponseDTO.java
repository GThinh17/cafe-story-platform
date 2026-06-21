package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.FollowTargetType;
import com.cafestory.entity.enums.PageStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FollowTargetResponseDTO {
    private UUID followId;
    private FollowTargetType targetType;
    private UUID targetId;
    private String displayName;
    private String username;
    private String avatar;
    private String city;
    private LocalDateTime followedAt;

    private UUID userId;
    private String userFullName;

    private UUID cafePageId;
    private String pageName;
    private UUID ownerUserId;
    private PageStatus pageStatus;
    private Boolean pageActive;
}
