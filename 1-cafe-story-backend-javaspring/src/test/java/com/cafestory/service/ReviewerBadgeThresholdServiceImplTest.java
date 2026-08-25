package com.cafestory.service;

import com.cafestory.dto.requestDTO.ReviewerBadgeThresholdRequest;
import com.cafestory.dto.responseDTO.ReviewerBadgeThresholdResponseDTO;
import com.cafestory.entity.ReviewerBadgeThreshold;
import com.cafestory.entity.ReviewerFormula;
import com.cafestory.entity.enums.ReviewerBadge;
import com.cafestory.repository.ReviewerBadgeThresholdRepository;
import com.cafestory.repository.ReviewerFormulaRepository;
import com.cafestory.service.serviceImplement.RagReindexClient;
import com.cafestory.service.serviceImplement.ReviewerBadgeThresholdServiceImpl;
import com.cafestory.service.serviceInterface.ReviewerFormulaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewerBadgeThresholdServiceImplTest {

    private static final UUID FORMULA_ID = UUID.fromString("dddddddd-0000-0000-0000-000000000004");
    private static final UUID ADMIN_ID = UUID.fromString("eeeeeeee-0000-0000-0000-000000000005");

    @Mock
    private ReviewerBadgeThresholdRepository thresholdRepository;

    @Mock
    private ReviewerFormulaRepository formulaRepository;

    @Mock
    private ReviewerFormulaService formulaService;

    @Mock
    private RagReindexClient ragReindexClient;

    private ReviewerBadgeThresholdServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReviewerBadgeThresholdServiceImpl(
                thresholdRepository, formulaRepository, formulaService, ragReindexClient);
    }

    private ReviewerFormula formula() {
        ReviewerFormula formula = new ReviewerFormula();
        formula.setId(FORMULA_ID);
        formula.setActive(true);
        return formula;
    }

    private ReviewerBadgeThreshold threshold(ReviewerBadge badge, long minScore) {
        ReviewerBadgeThreshold t = new ReviewerBadgeThreshold();
        t.setId(UUID.randomUUID());
        t.setBadge(badge);
        t.setMinScore(minScore);
        t.setFormula(formula());
        return t;
    }

    private List<ReviewerBadgeThreshold> configuredThresholds() {
        return List.of(
                threshold(ReviewerBadge.IRON, 0L),
                threshold(ReviewerBadge.BRONZE, 200L),
                threshold(ReviewerBadge.SILVER, 500L),
                threshold(ReviewerBadge.GOLD, 1000L),
                threshold(ReviewerBadge.DIAMOND, 2000L));
    }

    private List<ReviewerBadgeThresholdRequest> fullRequest() {
        List<ReviewerBadgeThresholdRequest> requests = new ArrayList<>();
        long[] scores = {2000L, 0L, 1000L, 200L, 500L};
        ReviewerBadge[] badges = {
                ReviewerBadge.DIAMOND, ReviewerBadge.IRON, ReviewerBadge.GOLD,
                ReviewerBadge.BRONZE, ReviewerBadge.SILVER};
        for (int i = 0; i < badges.length; i++) {
            ReviewerBadgeThresholdRequest r = new ReviewerBadgeThresholdRequest();
            r.setBadge(badges[i]);
            r.setMinScore(scores[i]);
            requests.add(r);
        }
        return requests;
    }

    @Test
    void badgeForScore_success_usesConfiguredThresholds_TC001() {
        when(formulaService.getActiveFormula()).thenReturn(formula());
        when(thresholdRepository.findByFormulaIdOrderByMinScoreAsc(FORMULA_ID))
                .thenReturn(configuredThresholds());

        assertThat(service.badgeForScore(0L)).isEqualTo(ReviewerBadge.IRON);
        assertThat(service.badgeForScore(199L)).isEqualTo(ReviewerBadge.IRON);
        assertThat(service.badgeForScore(200L)).isEqualTo(ReviewerBadge.BRONZE);
        assertThat(service.badgeForScore(999L)).isEqualTo(ReviewerBadge.SILVER);
        assertThat(service.badgeForScore(2500L)).isEqualTo(ReviewerBadge.DIAMOND);
    }

    @Test
    void badgeForScore_success_returnsLowestBadgeBelowFirstThreshold_TC002() {
        List<ReviewerBadgeThreshold> thresholds = List.of(
                threshold(ReviewerBadge.BRONZE, 200L),
                threshold(ReviewerBadge.SILVER, 500L));
        when(formulaService.getActiveFormula()).thenReturn(formula());
        when(thresholdRepository.findByFormulaIdOrderByMinScoreAsc(FORMULA_ID)).thenReturn(thresholds);

        assertThat(service.badgeForScore(10L)).isEqualTo(ReviewerBadge.BRONZE);
    }

    @Test
    void badgeForScore_success_fallsBackWhenNoThresholdConfigured_TC003() {
        when(formulaService.getActiveFormula()).thenReturn(formula());
        when(thresholdRepository.findByFormulaIdOrderByMinScoreAsc(FORMULA_ID)).thenReturn(List.of());

        assertThat(service.badgeForScore(50L)).isEqualTo(ReviewerBadge.IRON);
        assertThat(service.badgeForScore(150L)).isEqualTo(ReviewerBadge.BRONZE);
        assertThat(service.badgeForScore(400L)).isEqualTo(ReviewerBadge.SILVER);
        assertThat(service.badgeForScore(900L)).isEqualTo(ReviewerBadge.GOLD);
        assertThat(service.badgeForScore(5000L)).isEqualTo(ReviewerBadge.DIAMOND);
    }

    @Test
    void updateThresholds_fail_whenNotAllBadgeLevelsProvided_TC004() {
        when(formulaRepository.findById(FORMULA_ID)).thenReturn(Optional.of(formula()));
        List<ReviewerBadgeThresholdRequest> incomplete = fullRequest().subList(0, 3);

        assertThatThrownBy(() -> service.updateThresholds(ADMIN_ID, FORMULA_ID, incomplete))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("all 5 badge levels");

        verify(thresholdRepository, never()).deleteByFormulaId(any());
        verify(ragReindexClient, never()).triggerDbReindex();
    }

    @Test
    void updateThresholds_success_replacesPreviousThresholdsAtomically_TC005() {
        when(formulaRepository.findById(FORMULA_ID)).thenReturn(Optional.of(formula()));
        when(thresholdRepository.save(any(ReviewerBadgeThreshold.class))).thenAnswer(inv -> inv.getArgument(0));

        List<ReviewerBadgeThresholdResponseDTO> result =
                service.updateThresholds(ADMIN_ID, FORMULA_ID, fullRequest());

        assertThat(result).hasSize(5);
        verify(thresholdRepository).deleteByFormulaId(FORMULA_ID);
        verify(thresholdRepository).flush();
        verify(thresholdRepository, org.mockito.Mockito.times(5)).save(any(ReviewerBadgeThreshold.class));
        verify(ragReindexClient).triggerDbReindex();
    }

    @Test
    void updateThresholds_success_storesThresholdsSortedByMinScore_TC006() {
        when(formulaRepository.findById(FORMULA_ID)).thenReturn(Optional.of(formula()));
        when(thresholdRepository.save(any(ReviewerBadgeThreshold.class))).thenAnswer(inv -> inv.getArgument(0));

        List<ReviewerBadgeThresholdResponseDTO> result =
                service.updateThresholds(ADMIN_ID, FORMULA_ID, fullRequest());

        assertThat(result).extracting(ReviewerBadgeThresholdResponseDTO::getMinScore)
                .containsExactly(0L, 200L, 500L, 1000L, 2000L);
    }

    @Test
    void updateThresholds_fail_formulaNotFound_TC007() {
        when(formulaRepository.findById(FORMULA_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateThresholds(ADMIN_ID, FORMULA_ID, fullRequest()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Formula not found");
    }

    @Test
    void getThresholds_success_returnsThresholdsOfFormula_TC008() {
        when(formulaRepository.findById(FORMULA_ID)).thenReturn(Optional.of(formula()));
        when(thresholdRepository.findByFormulaIdOrderByMinScoreAsc(FORMULA_ID))
                .thenReturn(configuredThresholds());

        List<ReviewerBadgeThresholdResponseDTO> result = service.getThresholds(FORMULA_ID);

        assertThat(result).hasSize(5);
        assertThat(result).extracting(ReviewerBadgeThresholdResponseDTO::getBadge)
                .containsExactly(ReviewerBadge.IRON, ReviewerBadge.BRONZE, ReviewerBadge.SILVER,
                        ReviewerBadge.GOLD, ReviewerBadge.DIAMOND);
    }

    @Test
    void getThresholds_fail_formulaNotFound_TC009() {
        when(formulaRepository.findById(FORMULA_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getThresholds(FORMULA_ID))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Formula not found");
    }
}
