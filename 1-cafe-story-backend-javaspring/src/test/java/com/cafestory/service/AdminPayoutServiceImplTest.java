package com.cafestory.service;

import com.cafestory.dto.requestDTO.AdminPayoutStatusRequest;
import com.cafestory.dto.responseDTO.AdminPayoutResponseDTO;
import com.cafestory.entity.AdminPayout;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.ReviewerFormula;
import com.cafestory.entity.ReviewerRankingSnapshot;
import com.cafestory.entity.ReviewerStripeAccount;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.AdminPayoutStatus;
import com.cafestory.entity.enums.RankingPeriodType;
import com.cafestory.entity.enums.ReviewerBadge;
import com.cafestory.repository.AdminPayoutRepository;
import com.cafestory.repository.ReviewerIncomeRepository;
import com.cafestory.repository.ReviewerRankingSnapshotRepository;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.repository.ReviewerStripeAccountRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceImplement.AdminPayoutServiceImpl;
import com.cafestory.service.serviceInterface.ReviewerBadgeThresholdService;
import com.cafestory.service.serviceInterface.ReviewerFormulaService;
import com.cafestory.service.serviceInterface.ReviewerIncomeService;
import com.stripe.exception.ApiException;
import com.stripe.model.Transfer;
import com.stripe.net.RequestOptions;
import com.stripe.param.TransferCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link AdminPayoutServiceImpl}.
 *
 * <p>Lời gọi Stripe được chặn bằng {@code mockStatic(Transfer.class)} nên không
 * có yêu cầu HTTP thật nào rời khỏi tiến trình kiểm thử. Tháng dùng để sinh
 * payout luôn lấy tháng trước tháng hiện tại vì lớp chặn tháng chưa kết thúc.
 */
@ExtendWith(MockitoExtension.class)
class AdminPayoutServiceImplTest {

    private static final String STRIPE_SECRET_KEY = "sk_test_dummy";

    @Mock
    private AdminPayoutRepository payoutRepository;
    @Mock
    private ReviewerIncomeRepository incomeRepository;
    @Mock
    private ReviewerRankingSnapshotRepository snapshotRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReviewerFormulaService formulaService;
    @Mock
    private ReviewerIncomeService reviewerIncomeService;
    @Mock
    private ReviewerStripeAccountRepository stripeAccountRepository;
    @Mock
    private ReviewerBadgeThresholdService badgeThresholdService;
    @Mock
    private ReviewerRepository reviewerRepository;

    private AdminPayoutServiceImpl payoutService;

    private Reviewer reviewer;
    private ReviewerFormula formula;
    private String closedMonth;
    private LocalDate closedMonthStart;
    private LocalDate closedMonthEnd;

    @BeforeEach
    void setUp() {
        payoutService = new AdminPayoutServiceImpl(
                STRIPE_SECRET_KEY,
                payoutRepository,
                incomeRepository,
                snapshotRepository,
                userRepository,
                formulaService,
                reviewerIncomeService,
                stripeAccountRepository,
                badgeThresholdService,
                reviewerRepository);

        reviewer = reviewer();
        formula = formula();
        YearMonth previous = YearMonth.now().minusMonths(1);
        closedMonth = previous.toString();
        closedMonthStart = previous.atDay(1);
        closedMonthEnd = previous.plusMonths(1).atDay(1);
    }

    // --------------------------------------------------- generateMonthlyPayout

    @Test
    void generateMonthlyPayout_fail_monthNotFinishedYet_TC001() {
        String currentMonth = YearMonth.now().toString();

        assertThatThrownBy(() -> payoutService.generateMonthlyPayout(currentMonth))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cannot generate payout for an incomplete month");

        verify(payoutRepository, never()).saveAll(anyList());
    }

