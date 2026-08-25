package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.util.List;

@Data
public class ExploreSearchResponseDTO {
    private List<ExploreBlogSearchResultResponseDTO> blogs;
    private List<ExploreCafePageSearchResultResponseDTO> cafePages;
    private List<ExploreUserSearchResultResponseDTO> users;
}
