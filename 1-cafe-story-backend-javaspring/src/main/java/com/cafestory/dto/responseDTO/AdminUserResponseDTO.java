package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.UserRole;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AdminUserResponseDTO {
    private UUID userId;
    private String userName;
    private String userFullName;
    private String userEmail;
    private Long userPhone;
    private String userAvatar;
    private Integer userLike;
    private Integer userFollower;
    private Boolean accountStatus;
    private UUID regionId;
    private List<UserRole> roles;
}
