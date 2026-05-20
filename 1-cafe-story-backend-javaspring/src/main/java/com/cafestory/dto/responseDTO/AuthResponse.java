package com.cafestory.dto.responseDTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class AuthResponse {
    private AuthUserResponse user;

    @JsonIgnore
    private String accessToken;

    @JsonIgnore
    private String refreshToken;
}
