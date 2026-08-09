package com.cafestory.controller;

import com.cafestory.dto.requestDTO.AdminPayoutStatusRequest;
import com.cafestory.dto.responseDTO.AdminPayoutResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerIncomeResponseDTO;
import com.cafestory.entity.enums.AdminPayoutStatus;
import com.cafestory.service.serviceInterface.AdminPayoutService;
import com.cafestory.service.serviceInterface.ReviewerIncomeService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link AdminPayoutController}.
 *
 * <p>Bộ điều khiển này chủ yếu chuẩn hoá tham số truy vấn trước khi gọi service:
 * tháng để trống thì lấy tháng hiện tại (income) hoặc tháng trước (payout), ngày
 * để trống thì lấy hôm qua, và {@code sortDir} quyết định chiều sắp xếp.
 */
@ExtendWith(MockitoExtension.class)
class AdminPayoutControllerTest {

    @Mock
    private ReviewerIncomeService incomeService;
    @Mock
    private AdminPayoutService payoutService;

    @InjectMocks
    private AdminPayoutController adminPayoutController;

    private AuthenticatedUserPrincipal principal;

    @BeforeEach
    void setUp() {
        principal = new AuthenticatedUserPrincipal(UUID.randomUUID(), "admin", List.of("ADMIN"));
    }

    @Test
    void generateDailyIncome_success_usesGivenDate_TC001() {
        LocalDate date = LocalDate.of(2026, 7, 15);

        adminPayoutController.generateDailyIncome(date, principal);

        verify(incomeService).generateDailyIncome(date);
    }

    @Test
    void generateDailyIncome_success_defaultsToYesterday_TC002() {
        adminPayoutController.generateDailyIncome(null, principal);

        verify(incomeService).generateDailyIncome(LocalDate.now().minusDays(1));
    }

    @Test
    void generateDailyIncome_fail_missingPrincipal_TC003() {
        assertThatThrownBy(() -> adminPayoutController.generateDailyIncome(null, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Authentication is required");
    }

    @Test
    void getIncome_success_filtersByReviewerAndSortsAscending_TC004() {
        UUID reviewerId = UUID.randomUUID();
        Page<ReviewerIncomeResponseDTO> page = new PageImpl<>(List.of());
        when(incomeService.getIncomeByReviewer(eq(reviewerId), eq("2026-07"), any(Pageable.class)))
                .thenReturn(page);

        assertThat(adminPayoutController.getIncome(reviewerId, "2026-07", 1, 50, "ASC")).isSameAs(page);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(incomeService).getIncomeByReviewer(eq(reviewerId), eq("2026-07"), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(captor.getValue().getPageSize()).isEqualTo(50);
        assertThat(captor.getValue().getSort().getOrderFor("finalAmount").getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void getIncome_success_withoutReviewerUsesCurrentMonthAndDescending_TC005() {
        Page<ReviewerIncomeResponseDTO> page = new PageImpl<>(List.of());
        String currentMonth = YearMonth.now().toString();
        when(incomeService.getAllIncome(eq(currentMonth), any(Pageable.class))).thenReturn(page);

        assertThat(adminPayoutController.getIncome(null, null, 0, 20, "desc")).isSameAs(page);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(incomeService).getAllIncome(eq(currentMonth), captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("finalAmount").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void generateMonthlyPayout_success_usesGivenMonth_TC006() {
        adminPayoutController.generateMonthlyPayout("2026-06", principal);

        verify(payoutService).generateMonthlyPayout("2026-06");
    }

    @Test
    void generateMonthlyPayout_success_defaultsToPreviousMonth_TC007() {
        adminPayoutController.generateMonthlyPayout(null, principal);

        verify(payoutService).generateMonthlyPayout(YearMonth.now().minusMonths(1).toString());
    }

    @Test
    void generateMonthlyPayout_fail_missingPrincipal_TC008() {
        assertThatThrownBy(() -> adminPayoutController.generateMonthlyPayout("2026-06", null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Authentication is required");
    }

    @Test
    void getPayouts_success_passesFiltersAndDescendingSort_TC009() {
        Page<AdminPayoutResponseDTO> page = new PageImpl<>(List.of());
        when(payoutService.getPayouts(eq("2026-06"), eq(AdminPayoutStatus.PAID), any(Pageable.class)))
                .thenReturn(page);

        assertThat(adminPayoutController.getPayouts("2026-06", AdminPayoutStatus.PAID, 0, 20, "desc"))
                .isSameAs(page);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(payoutService).getPayouts(eq("2026-06"), eq(AdminPayoutStatus.PAID), captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("totalFinalAmount").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getPayouts_success_ascendingSort_TC010() {
        Page<AdminPayoutResponseDTO> page = new PageImpl<>(List.of());
        when(payoutService.getPayouts(any(), any(), any(Pageable.class))).thenReturn(page);

        adminPayoutController.getPayouts(null, null, 0, 20, "asc");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(payoutService).getPayouts(any(), any(), captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("totalFinalAmount").getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void updatePayoutStatus_success_passesAdminUserId_TC011() {
        UUID payoutId = UUID.randomUUID();
        AdminPayoutStatusRequest request = new AdminPayoutStatusRequest();
        request.setStatus(AdminPayoutStatus.APPROVED);
        AdminPayoutResponseDTO response = new AdminPayoutResponseDTO();
        when(payoutService.updatePayoutStatus(payoutId, principal.userId(), request)).thenReturn(response);

        assertThat(adminPayoutController.updatePayoutStatus(payoutId, request, principal)).isSameAs(response);
    }

    @Test
    void updatePayoutStatus_fail_missingPrincipal_TC012() {
        UUID payoutId = UUID.randomUUID();
        AdminPayoutStatusRequest request = new AdminPayoutStatusRequest();
        request.setStatus(AdminPayoutStatus.APPROVED);

        assertThatThrownBy(() -> adminPayoutController.updatePayoutStatus(payoutId, request, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Authentication is required");
    }
}
