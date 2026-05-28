package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.util.List;

@Data
public class BlogFeedCursorPageResponseDTO {
    private List<BlogFeedResponse> items;
    private String nextCursor;
    private Boolean hasMore;
}
