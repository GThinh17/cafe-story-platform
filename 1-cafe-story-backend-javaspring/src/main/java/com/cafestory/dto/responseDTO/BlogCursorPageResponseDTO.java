package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.util.List;

@Data
public class BlogCursorPageResponseDTO {
    private List<BlogResponseDTO> items;
    private String nextCursor;
    private Boolean hasMore;
}
