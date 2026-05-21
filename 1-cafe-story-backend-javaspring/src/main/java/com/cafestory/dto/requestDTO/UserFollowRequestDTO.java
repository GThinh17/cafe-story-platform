package com.cafestory.dto.requestDTO;

import lombok.Data;

import java.util.UUID;

@Data
public class UserFollowRequestDTO {

    private UUID followerUserId;
}
