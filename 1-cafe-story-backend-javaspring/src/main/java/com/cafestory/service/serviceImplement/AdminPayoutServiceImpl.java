package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminPayoutStatusRequest;
import com.cafestory.dto.responseDTO.AdminPayoutResponseDTO;
import com.cafestory.entity.AdminPayout;
import com.cafestory.entity.ReviewerFormula;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.ReviewerIncome;
import com.cafestory.entity.ReviewerRankingSnapshot;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.AdminPayoutStatus;
import com.cafestory.entity.enums.RankingPeriodType;
import com.cafestory.entity.enums.ReviewerBadge;
import com.cafestory.repository.AdminPayoutRepository;
import com.cafestory.repository.ReviewerIncomeRepository;
import com.cafestory.repository.ReviewerRankingSnapshotRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceInterface.AdminPayoutService;
import com.cafestory.service.serviceInterface.ReviewerFormulaService;
import com.cafestory.service.serviceInterface.ReviewerIncomeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class AdminPayoutServiceImpl implements AdminPayoutService {

    private static final Logger log = LoggerFactory.getLogger(AdminPayoutServiceImpl.class);

    private final AdminPayoutRepository payoutRepository;
    private final ReviewerIncomeRepository incomeRepository;
    private final ReviewerRankingSnapshotRepository snapshotRepository;
    private final UserRepository userRepository;
    private final ReviewerFormulaService formulaService;
    private final ReviewerIncomeService reviewerIncomeService;

    public AdminPayoutServiceImpl(
            AdminPayoutRepository payoutRepository,
            ReviewerIncomeRepository incomeRepository,
            ReviewerRankingSnapshotRepository snapshotRepository,
            UserRepository userRepository,
            ReviewerFormulaService formulaService,
            ReviewerIncomeService reviewerIncomeService) {
        this.payoutRepository = payoutRepository;
        this.incomeRepository = incomeRepository;
        this.snapshotRepository = snapshotRepository;
        this.userRepository = userRepository;
        this.formulaService = formulaService;
        this.reviewerIncomeService = reviewerIncomeService;
    }

    @Override
    @Transactional
    public void generateMonthlyPayout(String month) {
        YearMonth ym = YearMonth.parse(month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.plusMonths(1).atDay(1);   // exclusive upper bound

        // Guard: block mid-month generation — incomplete data produces wrong payout
        LocalDate today = LocalDate.now();
        if (end.isAfter(today)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot generate payout for an incomplete month. " + month + " ends on "
                            + end.minusDays(1) + ", today is " + today + ".");
        }

        ReviewerFormula formula = formulaService.getActiveFormula();

        List<ReviewerIncome> monthlyIncomes = incomeRepository
                .findByIncomeDateGreaterThanEqualAndIncomeDateLessThan(start, end);

        // Gap detection: find days in the month that have no income record, then backfill
        Set<LocalDate> coveredDates = new HashSet<>();
        for (ReviewerIncome income : monthlyIncomes) {
            coveredDates.add(income.getIncomeDate());
        }
        List<LocalDate> missingDates = new ArrayList<>();
        for (LocalDate d = start; d.isBefore(end); d = d.plusDays(1)) {
            if (!coveredDates.contains(d)) missingDates.add(d);
        }
        if (!missingDates.isEmpty()) {
            log.warn("generateMonthlyPayout[{}]: missing income for {} day(s) — backfilling: {}",
                    month, missingDates.size(), missingDates);
            for (LocalDate missing : missingDates) {
                reviewerIncomeService.generateDailyIncome(missing);
            }
            // Re-query to include newly generated records
            monthlyIncomes = incomeRepository
                    .findByIncomeDateGreaterThanEqualAndIncomeDateLessThan(start, end);
        }

        Map<UUID, Long> totalBaseByReviewerId = new HashMap<>();
        Map<UUID, Reviewer> reviewerById = new HashMap<>();
        for (ReviewerIncome income : monthlyIncomes) {
            UUID rid = income.getReviewer().getReviewerId();
            totalBaseByReviewerId.merge(rid, income.getBaseAmount(), Long::sum);
            reviewerById.putIfAbsent(rid, income.getReviewer());
        }

        if (totalBaseByReviewerId.isEmpty()) {
            return;
        }

        Map<UUID, ReviewerBadge> badgeByReviewerId = new HashMap<>();
        for (ReviewerRankingSnapshot snapshot : snapshotRepository
                .findByPeriodAndPeriodTypeOrderByRankPositionAsc(month, RankingPeriodType.MONTHLY)) {
            badgeByReviewerId.put(snapshot.getReviewer().getReviewerId(), snapshot.getBadge());
        }

        Map<UUID, AdminPayout> existingPayouts = new HashMap<>();
        for (AdminPayout payout : payoutRepository.findByPayoutMonth(month)) {
            existingPayouts.put(payout.getReviewer().getReviewerId(), payout);
        }

        List<AdminPayout> toSave = new ArrayList<>();
        for (Map.Entry<UUID, Long> entry : totalBaseByReviewerId.entrySet()) {
            UUID reviewerId = entry.getKey();
            Reviewer reviewer = reviewerById.get(reviewerId);
            long totalBase = entry.getValue();
            ReviewerBadge badge = badgeByReviewerId.getOrDefault(reviewerId, ReviewerBadge.IRON);
            BigDecimal multiplier = formula.getMultiplierForBadge(badge);
            long totalFinal = BigDecimal.valueOf(totalBase).multiply(multiplier).longValue();

            AdminPayout payout = existingPayouts.getOrDefault(reviewerId, new AdminPayout());
            payout.setReviewer(reviewer);
            payout.setPayoutMonth(month);
            payout.setTotalBaseAmount(totalBase);
            payout.setBadge(badge);
            payout.setBadgeMultiplier(multiplier);
            payout.setTotalFinalAmount(totalFinal);
            payout.setFormula(formula);
            toSave.add(payout);
        }
        payoutRepository.saveAll(toSave);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminPayoutResponseDTO> getPayouts(String month, AdminPayoutStatus status, Pageable pageable) {
        Page<AdminPayout> page;
        if (month != null && status != null) {
            page = payoutRepository.findByPayoutMonthAndStatus(month, status, pageable);
        } else if (month != null) {
            page = payoutRepository.findByPayoutMonth(month, pageable);
        } else if (status != null) {
            page = payoutRepository.findByStatus(status, pageable);
        } else {
            page = payoutRepository.findAll(pageable);
        }
        return page.map(this::toResponse);
    }

    @Override
    @Transactional
    public AdminPayoutResponseDTO updatePayoutStatus(UUID payoutId, UUID adminUserId, AdminPayoutStatusRequest request) {
        AdminPayout payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payout record not found"));

        validateStatusTransition(payout.getStatus(), request.getStatus());

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));

        payout.setStatus(request.getStatus());
        payout.setNote(request.getNote());

        if (request.getStatus() == AdminPayoutStatus.APPROVED) {
            payout.setApprovedBy(admin);
            payout.setApprovedAt(LocalDateTime.now());
        } else if (request.getStatus() == AdminPayoutStatus.PAID) {
            payout.setPaidAt(LocalDateTime.now());
        }

        return toResponse(payoutRepository.save(payout));
    }

    private void validateStatusTransition(AdminPayoutStatus current, AdminPayoutStatus next) {
        boolean valid = switch (current) {
            case PENDING -> next == AdminPayoutStatus.APPROVED || next == AdminPayoutStatus.CANCELLED;
            case APPROVED -> next == AdminPayoutStatus.PAID || next == AdminPayoutStatus.CANCELLED;
            case PAID, CANCELLED -> false;
        };
        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot transition payout status from " + current + " to " + next);
        }
    }

    private AdminPayoutResponseDTO toResponse(AdminPayout payout) {
        AdminPayoutResponseDTO dto = new AdminPayoutResponseDTO();
        dto.setId(payout.getId());
        dto.setReviewerId(payout.getReviewer().getReviewerId());
        dto.setReviewerUserName(payout.getReviewer().getUser().getUserName());
        dto.setReviewerUserAvatar(payout.getReviewer().getUser().getUserAvatar());
        dto.setPayoutMonth(payout.getPayoutMonth());
        dto.setTotalBaseAmount(payout.getTotalBaseAmount());
        dto.setBadge(payout.getBadge());
        dto.setBadgeMultiplier(payout.getBadgeMultiplier());
        dto.setTotalFinalAmount(payout.getTotalFinalAmount());
        dto.setStatus(payout.getStatus());
        dto.setApprovedBy(payout.getApprovedBy() != null ? payout.getApprovedBy().getUserId() : null);
        dto.setApprovedAt(payout.getApprovedAt());
        dto.setPaidAt(payout.getPaidAt());
        dto.setNote(payout.getNote());
        dto.setFormulaId(payout.getFormula().getId());
        dto.setCreatedAt(payout.getCreatedAt());
        dto.setUpdatedAt(payout.getUpdatedAt());
        return dto;
    }
}
