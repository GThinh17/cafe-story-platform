package com.cafestory.controller;

import com.cafestory.dto.requestDTO.ReviewerBadgeThresholdRequest;
import com.cafestory.dto.requestDTO.ReviewerFormulaRequestDTO;
import com.cafestory.dto.responseDTO.ReviewerFormulaResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerBadgeThresholdResponseDTO;
import com.cafestory.service.serviceInterface.ReviewerBadgeThresholdService;
import com.cafestory.service.serviceInterface.ReviewerFormulaService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/admin/formulas")
public class AdminFormulaController {

    private final ReviewerFormulaService formulaService;
    private final ReviewerBadgeThresholdService badgeThresholdService;

    public AdminFormulaController(ReviewerFormulaService formulaService,
                                  ReviewerBadgeThresholdService badgeThresholdService) {
        this.formulaService = formulaService;
        this.badgeThresholdService = badgeThresholdService;
    }

    @GetMapping
    public List<ReviewerFormulaResponseDTO> getAllFormulas() {
        return formulaService.getAllFormulas();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewerFormulaResponseDTO createFormula(
            @Valid @RequestBody ReviewerFormulaRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return formulaService.createFormula(requireUserId(principal), request);
    }

    @PutMapping("/{id}/activate")
    public ReviewerFormulaResponseDTO activateFormula(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return formulaService.activateFormula(requireUserId(principal), id);
    }

    @GetMapping("/{id}/thresholds")
    public List<ReviewerBadgeThresholdResponseDTO> getThresholds(@PathVariable UUID id) {
        return badgeThresholdService.getThresholds(id);
    }

    @PutMapping("/{id}/thresholds")
    public List<ReviewerBadgeThresholdResponseDTO> updateThresholds(
            @PathVariable UUID id,
            @Valid @RequestBody List<ReviewerBadgeThresholdRequest> thresholds,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return badgeThresholdService.updateThresholds(requireUserId(principal), id, thresholds);
    }
}
