package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminPayoutStatusRequest;
import com.cafestory.dto.responseDTO.AdminPayoutResponseDTO;
import com.cafestory.entity.AdminPayout;
import com.cafestory.entity.ReviewerFormula;
import com.cafestory.entity.Reviewer;
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
import com.cafestory.service.serviceInterface.AdminPayoutService;
import com.cafestory.service.serviceInterface.ReviewerBadgeThresholdService;
import com.cafestory.service.serviceInterface.ReviewerFormulaService;
import com.cafestory.service.serviceInterface.ReviewerIncomeService;
import com.stripe.exception.StripeException;
import com.stripe.model.Transfer;
import com.stripe.net.RequestOptions;
import com.stripe.param.TransferCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class AdminPayoutServiceImpl implements AdminPayoutService {

    private static final Logger log = LoggerFactory.getLogger(AdminPayoutServiceImpl.class);

    /**
     * Loại tiền không có đơn vị lẻ: Stripe nhận số nguyên đúng bằng mệnh giá.
     * Các loại còn lại tính theo đơn vị nhỏ nhất (USD -> cent).
     */
    private static final Set<String> ZERO_DECIMAL_CURRENCIES =
            Set.of("vnd", "jpy", "krw", "clp", "isk", "ugx");

    private final String stripeSecretKey;
    private final String payoutCurrency;
    private final BigDecimal vndPerPayoutUnit;
    private final AdminPayoutRepository payoutRepository;
    private final ReviewerIncomeRepository incomeRepository;
    private final ReviewerRankingSnapshotRepository snapshotRepository;
    private final UserRepository userRepository;
    private final ReviewerFormulaService formulaService;
    private final ReviewerIncomeService reviewerIncomeService;
    private final ReviewerStripeAccountRepository stripeAccountRepository;
    private final ReviewerBadgeThresholdService badgeThresholdService;
    private final ReviewerRepository reviewerRepository;

    public AdminPayoutServiceImpl(
            @Value("${stripe.secret-key:}") String stripeSecretKey,
            @Value("${stripe.payout.currency:usd}") String payoutCurrency,
            @Value("${stripe.payout.vnd-per-unit:25000}") BigDecimal vndPerPayoutUnit,
            AdminPayoutRepository payoutRepository,
            ReviewerIncomeRepository incomeRepository,
            ReviewerRankingSnapshotRepository snapshotRepository,
            UserRepository userRepository,
            ReviewerFormulaService formulaService,
            ReviewerIncomeService reviewerIncomeService,
            ReviewerStripeAccountRepository stripeAccountRepository,
            ReviewerBadgeThresholdService badgeThresholdService,
            ReviewerRepository reviewerRepository) {
        this.stripeSecretKey = stripeSecretKey;
        this.payoutCurrency = payoutCurrency.trim().toLowerCase();
        // Sai tỉ giá thì mọi lệnh chuyển tiền đều sai — chặn ngay lúc khởi động
        // thay vì để phát hiện sau khi đã chuyển nhầm.
        if (vndPerPayoutUnit.signum() <= 0) {
            throw new IllegalArgumentException(
                    "stripe.payout.vnd-per-unit phải lớn hơn 0, đang là " + vndPerPayoutUnit);
        }
        this.vndPerPayoutUnit = vndPerPayoutUnit;
        this.payoutRepository = payoutRepository;
        this.incomeRepository = incomeRepository;
        this.snapshotRepository = snapshotRepository;
        this.userRepository = userRepository;
        this.formulaService = formulaService;
        this.reviewerIncomeService = reviewerIncomeService;
        this.stripeAccountRepository = stripeAccountRepository;
        this.badgeThresholdService = badgeThresholdService;
        this.reviewerRepository = reviewerRepository;
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

        // Gap detection: find days in the month that have no income record, then backfill
        Set<LocalDate> coveredDates = new HashSet<>(incomeRepository.findCoveredDatesBetween(start, end));
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
        }

        Map<UUID, Long> totalBaseByReviewerId = new HashMap<>();
        for (ReviewerIncomeRepository.ReviewerBaseRow row
                : incomeRepository.sumBaseAmountByReviewerBetween(start, end)) {
            totalBaseByReviewerId.put(row.getReviewerId(), row.getTotalBase());
        }

        if (totalBaseByReviewerId.isEmpty()) {
            return;
        }

        Map<UUID, Reviewer> reviewerById = new HashMap<>();
        for (Reviewer reviewer : reviewerRepository.findAllById(totalBaseByReviewerId.keySet())) {
            reviewerById.put(reviewer.getReviewerId(), reviewer);
        }

        Map<UUID, ReviewerBadge> badgeByReviewerId = new HashMap<>();
        for (ReviewerRankingSnapshot snapshot : snapshotRepository
                .findByPeriodAndPeriodTypeOrderByRankPositionAsc(month, RankingPeriodType.MONTHLY)) {
            badgeByReviewerId.put(
                    snapshot.getReviewer().getReviewerId(),
                    badgeThresholdService.badgeForScore(snapshot.getScore()));
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
        // Chỉ ghi đè khi admin thực sự nhập note — trước đây update không kèm
        // note sẽ xoá trắng note cũ.
        if (request.getNote() != null) {
            payout.setNote(request.getNote());
        }

        if (request.getStatus() == AdminPayoutStatus.APPROVED) {
            payout.setApprovedBy(admin);
            payout.setApprovedAt(LocalDateTime.now());
        } else if (request.getStatus() == AdminPayoutStatus.PAID) {
            triggerStripeTransfer(payout);
            payout.setPaidAt(LocalDateTime.now());
        }

        return toResponse(payoutRepository.save(payout));
    }

    @Override
    @Transactional
    public void markTransferFailed(String stripeTransferId, String reason) {
        payoutRepository.findByStripeTransferId(stripeTransferId).ifPresentOrElse(payout -> {
            if (payout.getStatus() != AdminPayoutStatus.PAID) {
                log.warn("transfer.failed cho transfer {} nhưng payout {} đang ở {} — bỏ qua",
                        stripeTransferId, payout.getId(), payout.getStatus());
                return;
            }
            // Về APPROVED chứ không phải PENDING: khoản này đã được duyệt, chỉ
            // có bước chuyển tiền hỏng. Admin chỉ cần bấm PAID lại.
            payout.setStatus(AdminPayoutStatus.APPROVED);
            payout.setPaidAt(null);
            payout.setStripeTransferId(null);
            payout.setStripeIdempotencyKey(null);
            payout.setNote(appendNote(payout.getNote(), "Stripe transfer failed: " + reason));
            payoutRepository.save(payout);
            log.error("Payout {} rollback PAID -> APPROVED do transfer {} hỏng: {}",
                    payout.getId(), stripeTransferId, reason);
        }, () -> log.error(
                "transfer.failed cho transfer {} nhưng không tìm thấy admin_payout tương ứng",
                stripeTransferId));
    }

    private String appendNote(String existing, String addition) {
        if (existing == null || existing.isBlank()) {
            return addition;
        }
        return existing + " | " + addition;
    }

    private void triggerStripeTransfer(AdminPayout payout) {
        ReviewerStripeAccount stripeAccount = stripeAccountRepository
                .findByReviewerReviewerId(payout.getReviewer().getReviewerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Reviewer has no Stripe Connect account. Cannot process payout."));

        if (!stripeAccount.isPayoutsEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Reviewer's Stripe account is not fully verified. Cannot send payout.");
        }

        long stripeAmount = toStripeAmount(payout.getTotalFinalAmount());
        String idempotencyKey = buildIdempotencyKey(
                payout, stripeAmount, stripeAccount.getStripeAccountId());
        try {
            TransferCreateParams params = TransferCreateParams.builder()
                    .setAmount(stripeAmount)
                    .setCurrency(payoutCurrency)
                    .setDestination(stripeAccount.getStripeAccountId())
                    .build();
            // API key truyền theo lời gọi thay vì gán Stripe.apiKey static —
            // biến toàn cục đó bị nhiều class cùng ghi.
            Transfer transfer = Transfer.create(
                    params,
                    RequestOptions.builder()
                            .setApiKey(stripeSecretKey)
                            .setIdempotencyKey(idempotencyKey)
                            .build()
            );
            payout.setStripeTransferId(transfer.getId());
            payout.setStripeIdempotencyKey(idempotencyKey);
            log.info("Stripe transfer {} created for payout {}: {} VND -> {} {}",
                    transfer.getId(), payout.getId(), payout.getTotalFinalAmount(),
                    stripeAmount, payoutCurrency);
        } catch (StripeException e) {
            log.error("Stripe transfer failed for payout {}: {}", payout.getId(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Stripe transfer failed: " + e.getMessage());
        }
    }

    /**
     * Khoá idempotency phải mô tả ĐÚNG lệnh chuyển sắp gửi, không phải chỉ định
     * danh payout.
     *
     * <p>Stripe lưu khoá kèm tham số trong 24h và từ chối khi cùng khoá được
     * dùng lại với tham số khác ("Keys for idempotent requests can only be used
     * with the same parameters they were first used with"). Khoá cố định
     * {@code payout-<id>} vì thế khoá chết luôn payout sau lần thử đầu nếu số
     * tiền, loại tiền hoặc tài khoản đích thay đổi.
     *
     * <p>Gộp thêm {@code updatedAt} để mỗi lần admin bấm lại sau khi webhook
     * {@code transfer.failed} kéo trạng thái về APPROVED là một khoá mới. Không
     * có nó, Stripe trả lại bản ghi transfer hỏng cũ trong 24h và hệ thống đánh
     * dấu PAID mà tiền không hề đi.
     *
     * <p>Hai request bấm trùng cùng lúc đọc cùng một bản ghi nên {@code updatedAt}
     * giống nhau, ra cùng khoá — vẫn chặn được double-send.
     */
    private String buildIdempotencyKey(AdminPayout payout, long stripeAmount, String destination) {
        String fingerprint = String.join("|",
                payout.getId().toString(),
                String.valueOf(payout.getUpdatedAt()),
                String.valueOf(stripeAmount),
                payoutCurrency,
                destination);
        return "payout-" + payout.getId() + "-" + shortHash(fingerprint);
    }

    private static String shortHash(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 không khả dụng", e);
        }
    }

    /**
     * Quy đổi số tiền payout (luôn lưu bằng VND) sang đơn vị Stripe của
     * {@code stripe.payout.currency}.
     *
     * <p>Transfer chỉ rút được từ số dư CÙNG loại tiền, mà Stripe không hỗ trợ
     * Việt Nam làm quốc gia của platform: tài khoản không giữ được số dư VND nên
     * chuyển thẳng {@code "vnd"} luôn trả về {@code balance_insufficient}. Vì vậy
     * loại tiền gửi Stripe tách khỏi loại tiền lưu trong DB — DB vẫn là VND, chỉ
     * lệnh chuyển đi được quy đổi.
     *
     * <p>Đơn vị phụ thuộc loại tiền đích: VND không có đơn vị lẻ (1 = 1 đồng),
     * USD tính theo cent (1 = 0.01 USD).
     */
    private long toStripeAmount(long amountVnd) {
        if ("vnd".equals(payoutCurrency)) {
            return amountVnd;
        }
        BigDecimal converted = BigDecimal.valueOf(amountVnd)
                .divide(vndPerPayoutUnit, 10, RoundingMode.HALF_UP);
        if (!ZERO_DECIMAL_CURRENCIES.contains(payoutCurrency)) {
            converted = converted.multiply(BigDecimal.valueOf(100));
        }
        long stripeAmount = converted.setScale(0, RoundingMode.HALF_UP).longValue();
        // Làm tròn xuống 0 nghĩa là khoản này nhỏ hơn đơn vị nhỏ nhất Stripe nhận.
        // Báo rõ thay vì để Stripe trả lỗi khó hiểu về amount không hợp lệ.
        if (stripeAmount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Payout " + amountVnd + " VND quy đổi ra " + payoutCurrency.toUpperCase()
                            + " nhỏ hơn đơn vị nhỏ nhất Stripe chấp nhận. Không thể chuyển.");
        }
        return stripeAmount;
    }

    /**
     * Luồng trạng thái hợp lệ. Vừa dùng để chặn ở server, vừa trả ra DTO cho
     * admin UI render nút — một nguồn luật duy nhất.
     *
     * <p>PAID -> APPROVED cố ý không có ở đây: chỉ webhook transfer.failed mới
     * được rollback, qua {@link #markTransferFailed}.
     */
    private List<AdminPayoutStatus> allowedTransitions(AdminPayoutStatus current) {
        return switch (current) {
            case PENDING -> List.of(AdminPayoutStatus.APPROVED, AdminPayoutStatus.CANCELLED);
            case APPROVED -> List.of(AdminPayoutStatus.PAID, AdminPayoutStatus.CANCELLED);
            case PAID, CANCELLED -> List.of();
        };
    }

    private void validateStatusTransition(AdminPayoutStatus current, AdminPayoutStatus next) {
        if (!allowedTransitions(current).contains(next)) {
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
        dto.setStripeTransferId(payout.getStripeTransferId());
        dto.setAllowedTransitions(allowedTransitions(payout.getStatus()));
        dto.setCreatedAt(payout.getCreatedAt());
        dto.setUpdatedAt(payout.getUpdatedAt());
        return dto;
    }
}
