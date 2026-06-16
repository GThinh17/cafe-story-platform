package com.cafestory.config;

import com.cafestory.entity.ReviewerBadgeThreshold;
import com.cafestory.entity.ReviewerScoringFormula;
import com.cafestory.entity.enums.ReviewerBadge;
import com.cafestory.repository.ReviewerBadgeThresholdRepository;
import com.cafestory.repository.ReviewerScoringFormulaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ReviewerFormulaDataInitializer implements CommandLineRunner {

    private final ReviewerScoringFormulaRepository formulaRepository;
    private final ReviewerBadgeThresholdRepository thresholdRepository;

    public ReviewerFormulaDataInitializer(
            ReviewerScoringFormulaRepository formulaRepository,
            ReviewerBadgeThresholdRepository thresholdRepository) {
        this.formulaRepository = formulaRepository;
        this.thresholdRepository = thresholdRepository;
    }

    @Override
    public void run(String... args) {
        ReviewerScoringFormula activeFormula = ensureDefaultFormula();
        ensureDefaultThresholds(activeFormula);
    }

    private ReviewerScoringFormula ensureDefaultFormula() {
        Optional<ReviewerScoringFormula> existing = formulaRepository.findByActiveTrue();
        if (existing.isPresent()) {
            return existing.get();
        }
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

    private void ensureDefaultThresholds(ReviewerScoringFormula formula) {
        List<ReviewerBadgeThreshold> existing = thresholdRepository.findByFormulaIdOrderByMinScoreAsc(formula.getId());
        if (!existing.isEmpty()) {
            return;
        }
        createThreshold(formula, ReviewerBadge.IRON, 0L);
        createThreshold(formula, ReviewerBadge.BRONZE, 100L);
        createThreshold(formula, ReviewerBadge.SILVER, 300L);
        createThreshold(formula, ReviewerBadge.GOLD, 700L);
        createThreshold(formula, ReviewerBadge.DIAMOND, 1500L);
    }

    private void createThreshold(ReviewerScoringFormula formula, ReviewerBadge badge, long minScore) {
        ReviewerBadgeThreshold threshold = new ReviewerBadgeThreshold();
        threshold.setFormula(formula);
        threshold.setBadge(badge);
        threshold.setMinScore(minScore);
        thresholdRepository.save(threshold);
    }
}
