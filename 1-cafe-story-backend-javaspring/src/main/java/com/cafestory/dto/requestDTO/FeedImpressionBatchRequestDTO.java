package com.cafestory.dto.requestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class FeedImpressionBatchRequestDTO {

    @Valid
    @NotEmpty(message = "Items must not be empty")
    private List<FeedImpressionItemRequestDTO> items;
}
