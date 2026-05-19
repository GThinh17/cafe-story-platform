package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.ExtraFeeType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ExtraFeeResponseDTO {

    private UUID extraFeeId;

    private String name;

    private String description;

    private ExtraFeeType feeType;

    private long price;

    private Integer durationMonths;

    private Boolean status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
