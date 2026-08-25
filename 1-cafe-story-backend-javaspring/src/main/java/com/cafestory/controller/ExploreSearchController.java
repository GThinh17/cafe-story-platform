package com.cafestory.controller;

import com.cafestory.dto.responseDTO.ExploreSearchResponseDTO;
import com.cafestory.service.serviceInterface.ExploreSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class ExploreSearchController {

    private final ExploreSearchService exploreSearchService;

    public ExploreSearchController(ExploreSearchService exploreSearchService) {
        this.exploreSearchService = exploreSearchService;
    }

    @GetMapping
    public ExploreSearchResponseDTO search(
            @RequestParam String query,
            @RequestParam(defaultValue = "8") int size) {
        return exploreSearchService.search(query, size);
    }
}
