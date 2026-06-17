package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.ReviewerScoringFormulaRequest;
import com.cafestory.dto.responseDTO.reviewer.ReviewerScoringFormulaResponseDTO;
import com.cafestory.entity.ReviewerScoringFormula;

import java.util.List;
import java.util.UUID;

public interface ReviewerScoringFormulaService {

    ReviewerScoringFormula getActiveFormula();

    ReviewerScoringFormulaResponseDTO createFormula(UUID adminUserId, ReviewerScoringFormulaRequest request);

    ReviewerScoringFormulaResponseDTO activateFormula(UUID adminUserId, UUID formulaId);

    List<ReviewerScoringFormulaResponseDTO> getAllFormulas();

    long calculateScore(long likeCount, long shareCount, long commentCount);

    long calculatePayout(long likeCount, long shareCount, long commentCount);
}
