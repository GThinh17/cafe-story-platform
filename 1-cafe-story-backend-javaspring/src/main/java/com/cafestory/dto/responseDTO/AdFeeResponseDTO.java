package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.AdFeeType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AdFeeResponseDTO {
    private UUID adFeeId;
    private AdFeeType feeType;
    private BigDecimal price;
    private String currency;
    private Boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
