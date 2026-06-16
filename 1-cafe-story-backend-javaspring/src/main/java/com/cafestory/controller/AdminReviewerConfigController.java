package com.cafestory.controller;

import com.cafestory.dto.requestDTO.ReviewerBadgeThresholdRequest;
import com.cafestory.dto.requestDTO.ReviewerScoringFormulaRequest;
import com.cafestory.dto.responseDTO.reviewer.ReviewerBadgeThresholdResponseDTO;
import com.cafestory.dto.responseDTO.reviewer.ReviewerRankingSnapshotResponseDTO;
import com.cafestory.dto.responseDTO.reviewer.ReviewerScoringFormulaResponseDTO;
import com.cafestory.entity.enums.RankingPeriodType;
import com.cafestory.service.serviceInterface.ReviewerBadgeThresholdService;
import com.cafestory.service.serviceInterface.ReviewerRankingSnapshotService;
import com.cafestory.service.serviceInterface.ReviewerScoringFormulaService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/admin/reviewer-config")
public class AdminReviewerConfigController {

    private final ReviewerScoringFormulaService formulaService;
    private final ReviewerBadgeThresholdService badgeThresholdService;
    private final ReviewerRankingSnapshotService snapshotService;

    public AdminReviewerConfigController(
            ReviewerScoringFormulaService formulaService,
            ReviewerBadgeThresholdService badgeThresholdService,
            ReviewerRankingSnapshotService snapshotService) {
        this.formulaService = formulaService;
        this.badgeThresholdService = badgeThresholdService;
        this.snapshotService = snapshotService;
    }

    @GetMapping("/formulas")
    public List<ReviewerScoringFormulaResponseDTO> getAllFormulas() {
        return formulaService.getAllFormulas();
    }

    @PostMapping("/formulas")
    public ReviewerScoringFormulaResponseDTO createFormula(
            @Valid @RequestBody ReviewerScoringFormulaRequest request,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return formulaService.createFormula(requireUserId(principal), request);
    }

    @PutMapping("/formulas/{id}/activate")
    public ReviewerScoringFormulaResponseDTO activateFormula(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return formulaService.activateFormula(requireUserId(principal), id);
    }

    @GetMapping("/formulas/{id}/thresholds")
    public List<ReviewerBadgeThresholdResponseDTO> getThresholds(@PathVariable UUID id) {
        return badgeThresholdService.getThresholds(id);
    }

    @PutMapping("/formulas/{id}/thresholds")
    public List<ReviewerBadgeThresholdResponseDTO> updateThresholds(
            @PathVariable UUID id,
            @Valid @RequestBody List<ReviewerBadgeThresholdRequest> thresholds,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return badgeThresholdService.updateThresholds(requireUserId(principal), id, thresholds);
    }

    @PostMapping("/ranking/generate")
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
