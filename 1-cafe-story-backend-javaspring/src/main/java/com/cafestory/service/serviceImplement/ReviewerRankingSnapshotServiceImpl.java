package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.ReviewerRankingSnapshotResponseDTO;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.ReviewerBadgeHistory;
import com.cafestory.entity.ReviewerFormula;
import com.cafestory.entity.ReviewerRankingSnapshot;
import com.cafestory.entity.enums.RankingPeriodType;
import com.cafestory.repository.AuthorInteractionCountRow;
import com.cafestory.repository.BlogLikeRepository;
import com.cafestory.repository.BlogShareRepository;
import com.cafestory.repository.CommentRepository;
import com.cafestory.repository.ReviewerBadgeHistoryRepository;
import com.cafestory.repository.ReviewerRankingSnapshotRepository;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.service.serviceInterface.ReviewerBadgeThresholdService;
import com.cafestory.service.serviceInterface.ReviewerFormulaService;
import com.cafestory.service.serviceInterface.ReviewerRankingSnapshotService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final ReviewerBadgeHistoryRepository reviewerBadgeHistoryRepository;

    public ReviewerRankingSnapshotServiceImpl(
            ReviewerRankingSnapshotRepository snapshotRepository,
            ReviewerRepository reviewerRepository,
            BlogLikeRepository blogLikeRepository,
            BlogShareRepository blogShareRepository,
            CommentRepository commentRepository,
            ReviewerFormulaService formulaService,
            ReviewerBadgeThresholdService badgeThresholdService,
            ReviewerBadgeHistoryRepository reviewerBadgeHistoryRepository) {
        this.snapshotRepository = snapshotRepository;
        this.reviewerRepository = reviewerRepository;
        this.blogLikeRepository = blogLikeRepository;
        this.blogShareRepository = blogShareRepository;
        this.commentRepository = commentRepository;
        this.formulaService = formulaService;
        this.badgeThresholdService = badgeThresholdService;
        this.reviewerBadgeHistoryRepository = reviewerBadgeHistoryRepository;
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

        List<Reviewer> allReviewers = reviewerRepository.findAllWithUser();
        Map<UUID, Reviewer> reviewersByUserId = new HashMap<>();
        Map<UUID, long[]> counts = new HashMap<>();
        for (Reviewer reviewer : allReviewers) {
            reviewersByUserId.put(reviewer.getUser().getUserId(), reviewer);
            counts.put(reviewer.getReviewerId(), new long[]{0, 0, 0});
        }

        // Score/ranking/badge đo engagement blog của reviewer NHẬN được, không
        // phải engagement reviewer đi thả cho người khác.
        for (AuthorInteractionCountRow row
                : blogLikeRepository.countByBlogAuthorBetween(range.startDate(), range.endDate())) {
            Reviewer reviewer = reviewersByUserId.get(row.getAuthorUserId());
            if (reviewer != null) {
                counts.get(reviewer.getReviewerId())[0] = row.getEventCount();
            }
        }
        for (AuthorInteractionCountRow row
                : blogShareRepository.countByBlogAuthorBetween(range.startDate(), range.endDate())) {
            Reviewer reviewer = reviewersByUserId.get(row.getAuthorUserId());
            if (reviewer != null) {
                counts.get(reviewer.getReviewerId())[1] = row.getEventCount();
            }
        }
        for (AuthorInteractionCountRow row
                : commentRepository.countByBlogAuthorBetween(range.startDate(), range.endDate())) {
            Reviewer reviewer = reviewersByUserId.get(row.getAuthorUserId());
            if (reviewer != null) {
                counts.get(reviewer.getReviewerId())[2] = row.getEventCount();
            }
        }

        Map<UUID, ReviewerRankingSnapshot> existingByReviewerId = new HashMap<>();
        for (ReviewerRankingSnapshot existing : snapshotRepository.findByPeriodAndPeriodTypeOrderByRankPositionAsc(period, periodType)) {
            existingByReviewerId.put(existing.getReviewer().getReviewerId(), existing);
        }

        List<SnapshotEntry> entries = new ArrayList<>();
        for (Reviewer reviewer : allReviewers) {
            long[] c = counts.get(reviewer.getReviewerId());
            long score = c[0] * formula.getLikeWeight()
                    + c[1] * formula.getShareWeight()
                    + c[2] * formula.getCommentWeight();
            entries.add(new SnapshotEntry(reviewer, c[0], c[1], c[2], score));
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
            snapshot.setFormula(formula);
            toSave.add(snapshot);
        }
        snapshotRepository.saveAll(toSave);

        if (periodType == RankingPeriodType.MONTHLY) {
            upsertReviewerBadges(period, entries);
        }
    }

    private void upsertReviewerBadges(String month, List<SnapshotEntry> entries) {
        Map<UUID, ReviewerBadgeHistory> existingByReviewerId = new HashMap<>();
        for (ReviewerBadgeHistory existing : reviewerBadgeHistoryRepository.findByMonth(month)) {
            existingByReviewerId.put(existing.getReviewer().getReviewerId(), existing);
        }
        List<ReviewerBadgeHistory> toSave = new ArrayList<>();
        for (SnapshotEntry entry : entries) {
            ReviewerBadgeHistory history = existingByReviewerId
                    .getOrDefault(entry.reviewer().getReviewerId(), new ReviewerBadgeHistory());
            history.setReviewer(entry.reviewer());
            history.setMonth(month);
            history.setLikeCount(entry.likeCount());
            history.setShareCount(entry.shareCount());
            history.setCommentCount(entry.commentCount());
            history.setScore(entry.score());
            history.setBadge(badgeThresholdService.badgeForScore(entry.score()));
            toSave.add(history);
        }
        reviewerBadgeHistoryRepository.saveAll(toSave);
    }

    @Override
    @Transactional
    public void initSnapshotForNewReviewer(Reviewer reviewer) {
        LocalDate today = LocalDate.now();
        ReviewerFormula formula = formulaService.getActiveFormula();

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
                snapshot.setFormula(formula);
                snapshotRepository.save(snapshot);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewerRankingSnapshotResponseDTO> getRanking(String period, RankingPeriodType periodType, int page, int size) {
        int sanitizedPage = Math.max(page, 0);
        int sanitizedSize = Math.max(1, Math.min(size, 100));
        Pageable pageable = PageRequest.of(sanitizedPage, sanitizedSize, Sort.by(Sort.Direction.ASC, "rankPosition"));
        Page<ReviewerRankingSnapshot> pageResult = snapshotRepository
                .findByPeriodAndPeriodType(period, periodType, pageable);
        List<ReviewerRankingSnapshot> pageItems = pageResult.getContent();

        Map<UUID, ReviewerBadgeHistory> badgeByReviewerId = Map.of();
        if (periodType == RankingPeriodType.MONTHLY && !pageItems.isEmpty()) {
            List<UUID> reviewerIds = pageItems.stream()
                    .map(s -> s.getReviewer().getReviewerId())
                    .toList();
            badgeByReviewerId = reviewerBadgeHistoryRepository
                    .findByMonthAndReviewerReviewerIdIn(period, reviewerIds)
                    .stream()
                    .collect(java.util.stream.Collectors.toMap(
                            h -> h.getReviewer().getReviewerId(),
                            h -> h));
        }

        Map<UUID, ReviewerBadgeHistory> badges = badgeByReviewerId;
        return pageResult.map(snapshot -> {
            ReviewerRankingSnapshotResponseDTO dto = toResponseDTO(snapshot);
            ReviewerBadgeHistory history = badges.get(snapshot.getReviewer().getReviewerId());
            if (history != null) {
                dto.setBadge(history.getBadge());
            }
            return dto;
        });
    }

    private String resolvePeriod(LocalDate date, RankingPeriodType periodType) {
        return switch (periodType) {
            case DAILY -> date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            // WEEK_BASED_YEAR, không phải getYear(): tuần ISO bắc qua giao thừa
            // thuộc về năm của tuần, nên 2027-01-01 phải ra 2026-W53.
            case WEEKLY -> date.get(IsoFields.WEEK_BASED_YEAR)
                    + "-W" + String.format("%02d", date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
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
        dto.setFormulaId(snapshot.getFormula().getId());
        return dto;
    }

    private record DateRange(LocalDateTime startDate, LocalDateTime endDate) {
    }

    private record SnapshotEntry(Reviewer reviewer, long likeCount, long shareCount, long commentCount, long score) {
    }
}
