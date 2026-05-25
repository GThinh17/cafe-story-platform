package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminExtraFeeStatusUpdateRequestDTO {

    @NotNull(message = "status is required")
    private Boolean status;
}
