package com.cafestory.controller;

import com.cafestory.dto.requestDTO.AdminPayoutStatusRequest;
import com.cafestory.dto.responseDTO.AdminPayoutResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerIncomeResponseDTO;
import com.cafestory.entity.enums.AdminPayoutStatus;
import com.cafestory.service.serviceInterface.AdminPayoutService;
import com.cafestory.service.serviceInterface.ReviewerIncomeService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/admin/payout")
public class AdminPayoutController {

    private final ReviewerIncomeService incomeService;
    private final AdminPayoutService payoutService;

    public AdminPayoutController(
            ReviewerIncomeService incomeService,
            AdminPayoutService payoutService) {
        this.incomeService = incomeService;
        this.payoutService = payoutService;
    }

    // ── Reviewer daily income ────────────────────────────────────────────────

    @PostMapping("/income/generate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void generateDailyIncome(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        requireUserId(principal);
        incomeService.generateDailyIncome(date != null ? date : LocalDate.now().minusDays(1));
    }

    @GetMapping("/income")
    public Page<ReviewerIncomeResponseDTO> getIncome(
            @RequestParam(required = false) UUID reviewerId,
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String sortDir) {
        String resolvedMonth = month != null ? month : YearMonth.now().toString();
        Sort sort = "asc".equalsIgnoreCase(sortDir)
                ? Sort.by("finalAmount").ascending()
                : Sort.by("finalAmount").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        if (reviewerId != null) {
            return incomeService.getIncomeByReviewer(reviewerId, resolvedMonth, pageable);
        }
        return incomeService.getAllIncome(resolvedMonth, pageable);
    }

    // ── Admin monthly payout ─────────────────────────────────────────────────

    @PostMapping("/monthly/generate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void generateMonthlyPayout(
            @RequestParam(required = false) String month,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        requireUserId(principal);
        String resolvedMonth = month != null ? month : YearMonth.now().minusMonths(1).toString();
        payoutService.generateMonthlyPayout(resolvedMonth);
    }

    @GetMapping("/monthly")
    public Page<AdminPayoutResponseDTO> getPayouts(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) AdminPayoutStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = "asc".equalsIgnoreCase(sortDir)
                ? Sort.by("totalFinalAmount").ascending()
                : Sort.by("totalFinalAmount").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return payoutService.getPayouts(month, status, pageable);
    }

    @PatchMapping("/monthly/{id}/status")
    public AdminPayoutResponseDTO updatePayoutStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AdminPayoutStatusRequest request,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return payoutService.updatePayoutStatus(id, requireUserId(principal), request);
    }
}
