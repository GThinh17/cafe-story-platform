package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.ReviewerFormulaRequestDTO;
import com.cafestory.dto.responseDTO.ReviewerFormulaResponseDTO;
import com.cafestory.entity.ReviewerFormula;
import com.cafestory.entity.User;
import com.cafestory.repository.ReviewerFormulaRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceInterface.ReviewerFormulaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ReviewerFormulaServiceImpl implements ReviewerFormulaService {

    private final ReviewerFormulaRepository formulaRepository;
    private final UserRepository userRepository;

    public ReviewerFormulaServiceImpl(ReviewerFormulaRepository formulaRepository,
                                      UserRepository userRepository) {
        this.formulaRepository = formulaRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ReviewerFormula getActiveFormula() {
        return formulaRepository.findByActiveTrue()
                .orElseGet(this::createDefaultActiveFormula);
    }

    @Override
    @Transactional
    public ReviewerFormulaResponseDTO createFormula(UUID adminUserId, ReviewerFormulaRequestDTO request) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
        ReviewerFormula formula = buildFromRequest(request);
        formula.setCreatedBy(admin);
        formula.setActive(false);
        return toResponseDTO(formulaRepository.save(formula));
    }

    @Override
    @Transactional
    public ReviewerFormulaResponseDTO activateFormula(UUID adminUserId, UUID formulaId) {
        ReviewerFormula target = formulaRepository.findById(formulaId)
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
    public List<ReviewerFormulaResponseDTO> getAllFormulas() {
        return formulaRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public long calculateScore(long likeCount, long shareCount, long commentCount) {
        ReviewerFormula formula = getActiveFormula();
        return likeCount * formula.getLikeWeight()
                + shareCount * formula.getShareWeight()
                + commentCount * formula.getCommentWeight();
    }

    @Override
    public long calculatePayout(long likeCount, long shareCount, long commentCount) {
        ReviewerFormula formula = getActiveFormula();
        return likeCount * formula.getLikePayoutAmount()
                + shareCount * formula.getSharePayoutAmount()
                + commentCount * formula.getCommentPayoutAmount();
    }

    private ReviewerFormula createDefaultActiveFormula() {
        ReviewerFormula formula = new ReviewerFormula();
        formula.setLikeWeight(1);
        formula.setCommentWeight(5);
        formula.setShareWeight(3);
        formula.setLikePayoutAmount(100L);
        formula.setCommentPayoutAmount(500L);
        formula.setSharePayoutAmount(300L);
        formula.setIronMultiplier(BigDecimal.ONE);
        formula.setBronzeMultiplier(new BigDecimal("1.20"));
        formula.setSilverMultiplier(new BigDecimal("1.50"));
        formula.setGoldMultiplier(new BigDecimal("2.00"));
        formula.setDiamondMultiplier(new BigDecimal("3.00"));
        formula.setActive(true);
        formula.setDescription("Default formula");
        return formulaRepository.save(formula);
    }

    private ReviewerFormula buildFromRequest(ReviewerFormulaRequestDTO request) {
        ReviewerFormula formula = new ReviewerFormula();
        formula.setLikeWeight(request.getLikeWeight());
        formula.setCommentWeight(request.getCommentWeight());
        formula.setShareWeight(request.getShareWeight());
        formula.setLikePayoutAmount(request.getLikePayoutAmount());
        formula.setCommentPayoutAmount(request.getCommentPayoutAmount());
        formula.setSharePayoutAmount(request.getSharePayoutAmount());
        formula.setIronMultiplier(request.getIronMultiplier());
        formula.setBronzeMultiplier(request.getBronzeMultiplier());
        formula.setSilverMultiplier(request.getSilverMultiplier());
        formula.setGoldMultiplier(request.getGoldMultiplier());
        formula.setDiamondMultiplier(request.getDiamondMultiplier());
        formula.setDescription(request.getDescription());
        return formula;
    }

    public ReviewerFormulaResponseDTO toResponseDTO(ReviewerFormula formula) {
        ReviewerFormulaResponseDTO dto = new ReviewerFormulaResponseDTO();
        dto.setId(formula.getId());
        dto.setLikeWeight(formula.getLikeWeight());
        dto.setCommentWeight(formula.getCommentWeight());
        dto.setShareWeight(formula.getShareWeight());
        dto.setLikePayoutAmount(formula.getLikePayoutAmount());
        dto.setCommentPayoutAmount(formula.getCommentPayoutAmount());
        dto.setSharePayoutAmount(formula.getSharePayoutAmount());
        dto.setIronMultiplier(formula.getIronMultiplier());
        dto.setBronzeMultiplier(formula.getBronzeMultiplier());
        dto.setSilverMultiplier(formula.getSilverMultiplier());
        dto.setGoldMultiplier(formula.getGoldMultiplier());
        dto.setDiamondMultiplier(formula.getDiamondMultiplier());
        dto.setActive(formula.isActive());
        dto.setDescription(formula.getDescription());
        dto.setCreatedAt(formula.getCreatedAt());
        return dto;
    }
}
