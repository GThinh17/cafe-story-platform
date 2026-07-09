package com.cafestory.controller;

import com.cafestory.dto.responseDTO.RagTopReviewerResponseDTO;
import com.cafestory.dto.responseDTO.RagTrendingCafeResponseDTO;
import com.cafestory.service.serviceInterface.RagToolService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/internal/rag")
public class RagSqlToolController {

    private final RagToolService ragToolService;

    public RagSqlToolController(RagToolService ragToolService) {
        this.ragToolService = ragToolService;
    }

    @GetMapping("/trending-cafes")
    public List<RagTrendingCafeResponseDTO> getTrendingCafes(
            @RequestParam(value = "province", required = false) String province,
            @RequestParam(value = "limit", required = false, defaultValue = "5") int limit) {
        return ragToolService.getTrendingCafes(province, limit);
    }

    @GetMapping("/top-reviewers")
    public List<RagTopReviewerResponseDTO> getTopReviewers(
            @RequestParam(value = "limit", required = false, defaultValue = "5") int limit) {
        return ragToolService.getTopReviewers(limit);
    }
}
