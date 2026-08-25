package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.ModerationResolveAction;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminModerationResolveRequestDTO {

    @NotNull(message = "action is required")
    private ModerationResolveAction action;
}
