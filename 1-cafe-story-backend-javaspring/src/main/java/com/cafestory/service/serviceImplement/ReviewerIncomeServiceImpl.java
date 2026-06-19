package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.payout.ReviewerIncomeResponse;
import com.cafestory.entity.BlogLike;
import com.cafestory.entity.BlogShare;
import com.cafestory.entity.Comment;
import com.cafestory.entity.PayoutFormula;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.ReviewerIncome;
import com.cafestory.entity.ReviewerRankingSnapshot;
import com.cafestory.entity.enums.RankingPeriodType;
import com.cafestory.entity.enums.ReviewerBadge;
import com.cafestory.repository.BlogLikeRepository;
import com.cafestory.repository.BlogShareRepository;
import com.cafestory.repository.CommentRepository;
import com.cafestory.repository.ReviewerIncomeRepository;
import com.cafestory.repository.ReviewerRankingSnapshotRepository;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.service.serviceInterface.PayoutFormulaService;
import com.cafestory.service.serviceInterface.ReviewerIncomeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReviewerIncomeServiceImpl implements ReviewerIncomeService {

    private final ReviewerIncomeRepository incomeRepository;
    private final ReviewerRepository reviewerRepository;
    private final ReviewerRankingSnapshotRepository snapshotRepository;
    private final BlogLikeRepository blogLikeRepository;
    private final BlogShareRepository blogShareRepository;
    private final CommentRepository commentRepository;
    private final PayoutFormulaService payoutFormulaService;

    public ReviewerIncomeServiceImpl(
            ReviewerIncomeRepository incomeRepository,
            ReviewerRepository reviewerRepository,
            ReviewerRankingSnapshotRepository snapshotRepository,
            BlogLikeRepository blogLikeRepository,
            BlogShareRepository blogShareRepository,
            CommentRepository commentRepository,
            PayoutFormulaService payoutFormulaService) {
        this.incomeRepository = incomeRepository;
        this.reviewerRepository = reviewerRepository;
        this.snapshotRepository = snapshotRepository;
        this.blogLikeRepository = blogLikeRepository;
        this.blogShareRepository = blogShareRepository;
        this.commentRepository = commentRepository;
        this.payoutFormulaService = payoutFormulaService;
    }

    @Override
    @Transactional
    public void generateDailyIncome(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        String period = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        PayoutFormula formula = payoutFormulaService.getActiveFormula();

        // Load all reviewers, build userId→reviewer and reviewerId→counts maps
        List<Reviewer> allReviewers = reviewerRepository.findAll();
        Map<UUID, Reviewer> byUserId = new HashMap<>();
        Map<UUID, long[]> counts = new HashMap<>();
        for (Reviewer reviewer : allReviewers) {
            byUserId.put(reviewer.getUser().getUserId(), reviewer);
            counts.put(reviewer.getReviewerId(), new long[]{0L, 0L, 0L}); // like, share, comment
        }

        // Count engagement in the 24h window
        for (BlogLike like : blogLikeRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end)) {
            Reviewer reviewer = byUserId.get(like.getUser().getUserId());
            if (reviewer != null) counts.get(reviewer.getReviewerId())[0]++;
        }
        for (BlogShare share : blogShareRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end)) {
            Reviewer reviewer = byUserId.get(share.getUser().getUserId());
            if (reviewer != null) counts.get(reviewer.getReviewerId())[1]++;
        }
        for (Comment comment : commentRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end)) {
            Reviewer reviewer = byUserId.get(comment.getUser().getUserId());
            if (reviewer != null) counts.get(reviewer.getReviewerId())[2]++;
        }

        // Batch load DAILY ranking snapshots for badge lookup
        Map<UUID, ReviewerBadge> badgeByReviewerId = new HashMap<>();
        for (ReviewerRankingSnapshot snapshot : snapshotRepository
                .findByPeriodAndPeriodTypeOrderByRankPositionAsc(period, RankingPeriodType.DAILY)) {
            badgeByReviewerId.put(snapshot.getReviewer().getReviewerId(), snapshot.getBadge());
        }

        // Batch load existing income records for this date → upsert map
        Map<UUID, ReviewerIncome> existingByReviewerId = new HashMap<>();
        for (ReviewerIncome income : incomeRepository.findByIncomeDate(date)) {
            existingByReviewerId.put(income.getReviewer().getReviewerId(), income);
        }

        List<ReviewerIncome> toSave = new ArrayList<>();
        for (Reviewer reviewer : allReviewers) {
            long[] c = counts.get(reviewer.getReviewerId());
            long likeCount = c[0], shareCount = c[1], commentCount = c[2];

            ReviewerBadge badge = badgeByReviewerId.getOrDefault(reviewer.getReviewerId(), ReviewerBadge.IRON);
            BigDecimal multiplier = formula.getMultiplierForBadge(badge);
            long baseAmount = likeCount * formula.getLikePayoutAmount()
                    + commentCount * formula.getCommentPayoutAmount()
                    + shareCount * formula.getSharePayoutAmount();
            long finalAmount = BigDecimal.valueOf(baseAmount).multiply(multiplier).longValue();

            ReviewerIncome income = existingByReviewerId.getOrDefault(reviewer.getReviewerId(), new ReviewerIncome());
            income.setReviewer(reviewer);
            income.setIncomeDate(date);
            income.setLikeCount(likeCount);
            income.setShareCount(shareCount);
            income.setCommentCount(commentCount);
            income.setBadge(badge);
            income.setBadgeMultiplier(multiplier);
            income.setBaseAmount(baseAmount);
            income.setFinalAmount(finalAmount);
            income.setFormula(formula);
            toSave.add(income);
        }
        incomeRepository.saveAll(toSave);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewerIncomeResponse> getIncomeByReviewer(UUID reviewerId, String month, Pageable pageable) {
        YearMonth ym = YearMonth.parse(month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.plusMonths(1).atDay(1);
        return incomeRepository
                .findByReviewerReviewerIdAndIncomeDateGreaterThanEqualAndIncomeDateLessThan(reviewerId, start, end, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewerIncomeResponse> getAllIncome(String month, Pageable pageable) {
        YearMonth ym = YearMonth.parse(month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.plusMonths(1).atDay(1);
        return incomeRepository
                .findByIncomeDateGreaterThanEqualAndIncomeDateLessThan(start, end, pageable)
                .map(this::toResponse);
    }

    private ReviewerIncomeResponse toResponse(ReviewerIncome income) {
        ReviewerIncomeResponse dto = new ReviewerIncomeResponse();
        dto.setId(income.getId());
        dto.setReviewerId(income.getReviewer().getReviewerId());
        dto.setReviewerUserName(income.getReviewer().getUser().getUserName());
        dto.setReviewerUserAvatar(income.getReviewer().getUser().getUserAvatar());
        dto.setIncomeDate(income.getIncomeDate());
        dto.setLikeCount(income.getLikeCount());
        dto.setCommentCount(income.getCommentCount());
        dto.setShareCount(income.getShareCount());
        dto.setBadge(income.getBadge());
        dto.setBadgeMultiplier(income.getBadgeMultiplier());
        dto.setBaseAmount(income.getBaseAmount());
        dto.setFinalAmount(income.getFinalAmount());
        dto.setFormulaId(income.getFormula().getId());
        dto.setCreatedAt(income.getCreatedAt());
        dto.setUpdatedAt(income.getUpdatedAt());
        return dto;
    }
}
