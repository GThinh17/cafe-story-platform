package com.cafestory.controller;

import com.cafestory.dto.responseDTO.ReviewerRankingSnapshotResponseDTO;
import com.cafestory.entity.enums.RankingPeriodType;
import com.cafestory.service.serviceInterface.ReviewerRankingSnapshotService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link AdminReviewerConfigController}.
 */
@ExtendWith(MockitoExtension.class)
class AdminReviewerConfigControllerTest {

    @Mock
    private ReviewerRankingSnapshotService snapshotService;

    @InjectMocks
    private AdminReviewerConfigController adminReviewerConfigController;

    private AuthenticatedUserPrincipal principal;

    @BeforeEach
    void setUp() {
        principal = new AuthenticatedUserPrincipal(UUID.randomUUID(), "admin", List.of("ADMIN"));
    }

    @Test
    void generateRankingSnapshot_success_usesGivenReferenceDate_TC001() {
        LocalDate referenceDate = LocalDate.of(2026, 7, 15);

        adminReviewerConfigController.generateRankingSnapshot(
                RankingPeriodType.MONTHLY, referenceDate, principal);

        verify(snapshotService).generateSnapshot(RankingPeriodType.MONTHLY, referenceDate);
    }

    @Test
    void generateRankingSnapshot_success_defaultsToToday_TC002() {
        adminReviewerConfigController.generateRankingSnapshot(RankingPeriodType.DAILY, null, principal);

        verify(snapshotService).generateSnapshot(RankingPeriodType.DAILY, LocalDate.now());
    }

    @Test
    void generateRankingSnapshot_fail_missingPrincipal_TC003() {
        assertThatThrownBy(() -> adminReviewerConfigController.generateRankingSnapshot(
                RankingPeriodType.DAILY, null, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Authentication is required");
    }

    @Test
    void getRanking_success_delegates_TC004() {
        Page<ReviewerRankingSnapshotResponseDTO> page = new PageImpl<>(List.of());
        when(snapshotService.getRanking("2026-07", RankingPeriodType.MONTHLY, 1, 30)).thenReturn(page);

        assertThat(adminReviewerConfigController.getRanking("2026-07", RankingPeriodType.MONTHLY, 1, 30))
                .isSameAs(page);
    }
}
