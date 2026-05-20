package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Identifier is mandatory")
    private String identifier;

    @NotBlank(message = "Password is mandatory")
    private String password;
}
