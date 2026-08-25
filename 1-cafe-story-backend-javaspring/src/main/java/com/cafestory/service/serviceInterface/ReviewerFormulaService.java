package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.ReviewerFormulaRequestDTO;
import com.cafestory.dto.responseDTO.ReviewerFormulaResponseDTO;
import com.cafestory.entity.ReviewerFormula;

import java.util.List;
import java.util.UUID;

public interface ReviewerFormulaService {

    ReviewerFormula getActiveFormula();

    ReviewerFormulaResponseDTO createFormula(UUID adminUserId, ReviewerFormulaRequestDTO request);

    ReviewerFormulaResponseDTO activateFormula(UUID adminUserId, UUID formulaId);

    List<ReviewerFormulaResponseDTO> getAllFormulas();

    long calculateScore(long likeCount, long shareCount, long commentCount);

    long calculatePayout(long likeCount, long shareCount, long commentCount);
}
