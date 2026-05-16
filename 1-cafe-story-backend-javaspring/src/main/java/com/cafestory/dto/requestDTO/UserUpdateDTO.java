package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {

    @Size(max = 255)
    private String userName;

    @Size(max = 255)
    private String userFullName;

    @Size(max = 255)
    private String userPassword;

    @Email(message = "Email should be valid")
    private String userEmail;

    private Long userPhone;

    private String userAvatar;

    private Boolean accountStatus;
}
