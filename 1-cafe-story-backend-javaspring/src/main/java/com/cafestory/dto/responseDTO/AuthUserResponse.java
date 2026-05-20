package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AuthUserResponse {
    private UUID userId;
    private String userName;
    private String userFullName;
    private String userEmail;
    private Long userPhone;
    private String userAvatar;
    private Boolean accountStatus;
    private List<String> roles;
}
