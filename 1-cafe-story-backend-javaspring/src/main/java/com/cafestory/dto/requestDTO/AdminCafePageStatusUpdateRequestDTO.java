package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.PageStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminCafePageStatusUpdateRequestDTO {

    @NotNull(message = "status is required")
    private PageStatus status;
}
