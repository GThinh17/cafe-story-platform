package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.util.UUID;

@Data
public class UserResponseDTO {
    private UUID userId;
    private String userName;
    private String userFullName;
    private String userEmail;
    private Long userPhone;
    private String userAvatar;
    private Integer userLike;
    private Integer userFollower;
    private Boolean accountStatus;
}
