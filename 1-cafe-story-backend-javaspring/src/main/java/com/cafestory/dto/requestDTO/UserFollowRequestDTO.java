package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UserFollowRequestDTO {

    @NotNull(message = "Follower user id is mandatory")
    private UUID followerUserId;
}
