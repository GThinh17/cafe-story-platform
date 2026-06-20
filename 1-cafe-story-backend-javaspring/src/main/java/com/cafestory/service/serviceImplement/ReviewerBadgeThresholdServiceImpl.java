package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.ReviewerBadgeThresholdRequest;
import com.cafestory.dto.responseDTO.ReviewerBadgeThresholdResponseDTO;
import com.cafestory.entity.ReviewerBadgeThreshold;
import com.cafestory.entity.ReviewerFormula;
import com.cafestory.entity.enums.ReviewerBadge;
import com.cafestory.repository.ReviewerBadgeThresholdRepository;
import com.cafestory.repository.ReviewerFormulaRepository;
import com.cafestory.service.serviceInterface.ReviewerBadgeThresholdService;
import com.cafestory.service.serviceInterface.ReviewerFormulaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReviewerBadgeThresholdServiceImpl implements ReviewerBadgeThresholdService {

    private final ReviewerBadgeThresholdRepository thresholdRepository;
    private final ReviewerFormulaRepository formulaRepository;
    private final ReviewerFormulaService formulaService;

    public ReviewerBadgeThresholdServiceImpl(
            ReviewerBadgeThresholdRepository thresholdRepository,
            ReviewerFormulaRepository formulaRepository,
            ReviewerFormulaService formulaService) {
        this.thresholdRepository = thresholdRepository;
        this.formulaRepository = formulaRepository;
        this.formulaService = formulaService;
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewerBadge badgeForScore(long score) {
        ReviewerFormula activeFormula = formulaService.getActiveFormula();
        List<ReviewerBadgeThreshold> thresholds = thresholdRepository
                .findByFormulaIdOrderByMinScoreAsc(activeFormula.getId());
        if (thresholds.isEmpty()) {
            return fallbackBadgeForScore(score);
        }
        ReviewerBadge result = thresholds.get(0).getBadge();
        for (ReviewerBadgeThreshold threshold : thresholds) {
            if (score >= threshold.getMinScore()) {
                result = threshold.getBadge();
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewerBadgeThresholdResponseDTO> getThresholds(UUID formulaId) {
        formulaRepository.findById(formulaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formula not found"));
        return thresholdRepository.findByFormulaIdOrderByMinScoreAsc(formulaId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public List<ReviewerBadgeThresholdResponseDTO> updateThresholds(
            UUID adminUserId,
            UUID formulaId,
            List<ReviewerBadgeThresholdRequest> thresholds) {
        ReviewerFormula formula = formulaRepository.findById(formulaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formula not found"));
        Set<ReviewerBadge> requestedBadges = thresholds.stream()
                .map(ReviewerBadgeThresholdRequest::getBadge)
                .collect(Collectors.toSet());
        if (requestedBadges.size() != ReviewerBadge.values().length) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Must provide thresholds for all 5 badge levels: IRON, BRONZE, SILVER, GOLD, DIAMOND");
        }
        thresholdRepository.deleteByFormulaId(formulaId);
        thresholdRepository.flush();
        List<ReviewerBadgeThreshold> saved = thresholds.stream()
                .sorted(Comparator.comparingLong(ReviewerBadgeThresholdRequest::getMinScore))
                .map(req -> {
                    ReviewerBadgeThreshold threshold = new ReviewerBadgeThreshold();
                    threshold.setBadge(req.getBadge());
                    threshold.setMinScore(req.getMinScore());
                    threshold.setFormula(formula);
                    return thresholdRepository.save(threshold);
                })
                .toList();
        return saved.stream().map(this::toResponseDTO).toList();
    }

    private ReviewerBadge fallbackBadgeForScore(long score) {
        if (score < 100) return ReviewerBadge.IRON;
        if (score < 300) return ReviewerBadge.BRONZE;
        if (score < 700) return ReviewerBadge.SILVER;
        if (score < 1500) return ReviewerBadge.GOLD;
        return ReviewerBadge.DIAMOND;
    }

    private ReviewerBadgeThresholdResponseDTO toResponseDTO(ReviewerBadgeThreshold threshold) {
        ReviewerBadgeThresholdResponseDTO dto = new ReviewerBadgeThresholdResponseDTO();
        dto.setId(threshold.getId());
        dto.setBadge(threshold.getBadge());
        dto.setMinScore(threshold.getMinScore());
        dto.setFormulaId(threshold.getFormula().getId());
        return dto;
    }
}
