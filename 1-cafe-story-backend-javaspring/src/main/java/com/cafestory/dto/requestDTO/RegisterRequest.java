package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username is mandatory")
    @Size(max = 255)
    private String userName;

    @Size(max = 255)
    private String userFullName;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 6, max = 255)
    private String password;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is mandatory")
    private String userEmail;

    private Long userPhone;

    private String userAvatar;

    private String userDescription;
}
