package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserFollowResponseDTO {
    private UUID id;
    private UUID followerUserId;
    private UUID followingUserId;
    private LocalDateTime createdAt;
}
