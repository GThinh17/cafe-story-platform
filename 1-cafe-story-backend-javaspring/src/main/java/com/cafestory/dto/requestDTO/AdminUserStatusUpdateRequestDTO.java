package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUserStatusUpdateRequestDTO {

    @NotNull(message = "accountStatus is required")
    private Boolean accountStatus;
}
