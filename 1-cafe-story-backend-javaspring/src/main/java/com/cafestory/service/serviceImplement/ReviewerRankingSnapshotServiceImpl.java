package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.ReviewerRankingSnapshotResponseDTO;
import com.cafestory.entity.BlogLike;
import com.cafestory.entity.BlogShare;
import com.cafestory.entity.Comment;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.ReviewerFormula;
import com.cafestory.entity.ReviewerRankingSnapshot;
import com.cafestory.entity.enums.RankingPeriodType;
import com.cafestory.entity.enums.ReviewerBadge;
import com.cafestory.repository.BlogLikeRepository;
import com.cafestory.repository.BlogShareRepository;
import com.cafestory.repository.CommentRepository;
import com.cafestory.repository.ReviewerRankingSnapshotRepository;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.service.serviceInterface.ReviewerBadgeThresholdService;
import com.cafestory.service.serviceInterface.ReviewerFormulaService;
import com.cafestory.service.serviceInterface.ReviewerRankingSnapshotService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReviewerRankingSnapshotServiceImpl implements ReviewerRankingSnapshotService {

    private final ReviewerRankingSnapshotRepository snapshotRepository;
    private final ReviewerRepository reviewerRepository;
    private final BlogLikeRepository blogLikeRepository;
    private final BlogShareRepository blogShareRepository;
    private final CommentRepository commentRepository;
    private final ReviewerFormulaService formulaService;
    private final ReviewerBadgeThresholdService badgeThresholdService;

    public ReviewerRankingSnapshotServiceImpl(
            ReviewerRankingSnapshotRepository snapshotRepository,
            ReviewerRepository reviewerRepository,
            BlogLikeRepository blogLikeRepository,
            BlogShareRepository blogShareRepository,
            CommentRepository commentRepository,
            ReviewerFormulaService formulaService,
            ReviewerBadgeThresholdService badgeThresholdService) {
        this.snapshotRepository = snapshotRepository;
        this.reviewerRepository = reviewerRepository;
        this.blogLikeRepository = blogLikeRepository;
        this.blogShareRepository = blogShareRepository;
        this.commentRepository = commentRepository;
        this.formulaService = formulaService;
        this.badgeThresholdService = badgeThresholdService;
    }

    @Override
    @Transactional
    public void generateSnapshot(RankingPeriodType periodType) {
        generateSnapshot(periodType, LocalDate.now());
    }

    @Override
    @Transactional
    public void generateSnapshot(RankingPeriodType periodType, LocalDate referenceDate) {
        LocalDate today = referenceDate;
        String period = resolvePeriod(today, periodType);
        DateRange range = resolveDateRange(today, periodType);
        ReviewerFormula formula = formulaService.getActiveFormula();

        List<Reviewer> allReviewers = reviewerRepository.findAll();
        Map<UUID, Reviewer> reviewersByUserId = new HashMap<>();
        Map<UUID, long[]> counts = new HashMap<>();
        for (Reviewer reviewer : allReviewers) {
            reviewersByUserId.put(reviewer.getUser().getUserId(), reviewer);
            counts.put(reviewer.getReviewerId(), new long[]{0, 0, 0});
        }

        for (BlogLike like : blogLikeRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(range.startDate(), range.endDate())) {
            Reviewer reviewer = reviewersByUserId.get(like.getUser().getUserId());
            if (reviewer != null) {
                counts.get(reviewer.getReviewerId())[0]++;
            }
        }
        for (BlogShare share : blogShareRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(range.startDate(), range.endDate())) {
            Reviewer reviewer = reviewersByUserId.get(share.getUser().getUserId());
            if (reviewer != null) {
                counts.get(reviewer.getReviewerId())[1]++;
            }
        }
        for (Comment comment : commentRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(range.startDate(), range.endDate())) {
            Reviewer reviewer = reviewersByUserId.get(comment.getUser().getUserId());
            if (reviewer != null) {
                counts.get(reviewer.getReviewerId())[2]++;
            }
        }

        Map<UUID, ReviewerRankingSnapshot> existingByReviewerId = new HashMap<>();
        for (ReviewerRankingSnapshot existing : snapshotRepository.findByPeriodAndPeriodTypeOrderByRankPositionAsc(period, periodType)) {
            existingByReviewerId.put(existing.getReviewer().getReviewerId(), existing);
        }

        List<SnapshotEntry> entries = new ArrayList<>();
        for (Reviewer reviewer : allReviewers) {
            long[] c = counts.get(reviewer.getReviewerId());
            long score = formulaService.calculateScore(c[0], c[1], c[2]);
            ReviewerBadge badge = badgeThresholdService.badgeForScore(score);
            entries.add(new SnapshotEntry(reviewer, c[0], c[1], c[2], score, badge));
        }
        entries.sort(Comparator.comparingLong(SnapshotEntry::score).reversed());

        List<ReviewerRankingSnapshot> toSave = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            SnapshotEntry entry = entries.get(i);
            ReviewerRankingSnapshot snapshot = existingByReviewerId
                    .getOrDefault(entry.reviewer().getReviewerId(), new ReviewerRankingSnapshot());
            snapshot.setReviewer(entry.reviewer());
            snapshot.setPeriod(period);
            snapshot.setPeriodType(periodType);
            snapshot.setRankPosition(i + 1);
            snapshot.setScore(entry.score());
            snapshot.setLikeCount(entry.likeCount());
            snapshot.setShareCount(entry.shareCount());
            snapshot.setCommentCount(entry.commentCount());
            snapshot.setBadge(entry.badge());
            snapshot.setFormula(formula);
            toSave.add(snapshot);
        }
        snapshotRepository.saveAll(toSave);
    }

    @Override
    @Transactional
    public void initSnapshotForNewReviewer(Reviewer reviewer) {
        LocalDate today = LocalDate.now();
        ReviewerFormula formula = formulaService.getActiveFormula();
        ReviewerBadge defaultBadge = badgeThresholdService.badgeForScore(0);

        for (RankingPeriodType periodType : RankingPeriodType.values()) {
            String period = resolvePeriod(today, periodType);
            boolean exists = snapshotRepository
                    .findByReviewerReviewerIdAndPeriodAndPeriodType(
                            reviewer.getReviewerId(), period, periodType)
                    .isPresent();
            if (!exists) {
                ReviewerRankingSnapshot snapshot = new ReviewerRankingSnapshot();
                snapshot.setReviewer(reviewer);
                snapshot.setPeriod(period);
                snapshot.setPeriodType(periodType);
                snapshot.setRankPosition(0);
                snapshot.setScore(0);
                snapshot.setLikeCount(0);
                snapshot.setShareCount(0);
                snapshot.setCommentCount(0);
                snapshot.setBadge(defaultBadge);
                snapshot.setFormula(formula);
                snapshotRepository.save(snapshot);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewerRankingSnapshotResponseDTO> getRanking(String period, RankingPeriodType periodType, int page, int limit) {
        int sanitizedPage = Math.max(page, 1);
        int sanitizedLimit = Math.max(1, Math.min(limit, 100));
        List<ReviewerRankingSnapshot> all = snapshotRepository
                .findByPeriodAndPeriodTypeOrderByRankPositionAsc(period, periodType);
        int fromIndex = Math.min((sanitizedPage - 1) * sanitizedLimit, all.size());
        int toIndex = Math.min(fromIndex + sanitizedLimit, all.size());
        return all.subList(fromIndex, toIndex)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private String resolvePeriod(LocalDate date, RankingPeriodType periodType) {
        return switch (periodType) {
            case DAILY -> date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            case WEEKLY -> date.getYear() + "-W" + String.format("%02d", date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
            case MONTHLY -> YearMonth.from(date).toString();
        };
    }

    private DateRange resolveDateRange(LocalDate date, RankingPeriodType periodType) {
        return switch (periodType) {
            case DAILY -> new DateRange(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
            case WEEKLY -> {
                LocalDate monday = date.with(java.time.DayOfWeek.MONDAY);
                yield new DateRange(monday.atStartOfDay(), monday.plusWeeks(1).atStartOfDay());
            }
            case MONTHLY -> {
                YearMonth ym = YearMonth.from(date);
                yield new DateRange(ym.atDay(1).atStartOfDay(), ym.plusMonths(1).atDay(1).atStartOfDay());
            }
        };
    }

    private ReviewerRankingSnapshotResponseDTO toResponseDTO(ReviewerRankingSnapshot snapshot) {
        ReviewerRankingSnapshotResponseDTO dto = new ReviewerRankingSnapshotResponseDTO();
        dto.setId(snapshot.getId());
        dto.setReviewerId(snapshot.getReviewer().getReviewerId());
        dto.setReviewerUserName(snapshot.getReviewer().getUser().getUserName());
        dto.setReviewerUserAvatar(snapshot.getReviewer().getUser().getUserAvatar());
        dto.setPeriod(snapshot.getPeriod());
        dto.setPeriodType(snapshot.getPeriodType());
        dto.setRankPosition(snapshot.getRankPosition());
        dto.setScore(snapshot.getScore());
        dto.setLikeCount(snapshot.getLikeCount());
        dto.setShareCount(snapshot.getShareCount());
        dto.setCommentCount(snapshot.getCommentCount());
        dto.setBadge(snapshot.getBadge());
        dto.setFormulaId(snapshot.getFormula().getId());
        return dto;
    }

    private record DateRange(LocalDateTime startDate, LocalDateTime endDate) {
    }

    private record SnapshotEntry(Reviewer reviewer, long likeCount, long shareCount, long commentCount, long score, ReviewerBadge badge) {
    }
}
