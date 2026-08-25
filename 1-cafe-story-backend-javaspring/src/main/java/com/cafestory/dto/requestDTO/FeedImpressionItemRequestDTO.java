package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.UUID;

@Data
public class FeedImpressionItemRequestDTO {

    @NotNull(message = "Blog id is mandatory")
    private UUID blogId;

    @NotNull(message = "Position is mandatory")
    @PositiveOrZero(message = "Position must be zero or positive")
    private Integer position;
}
