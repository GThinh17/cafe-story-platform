package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.ReviewerScoringFormulaRequest;
import com.cafestory.dto.responseDTO.reviewer.ReviewerScoringFormulaResponseDTO;
import com.cafestory.entity.ReviewerScoringFormula;
import com.cafestory.entity.User;
import com.cafestory.repository.ReviewerScoringFormulaRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceInterface.ReviewerScoringFormulaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewerScoringFormulaServiceImpl implements ReviewerScoringFormulaService {

    private final ReviewerScoringFormulaRepository formulaRepository;
    private final UserRepository userRepository;

    public ReviewerScoringFormulaServiceImpl(
            ReviewerScoringFormulaRepository formulaRepository,
            UserRepository userRepository) {
        this.formulaRepository = formulaRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ReviewerScoringFormula getActiveFormula() {
        return formulaRepository.findByActiveTrue()
                .orElseGet(this::createAndActivateDefaultFormula);
    }

    @Override
    @Transactional
    public ReviewerScoringFormulaResponseDTO createFormula(UUID adminUserId, ReviewerScoringFormulaRequest request) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin user not found"));
        ReviewerScoringFormula formula = new ReviewerScoringFormula();
        formula.setLikeWeight(request.getLikeWeight());
        formula.setCommentWeight(request.getCommentWeight());
        formula.setShareWeight(request.getShareWeight());
        formula.setLikePayoutAmount(request.getLikePayoutAmount());
        formula.setCommentPayoutAmount(request.getCommentPayoutAmount());
        formula.setSharePayoutAmount(request.getSharePayoutAmount());
        formula.setDescription(request.getDescription());
        formula.setActive(false);
        formula.setCreatedBy(admin);
        return toResponseDTO(formulaRepository.save(formula));
    }

    @Override
    @Transactional
    public ReviewerScoringFormulaResponseDTO activateFormula(UUID adminUserId, UUID formulaId) {
        ReviewerScoringFormula target = formulaRepository.findById(formulaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formula not found"));
        formulaRepository.findByActiveTrue().ifPresent(current -> {
            if (!current.getId().equals(formulaId)) {
                current.setActive(false);
                formulaRepository.save(current);
            }
        });
        target.setActive(true);
        return toResponseDTO(formulaRepository.save(target));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewerScoringFormulaResponseDTO> getAllFormulas() {
        return formulaRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public long calculateScore(long likeCount, long shareCount, long commentCount) {
        ReviewerScoringFormula formula = getActiveFormula();
        return likeCount * formula.getLikeWeight()
                + shareCount * formula.getShareWeight()
                + commentCount * formula.getCommentWeight();
    }

    @Override
    public long calculatePayout(long likeCount, long shareCount, long commentCount) {
        ReviewerScoringFormula formula = getActiveFormula();
        return likeCount * formula.getLikePayoutAmount()
                + shareCount * formula.getSharePayoutAmount()
                + commentCount * formula.getCommentPayoutAmount();
    }

    private ReviewerScoringFormula createAndActivateDefaultFormula() {
        ReviewerScoringFormula formula = new ReviewerScoringFormula();
        formula.setLikeWeight(1);
        formula.setCommentWeight(5);
        formula.setShareWeight(3);
        formula.setLikePayoutAmount(100L);
        formula.setCommentPayoutAmount(500L);
        formula.setSharePayoutAmount(300L);
        formula.setActive(true);
        formula.setDescription("Default formula");
        return formulaRepository.save(formula);
    }

    public ReviewerScoringFormulaResponseDTO toResponseDTO(ReviewerScoringFormula formula) {
        ReviewerScoringFormulaResponseDTO dto = new ReviewerScoringFormulaResponseDTO();
        dto.setId(formula.getId());
        dto.setLikeWeight(formula.getLikeWeight());
        dto.setCommentWeight(formula.getCommentWeight());
        dto.setShareWeight(formula.getShareWeight());
        dto.setLikePayoutAmount(formula.getLikePayoutAmount());
        dto.setCommentPayoutAmount(formula.getCommentPayoutAmount());
        dto.setSharePayoutAmount(formula.getSharePayoutAmount());
        dto.setActive(formula.isActive());
        dto.setDescription(formula.getDescription());
        dto.setCreatedAt(formula.getCreatedAt());
        return dto;
    }
}