    @Test
    void generateMonthlyPayout_success_noIncomeStopsEarly_TC002() {
        when(formulaService.getActiveFormula()).thenReturn(formula);
        when(incomeRepository.findCoveredDatesBetween(closedMonthStart, closedMonthEnd))
                .thenReturn(allDaysOfClosedMonth());
        when(incomeRepository.sumBaseAmountByReviewerBetween(closedMonthStart, closedMonthEnd))
                .thenReturn(List.of());

        payoutService.generateMonthlyPayout(closedMonth);

        verify(reviewerIncomeService, never()).generateDailyIncome(any(LocalDate.class));
        verify(payoutRepository, never()).saveAll(anyList());
    }

    @Test
    void generateMonthlyPayout_success_backfillsMissingDaysThenSaves_TC003() {
        // Chỉ ngày đầu tháng có bản ghi income: những ngày còn lại phải được backfill.
        when(formulaService.getActiveFormula()).thenReturn(formula);
        when(incomeRepository.findCoveredDatesBetween(closedMonthStart, closedMonthEnd))
                .thenReturn(List.of(closedMonthStart));
        when(incomeRepository.sumBaseAmountByReviewerBetween(closedMonthStart, closedMonthEnd))
                .thenReturn(List.of(baseRow(reviewer.getReviewerId(), 1_000L)));
        when(reviewerRepository.findAllById(any())).thenReturn(List.of(reviewer));
        when(snapshotRepository.findByPeriodAndPeriodTypeOrderByRankPositionAsc(
                closedMonth, RankingPeriodType.MONTHLY))
                .thenReturn(List.of(snapshot(reviewer, 900L)));
        when(badgeThresholdService.badgeForScore(900L)).thenReturn(ReviewerBadge.GOLD);
        when(payoutRepository.findByPayoutMonth(closedMonth)).thenReturn(List.of());

        payoutService.generateMonthlyPayout(closedMonth);

        int expectedBackfills = closedMonthStart.lengthOfMonth() - 1;
        verify(reviewerIncomeService, org.mockito.Mockito.times(expectedBackfills))
                .generateDailyIncome(any(LocalDate.class));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AdminPayout>> captor = ArgumentCaptor.forClass(List.class);
        verify(payoutRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        AdminPayout saved = captor.getValue().get(0);
        assertThat(saved.getReviewer()).isSameAs(reviewer);
        assertThat(saved.getPayoutMonth()).isEqualTo(closedMonth);
        assertThat(saved.getTotalBaseAmount()).isEqualTo(1_000L);
        assertThat(saved.getBadge()).isEqualTo(ReviewerBadge.GOLD);
        assertThat(saved.getBadgeMultiplier()).isEqualByComparingTo(new BigDecimal("2.00"));
        assertThat(saved.getTotalFinalAmount()).isEqualTo(2_000L);
        assertThat(saved.getFormula()).isSameAs(formula);
    }

    @Test
    void generateMonthlyPayout_success_reusesExistingRowAndDefaultsBadgeToIron_TC004() {
        AdminPayout existing = payout(AdminPayoutStatus.PENDING);
        when(formulaService.getActiveFormula()).thenReturn(formula);
        when(incomeRepository.findCoveredDatesBetween(closedMonthStart, closedMonthEnd))
                .thenReturn(allDaysOfClosedMonth());
        when(incomeRepository.sumBaseAmountByReviewerBetween(closedMonthStart, closedMonthEnd))
                .thenReturn(List.of(baseRow(reviewer.getReviewerId(), 500L)));
        when(reviewerRepository.findAllById(any())).thenReturn(List.of(reviewer));
        when(snapshotRepository.findByPeriodAndPeriodTypeOrderByRankPositionAsc(
                closedMonth, RankingPeriodType.MONTHLY))
                .thenReturn(List.of());
        when(payoutRepository.findByPayoutMonth(closedMonth)).thenReturn(List.of(existing));

        payoutService.generateMonthlyPayout(closedMonth);

        assertThat(existing.getBadge()).isEqualTo(ReviewerBadge.IRON);
        assertThat(existing.getTotalBaseAmount()).isEqualTo(500L);
        assertThat(existing.getTotalFinalAmount()).isEqualTo(500L);
        verify(reviewerIncomeService, never()).generateDailyIncome(any(LocalDate.class));
    }

    // ------------------------------------------------------------ getPayouts

    @Test
    void getPayouts_success_filtersByMonthAndStatus_TC005() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(payoutRepository.findByPayoutMonthAndStatus(closedMonth, AdminPayoutStatus.PAID, pageable))
                .thenReturn(new PageImpl<>(List.of(payout(AdminPayoutStatus.PAID))));

        Page<AdminPayoutResponseDTO> result =
                payoutService.getPayouts(closedMonth, AdminPayoutStatus.PAID, pageable);

        assertThat(result.getContent()).hasSize(1);
        AdminPayoutResponseDTO dto = result.getContent().get(0);
        assertThat(dto.getReviewerId()).isEqualTo(reviewer.getReviewerId());
        assertThat(dto.getReviewerUserName()).isEqualTo("an");
        assertThat(dto.getStatus()).isEqualTo(AdminPayoutStatus.PAID);
        assertThat(dto.getAllowedTransitions()).isEmpty();
        assertThat(dto.getApprovedBy()).isNull();
        assertThat(dto.getFormulaId()).isEqualTo(formula.getId());
    }

