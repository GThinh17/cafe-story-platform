package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.AdminPayoutStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminPayoutStatusRequest {

    @NotNull
    private AdminPayoutStatus status;

    private String note;
}
