package com.cafestory.service;

import com.cafestory.dto.requestDTO.ReviewerFormulaRequestDTO;
import com.cafestory.dto.responseDTO.ReviewerFormulaResponseDTO;
import com.cafestory.entity.ReviewerFormula;
import com.cafestory.entity.User;
import com.cafestory.repository.ReviewerFormulaRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceImplement.RagReindexClient;
import com.cafestory.service.serviceImplement.ReviewerFormulaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
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
class ReviewerFormulaServiceImplTest {

    private static final UUID ADMIN_ID = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    private static final UUID FORMULA_ID = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000002");
    private static final UUID OTHER_FORMULA_ID = UUID.fromString("cccccccc-0000-0000-0000-000000000003");

    @Mock
    private ReviewerFormulaRepository formulaRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RagReindexClient ragReindexClient;

    private ReviewerFormulaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReviewerFormulaServiceImpl(formulaRepository, userRepository, ragReindexClient);
    }

    private ReviewerFormula activeFormula() {
        ReviewerFormula formula = new ReviewerFormula();
        formula.setId(FORMULA_ID);
        formula.setLikeWeight(1);
        formula.setCommentWeight(5);
        formula.setShareWeight(3);
        formula.setLikePayoutAmount(100L);
        formula.setCommentPayoutAmount(500L);
        formula.setSharePayoutAmount(300L);
        formula.setActive(true);
        return formula;
    }

    @Test
    void getActiveFormula_success_returnsExistingActiveFormula_TC001() {
        ReviewerFormula existing = activeFormula();
        when(formulaRepository.findByActiveTrue()).thenReturn(Optional.of(existing));

        ReviewerFormula result = service.getActiveFormula();

        assertThat(result).isSameAs(existing);
        verify(formulaRepository, never()).save(any());
    }

    @Test
    void getActiveFormula_success_createsDefaultFormulaWhenNoneActive_TC002() {
        when(formulaRepository.findByActiveTrue()).thenReturn(Optional.empty());
        when(formulaRepository.save(any(ReviewerFormula.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewerFormula result = service.getActiveFormula();

        assertThat(result.isActive()).isTrue();
        assertThat(result.getLikeWeight()).isEqualTo(1);
        assertThat(result.getCommentWeight()).isEqualTo(5);
        assertThat(result.getShareWeight()).isEqualTo(3);
        assertThat(result.getLikePayoutAmount()).isEqualTo(100L);
        assertThat(result.getCommentPayoutAmount()).isEqualTo(500L);
        assertThat(result.getSharePayoutAmount()).isEqualTo(300L);
        assertThat(result.getIronMultiplier()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(result.getDiamondMultiplier()).isEqualByComparingTo(new BigDecimal("3.00"));
    }

    @Test
    void calculateScore_success_appliesInteractionWeights_TC003() {
        when(formulaRepository.findByActiveTrue()).thenReturn(Optional.of(activeFormula()));

        long score = service.calculateScore(10L, 2L, 4L);

        // 10 likes * 1 + 2 shares * 3 + 4 comments * 5 = 36
        assertThat(score).isEqualTo(36L);
    }

    @Test
    void calculatePayout_success_appliesUnitAmounts_TC004() {
        when(formulaRepository.findByActiveTrue()).thenReturn(Optional.of(activeFormula()));

        long payout = service.calculatePayout(10L, 2L, 4L);

        // 10 * 100 + 2 * 300 + 4 * 500 = 3600
        assertThat(payout).isEqualTo(3600L);
    }

    @Test
    void activateFormula_success_deactivatesPreviouslyActiveFormula_TC005() {
        ReviewerFormula target = new ReviewerFormula();
        target.setId(FORMULA_ID);
        ReviewerFormula current = new ReviewerFormula();
        current.setId(OTHER_FORMULA_ID);
        current.setActive(true);

        when(formulaRepository.findById(FORMULA_ID)).thenReturn(Optional.of(target));
        when(formulaRepository.findByActiveTrue()).thenReturn(Optional.of(current));
        when(formulaRepository.save(any(ReviewerFormula.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewerFormulaResponseDTO response = service.activateFormula(ADMIN_ID, FORMULA_ID);

        assertThat(response).isNotNull();
        assertThat(target.isActive()).isTrue();
        assertThat(current.isActive()).isFalse();

        ArgumentCaptor<ReviewerFormula> captor = ArgumentCaptor.forClass(ReviewerFormula.class);
        verify(formulaRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(ReviewerFormula::getId)
                .containsExactly(OTHER_FORMULA_ID, FORMULA_ID);
        verify(ragReindexClient).triggerDbReindex();
    }

    @Test
    void activateFormula_success_keepsSingleFormulaWhenAlreadyActive_TC006() {
        ReviewerFormula target = new ReviewerFormula();
        target.setId(FORMULA_ID);
        target.setActive(true);

        when(formulaRepository.findById(FORMULA_ID)).thenReturn(Optional.of(target));
        when(formulaRepository.findByActiveTrue()).thenReturn(Optional.of(target));
        when(formulaRepository.save(any(ReviewerFormula.class))).thenAnswer(inv -> inv.getArgument(0));

        service.activateFormula(ADMIN_ID, FORMULA_ID);

        assertThat(target.isActive()).isTrue();
        verify(formulaRepository, org.mockito.Mockito.times(1)).save(any(ReviewerFormula.class));
    }

    @Test
    void activateFormula_fail_formulaNotFound_TC007() {
        when(formulaRepository.findById(FORMULA_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.activateFormula(ADMIN_ID, FORMULA_ID))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Formula not found");

        verify(ragReindexClient, never()).triggerDbReindex();
    }

    @Test
    void createFormula_success_storesFormulaAsInactive_TC008() {
        User admin = new User();
        admin.setUserId(ADMIN_ID);
        ReviewerFormulaRequestDTO request = new ReviewerFormulaRequestDTO();
        request.setLikeWeight(2);
        request.setCommentWeight(6);
        request.setShareWeight(4);
        request.setLikePayoutAmount(200L);
        request.setCommentPayoutAmount(600L);
        request.setSharePayoutAmount(400L);
        request.setIronMultiplier(BigDecimal.ONE);
        request.setBronzeMultiplier(new BigDecimal("1.20"));
        request.setSilverMultiplier(new BigDecimal("1.50"));
        request.setGoldMultiplier(new BigDecimal("2.00"));
        request.setDiamondMultiplier(new BigDecimal("3.00"));

        when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));
        when(formulaRepository.save(any(ReviewerFormula.class))).thenAnswer(inv -> inv.getArgument(0));

        service.createFormula(ADMIN_ID, request);

        ArgumentCaptor<ReviewerFormula> captor = ArgumentCaptor.forClass(ReviewerFormula.class);
        verify(formulaRepository).save(captor.capture());
        ReviewerFormula saved = captor.getValue();
        assertThat(saved.isActive()).isFalse();
        assertThat(saved.getCreatedBy()).isSameAs(admin);
        assertThat(saved.getLikeWeight()).isEqualTo(2);
    }

    @Test
    void createFormula_fail_adminNotFound_TC009() {
        when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createFormula(ADMIN_ID, new ReviewerFormulaRequestDTO()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Admin not found");

        verify(formulaRepository, never()).save(any());
    }

    @Test
    void getAllFormulas_success_returnsFormulasOrderedByRepository_TC010() {
        ReviewerFormula first = activeFormula();
        ReviewerFormula second = new ReviewerFormula();
        second.setId(OTHER_FORMULA_ID);
        when(formulaRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(first, second));

        List<ReviewerFormulaResponseDTO> result = service.getAllFormulas();

        assertThat(result).hasSize(2);
    }
}
