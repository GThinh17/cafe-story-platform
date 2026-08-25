package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagUserBlogModerationRequestDTO {

    @NotBlank(message = "userJwt is mandatory")
    private String userJwt;

    private Integer limit;
}