    @Test
    void getPayouts_success_filtersByMonthOnly_TC006() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(payoutRepository.findByPayoutMonth(closedMonth, pageable))
                .thenReturn(new PageImpl<>(List.of(payout(AdminPayoutStatus.PENDING))));

        Page<AdminPayoutResponseDTO> result = payoutService.getPayouts(closedMonth, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAllowedTransitions())
                .containsExactly(AdminPayoutStatus.APPROVED, AdminPayoutStatus.CANCELLED);
    }

    @Test
    void getPayouts_success_filtersByStatusOnly_TC007() {
        PageRequest pageable = PageRequest.of(0, 10);
        AdminPayout approved = payout(AdminPayoutStatus.APPROVED);
        approved.setApprovedBy(admin());
        when(payoutRepository.findByStatus(AdminPayoutStatus.APPROVED, pageable))
                .thenReturn(new PageImpl<>(List.of(approved)));

        Page<AdminPayoutResponseDTO> result =
                payoutService.getPayouts(null, AdminPayoutStatus.APPROVED, pageable);

        assertThat(result.getContent().get(0).getApprovedBy()).isEqualTo(approved.getApprovedBy().getUserId());
        assertThat(result.getContent().get(0).getAllowedTransitions())
                .containsExactly(AdminPayoutStatus.PAID, AdminPayoutStatus.CANCELLED);
    }

    @Test
    void getPayouts_success_noFilterListsEverything_TC008() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(payoutRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(payout(AdminPayoutStatus.CANCELLED))));

        assertThat(payoutService.getPayouts(null, null, pageable).getContent()).hasSize(1);
    }

    // ----------------------------------------------------- updatePayoutStatus

    @Test
    void updatePayoutStatus_fail_payoutNotFound_TC009() {
        UUID payoutId = UUID.randomUUID();
        AdminPayoutStatusRequest request = statusRequest(AdminPayoutStatus.APPROVED, null);
        when(payoutRepository.findById(payoutId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> payoutService.updatePayoutStatus(payoutId, UUID.randomUUID(), request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Payout record not found");
    }

    @Test
    void updatePayoutStatus_fail_illegalTransition_TC010() {
        AdminPayout paid = payout(AdminPayoutStatus.PAID);
        AdminPayoutStatusRequest request = statusRequest(AdminPayoutStatus.APPROVED, null);
        when(payoutRepository.findById(paid.getId())).thenReturn(Optional.of(paid));

        assertThatThrownBy(() -> payoutService.updatePayoutStatus(paid.getId(), UUID.randomUUID(), request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cannot transition payout status from PAID to APPROVED");
    }

    @Test
    void updatePayoutStatus_fail_adminNotFound_TC011() {
        AdminPayout pending = payout(AdminPayoutStatus.PENDING);
        UUID adminId = UUID.randomUUID();
        AdminPayoutStatusRequest request = statusRequest(AdminPayoutStatus.APPROVED, null);
        when(payoutRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(userRepository.findById(adminId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> payoutService.updatePayoutStatus(pending.getId(), adminId, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Admin not found");
    }

    @Test
    void updatePayoutStatus_success_approveStampsAdminAndKeepsOldNote_TC012() {
        AdminPayout pending = payout(AdminPayoutStatus.PENDING);
        pending.setNote("ghi chu cu");
        User admin = admin();
        AdminPayoutStatusRequest request = statusRequest(AdminPayoutStatus.APPROVED, null);
        when(payoutRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(userRepository.findById(admin.getUserId())).thenReturn(Optional.of(admin));
        when(payoutRepository.save(pending)).thenReturn(pending);

        AdminPayoutResponseDTO result =
                payoutService.updatePayoutStatus(pending.getId(), admin.getUserId(), request);

        assertThat(result.getStatus()).isEqualTo(AdminPayoutStatus.APPROVED);
        assertThat(pending.getApprovedBy()).isSameAs(admin);
        assertThat(pending.getApprovedAt()).isNotNull();
        assertThat(pending.getNote()).isEqualTo("ghi chu cu");
    }

    @Test
    void updatePayoutStatus_success_cancelOverwritesNote_TC013() {
        AdminPayout pending = payout(AdminPayoutStatus.PENDING);
        pending.setNote("ghi chu cu");
        User admin = admin();
        AdminPayoutStatusRequest request = statusRequest(AdminPayoutStatus.CANCELLED, "huy theo yeu cau");
        when(payoutRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(userRepository.findById(admin.getUserId())).thenReturn(Optional.of(admin));
        when(payoutRepository.save(pending)).thenReturn(pending);

        payoutService.updatePayoutStatus(pending.getId(), admin.getUserId(), request);

        assertThat(pending.getNote()).isEqualTo("huy theo yeu cau");
        assertThat(pending.getApprovedBy()).isNull();
        assertThat(pending.getPaidAt()).isNull();
    }

    @Test
    void updatePayoutStatus_fail_payWithoutStripeAccount_TC014() {
        AdminPayout approved = payout(AdminPayoutStatus.APPROVED);
        User admin = admin();
        AdminPayoutStatusRequest request = statusRequest(AdminPayoutStatus.PAID, null);
        when(payoutRepository.findById(approved.getId())).thenReturn(Optional.of(approved));
        when(userRepository.findById(admin.getUserId())).thenReturn(Optional.of(admin));
        when(stripeAccountRepository.findByReviewerReviewerId(reviewer.getReviewerId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> payoutService.updatePayoutStatus(approved.getId(), admin.getUserId(), request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Reviewer has no Stripe Connect account");
    }

    @Test
    void updatePayoutStatus_fail_payWhenPayoutsNotEnabled_TC015() {
        AdminPayout approved = payout(AdminPayoutStatus.APPROVED);
        User admin = admin();
        AdminPayoutStatusRequest request = statusRequest(AdminPayoutStatus.PAID, null);
        when(payoutRepository.findById(approved.getId())).thenReturn(Optional.of(approved));
        when(userRepository.findById(admin.getUserId())).thenReturn(Optional.of(admin));
        when(stripeAccountRepository.findByReviewerReviewerId(reviewer.getReviewerId()))
                .thenReturn(Optional.of(stripeAccount(false)));

        assertThatThrownBy(() -> payoutService.updatePayoutStatus(approved.getId(), admin.getUserId(), request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not fully verified");
    }

    @Test
    void updatePayoutStatus_success_payCreatesStripeTransfer_TC016() {
        AdminPayout approved = payout(AdminPayoutStatus.APPROVED);
        User admin = admin();
        AdminPayoutStatusRequest request = statusRequest(AdminPayoutStatus.PAID, "chuyen tien thang " + closedMonth);
        when(payoutRepository.findById(approved.getId())).thenReturn(Optional.of(approved));
        when(userRepository.findById(admin.getUserId())).thenReturn(Optional.of(admin));
        when(stripeAccountRepository.findByReviewerReviewerId(reviewer.getReviewerId()))
                .thenReturn(Optional.of(stripeAccount(true)));
        when(payoutRepository.save(approved)).thenReturn(approved);

        Transfer transfer = new Transfer();
        transfer.setId("tr_123");
        try (MockedStatic<Transfer> stripeTransfer = mockStatic(Transfer.class)) {
            stripeTransfer.when(() -> Transfer.create(
                            any(TransferCreateParams.class), any(RequestOptions.class)))
                    .thenReturn(transfer);

            AdminPayoutResponseDTO result =
                    payoutService.updatePayoutStatus(approved.getId(), admin.getUserId(), request);

            assertThat(result.getStatus()).isEqualTo(AdminPayoutStatus.PAID);
            assertThat(result.getStripeTransferId()).isEqualTo("tr_123");
        }

        assertThat(approved.getPaidAt()).isNotNull();
        assertThat(approved.getStripeIdempotencyKey()).isEqualTo("payout-" + approved.getId());
    }

    @Test
    void updatePayoutStatus_fail_stripeTransferRaisesBadGateway_TC017() {
        AdminPayout approved = payout(AdminPayoutStatus.APPROVED);
        User admin = admin();
        AdminPayoutStatusRequest request = statusRequest(AdminPayoutStatus.PAID, null);
        when(payoutRepository.findById(approved.getId())).thenReturn(Optional.of(approved));
        when(userRepository.findById(admin.getUserId())).thenReturn(Optional.of(admin));
        when(stripeAccountRepository.findByReviewerReviewerId(reviewer.getReviewerId()))
                .thenReturn(Optional.of(stripeAccount(true)));

        try (MockedStatic<Transfer> stripeTransfer = mockStatic(Transfer.class)) {
            stripeTransfer.when(() -> Transfer.create(
                            any(TransferCreateParams.class), any(RequestOptions.class)))
                    .thenThrow(new ApiException("insufficient funds", "req_1", "code", 402, null));

            assertThatThrownBy(() ->
                    payoutService.updatePayoutStatus(approved.getId(), admin.getUserId(), request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Stripe transfer failed")
                    .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                            .isEqualTo(HttpStatus.BAD_GATEWAY));
        }

        verify(payoutRepository, never()).save(any(AdminPayout.class));
    }

    // ----------------------------------------------------- markTransferFailed

    @Test
    void markTransferFailed_success_rollsBackPaidToApproved_TC018() {
        AdminPayout paid = payout(AdminPayoutStatus.PAID);
        paid.setPaidAt(LocalDateTime.now());
        paid.setStripeTransferId("tr_123");
        paid.setStripeIdempotencyKey("payout-" + paid.getId());
        when(payoutRepository.findByStripeTransferId("tr_123")).thenReturn(Optional.of(paid));

        payoutService.markTransferFailed("tr_123", "account_closed");

        assertThat(paid.getStatus()).isEqualTo(AdminPayoutStatus.APPROVED);
        assertThat(paid.getPaidAt()).isNull();
        assertThat(paid.getStripeTransferId()).isNull();
        assertThat(paid.getStripeIdempotencyKey()).isNull();
        assertThat(paid.getNote()).isEqualTo("Stripe transfer failed: account_closed");
        verify(payoutRepository).save(paid);
    }

    @Test
    void markTransferFailed_success_appendsToExistingNote_TC019() {
        AdminPayout paid = payout(AdminPayoutStatus.PAID);
        paid.setNote("ghi chu cu");
        when(payoutRepository.findByStripeTransferId("tr_456")).thenReturn(Optional.of(paid));

        payoutService.markTransferFailed("tr_456", "bank_declined");

        assertThat(paid.getNote()).isEqualTo("ghi chu cu | Stripe transfer failed: bank_declined");
    }

    @Test
    void markTransferFailed_success_ignoresPayoutNotInPaidState_TC020() {
        AdminPayout approved = payout(AdminPayoutStatus.APPROVED);
        when(payoutRepository.findByStripeTransferId("tr_789")).thenReturn(Optional.of(approved));

        payoutService.markTransferFailed("tr_789", "duplicate");

        assertThat(approved.getStatus()).isEqualTo(AdminPayoutStatus.APPROVED);
        verify(payoutRepository, never()).save(any(AdminPayout.class));
    }

    @Test
    void markTransferFailed_success_unknownTransferIsIgnored_TC021() {
        when(payoutRepository.findByStripeTransferId("tr_unknown")).thenReturn(Optional.empty());

        payoutService.markTransferFailed("tr_unknown", "not_found");

        verify(payoutRepository, never()).save(any(AdminPayout.class));
    }

    // ------------------------------------------------------------- Helpers

    private List<LocalDate> allDaysOfClosedMonth() {
        return closedMonthStart.datesUntil(closedMonthEnd).toList();
    }

    private Reviewer reviewer() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("an");
        user.setUserAvatar("https://cdn.example.com/an.png");
        Reviewer newReviewer = new Reviewer();
        newReviewer.setReviewerId(UUID.randomUUID());
        newReviewer.setUser(user);
        return newReviewer;
    }

    private User admin() {
        User admin = new User();
        admin.setUserId(UUID.randomUUID());
        admin.setUserName("admin");
        return admin;
    }

    private ReviewerFormula formula() {
        ReviewerFormula newFormula = new ReviewerFormula();
        newFormula.setId(UUID.randomUUID());
        newFormula.setIronMultiplier(BigDecimal.ONE);
        newFormula.setGoldMultiplier(new BigDecimal("2.00"));
        return newFormula;
    }

    private AdminPayout payout(AdminPayoutStatus status) {
        AdminPayout adminPayout = new AdminPayout();
        adminPayout.setId(UUID.randomUUID());
        adminPayout.setReviewer(reviewer);
        adminPayout.setPayoutMonth(closedMonth);
        adminPayout.setTotalBaseAmount(1_000L);
        adminPayout.setTotalFinalAmount(2_000L);
        adminPayout.setBadge(ReviewerBadge.GOLD);
        adminPayout.setBadgeMultiplier(new BigDecimal("2.00"));
        adminPayout.setStatus(status);
        adminPayout.setFormula(formula);
        return adminPayout;
    }

    private ReviewerStripeAccount stripeAccount(boolean payoutsEnabled) {
        ReviewerStripeAccount account = new ReviewerStripeAccount();
        account.setId(UUID.randomUUID());
        account.setReviewer(reviewer);
        account.setStripeAccountId("acct_123");
        account.setPayoutsEnabled(payoutsEnabled);
        return account;
    }

    private ReviewerRankingSnapshot snapshot(Reviewer owner, long score) {
        ReviewerRankingSnapshot newSnapshot = new ReviewerRankingSnapshot();
        newSnapshot.setId(UUID.randomUUID());
        newSnapshot.setReviewer(owner);
        newSnapshot.setPeriod(closedMonth);
        newSnapshot.setPeriodType(RankingPeriodType.MONTHLY);
        newSnapshot.setScore(score);
        return newSnapshot;
    }

    private AdminPayoutStatusRequest statusRequest(AdminPayoutStatus status, String note) {
        AdminPayoutStatusRequest request = new AdminPayoutStatusRequest();
        request.setStatus(status);
        request.setNote(note);
        return request;
    }

    private ReviewerIncomeRepository.ReviewerBaseRow baseRow(UUID reviewerId, long totalBase) {
        return new ReviewerIncomeRepository.ReviewerBaseRow() {
            @Override
            public UUID getReviewerId() {
                return reviewerId;
            }

            @Override
            public Long getTotalBase() {
                return totalBase;
            }
        };
    }
}
