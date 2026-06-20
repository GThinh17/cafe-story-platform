package com.cafestory.controller;

import com.cafestory.dto.responseDTO.ReviewerRankingSnapshotResponseDTO;
import com.cafestory.entity.enums.RankingPeriodType;
import com.cafestory.service.serviceInterface.ReviewerRankingSnapshotService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/admin/reviewer-config")
public class AdminReviewerConfigController {

    private final ReviewerRankingSnapshotService snapshotService;

    public AdminReviewerConfigController(ReviewerRankingSnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }

    @PostMapping("/ranking/generate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void generateRankingSnapshot(
            @RequestParam(defaultValue = "DAILY") RankingPeriodType periodType,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        requireUserId(principal);
        snapshotService.generateSnapshot(periodType);
    }

    @GetMapping("/ranking")
    public List<ReviewerRankingSnapshotResponseDTO> getRanking(
            @RequestParam String period,
            @RequestParam(defaultValue = "DAILY") RankingPeriodType periodType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return snapshotService.getRanking(period, periodType, page, limit);
    }
}
