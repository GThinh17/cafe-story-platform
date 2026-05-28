package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.util.List;

@Data
public class FeedResponseDTO {
    private List<FeedItemResponseDTO> items;
    private String nextCursor;
    private Boolean hasMore;
}
