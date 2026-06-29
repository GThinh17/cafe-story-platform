package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.ReviewerBadgeResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerDiscoveryResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerGeoAnalyticsResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerPayoutResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerRankingResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerSegmentResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerStatsResponseDTO;
import com.cafestory.dto.responseDTO.RegionResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogLike;
import com.cafestory.entity.BlogSave;
import com.cafestory.entity.BlogShare;
import com.cafestory.entity.Comment;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.ReviewerBadgeHistory;
import com.cafestory.entity.ReviewerPayout;
import com.cafestory.entity.Region;
import com.cafestory.entity.ReviewerFormula;
import com.cafestory.entity.Role;
import com.cafestory.entity.User;
import com.cafestory.entity.UserRoleAssignment;
import com.cafestory.entity.enums.ReviewerBadge;
import com.cafestory.entity.enums.PayoutStatus;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.repository.BlogLikeRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.BlogSaveRepository;
import com.cafestory.repository.BlogShareRepository;
import com.cafestory.repository.CommentRepository;
import com.cafestory.repository.ReviewerBadgeHistoryRepository;
import com.cafestory.repository.ReviewerPayoutRepository;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.repository.RoleRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.repository.UserFollowRepository;
import com.cafestory.repository.UserRoleAssignmentRepository;
import com.cafestory.service.serviceInterface.ReviewerBadgeThresholdService;
import com.cafestory.service.serviceInterface.ReviewerRankingSnapshotService;
import com.cafestory.service.serviceInterface.ReviewerFormulaService;
import com.cafestory.service.serviceInterface.ReviewerService;
import com.cafestory.validation.UserValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ReviewerServiceImpl implements ReviewerService {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String REVIEWER_ROLE = "REVIEWER";

    private final BlogLikeRepository blogLikeRepository;
    private final BlogRepository blogRepository;
    private final BlogSaveRepository blogSaveRepository;
    private final BlogShareRepository blogShareRepository;
    private final CommentRepository commentRepository;
    private final ReviewerPayoutRepository reviewerPayoutRepository;
    private final ReviewerBadgeHistoryRepository reviewerBadgeHistoryRepository;
    private final ReviewerRepository reviewerRepository;
    private final RoleRepository roleRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;
    private final UserValidator userValidator;
    private final ReviewerFormulaService formulaService;
    private final ReviewerBadgeThresholdService badgeThresholdService;
    private final ReviewerRankingSnapshotService snapshotService;

    public ReviewerServiceImpl(
            BlogLikeRepository blogLikeRepository,
            BlogRepository blogRepository,
            BlogSaveRepository blogSaveRepository,
            BlogShareRepository blogShareRepository,
            CommentRepository commentRepository,
            ReviewerPayoutRepository reviewerPayoutRepository,
            ReviewerBadgeHistoryRepository reviewerBadgeHistoryRepository,
            ReviewerRepository reviewerRepository,
            RoleRepository roleRepository,
            UserRoleAssignmentRepository userRoleAssignmentRepository,
            UserRepository userRepository,
            UserFollowRepository userFollowRepository,
            UserValidator userValidator,
            ReviewerFormulaService formulaService,
            ReviewerBadgeThresholdService badgeThresholdService,
            ReviewerRankingSnapshotService snapshotService) {
        this.blogLikeRepository = blogLikeRepository;
        this.blogRepository = blogRepository;
        this.blogSaveRepository = blogSaveRepository;
        this.blogShareRepository = blogShareRepository;
        this.commentRepository = commentRepository;
        this.reviewerPayoutRepository = reviewerPayoutRepository;
        this.reviewerBadgeHistoryRepository = reviewerBadgeHistoryRepository;
        this.reviewerRepository = reviewerRepository;
        this.roleRepository = roleRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.userRepository = userRepository;
        this.userFollowRepository = userFollowRepository;
        this.userValidator = userValidator;
        this.formulaService = formulaService;
        this.badgeThresholdService = badgeThresholdService;
        this.snapshotService = snapshotService;
    }

    @Override
    @Transactional
    public ReviewerResponseDTO createReviewer(UUID userId) {
        User user = userValidator.validateUserExists(userId);
        assignRole(user, REVIEWER_ROLE);
        Reviewer reviewer = reviewerRepository.findByUserUserId(userId).orElseGet(Reviewer::new);
        reviewer.setUser(user);
        Reviewer saved = reviewerRepository.save(reviewer);
        snapshotService.initSnapshotForNewReviewer(saved);
        return toReviewerResponse(saved, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewerResponseDTO getReviewer(UUID userId) {
        Reviewer reviewer = reviewerRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reviewer not found"));
        return toReviewerResponse(reviewer, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewerResponseDTO> getAllReviewer(UUID viewerUserId) {
        return reviewerRepository.findAll()
                .stream()
                .map(r -> toReviewerResponse(r, viewerUserId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewerResponseDTO> getAllActiveReviewers(UUID viewerUserId) {
        return reviewerRepository.findAllActiveReviewers()
                .stream()
                .map(r -> toReviewerResponse(r, viewerUserId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewerResponseDTO> searchReviewers(String query, UUID viewerUserId) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return reviewerRepository.searchActiveReviewers(query.trim())
                .stream()
                .map(r -> toReviewerResponse(r, viewerUserId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewerStatsResponseDTO countReviewerStats(UUID requesterId, UUID reviewerId, String period) {
        validateSelfOrAdmin(requesterId, reviewerId);
        DateRange range = dateRangeForPeriod(period);
        ReviewerStatsResponseDTO stats = countReviewerStatsByDateRange(reviewerId, range.startDate(), range.endDate());
        stats.setPeriod(period.toLowerCase());
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewerStatsResponseDTO countReviewerStatsByDateRange(UUID reviewerId, LocalDateTime startDate, LocalDateTime endDate) {
        Reviewer reviewer = validateReviewerExists(reviewerId);
        UUID userId = reviewer.getUser().getUserId();
        long likeCount = blogLikeRepository.countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(userId, startDate, endDate);
        long shareCount = blogShareRepository.countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(userId, startDate, endDate);
        long commentCount = commentRepository.countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(userId, startDate, endDate);
        long score = calculateScore(likeCount, shareCount, commentCount);
        return new ReviewerStatsResponseDTO(reviewerId, "custom", likeCount, shareCount, commentCount, score);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewerRankingResponseDTO> getReviewerRanking(
            String period,
            int page,
            int limit,
            String city,
            String province,
            String area) {
        DateRange range = dateRangeForPeriod(period);
        Map<UUID, EngagementAccumulator> engagement = aggregateEngagementForAllUsers(range.startDate(), range.endDate());
        List<ReviewerRankingResponseDTO> rankings = engagement.values()
                .stream()
                .filter(accumulator -> matchesLocation(accumulator.reviewer(), city, province, area))
                .map(this::toRankingResponse)
                .sorted(rankingComparator())
                .toList();
        List<ReviewerRankingResponseDTO> ranked = new ArrayList<>();
        for (int i = 0; i < rankings.size(); i++) {
            ReviewerRankingResponseDTO response = rankings.get(i);
            response.setRank(i + 1);
            ranked.add(response);
        }
        int sanitizedPage = Math.max(page, 1);
        int sanitizedLimit = Math.max(1, Math.min(limit, 100));
        int fromIndex = Math.min((sanitizedPage - 1) * sanitizedLimit, ranked.size());
        int toIndex = Math.min(fromIndex + sanitizedLimit, ranked.size());
        return ranked.subList(fromIndex, toIndex);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewerDiscoveryResponseDTO> getReviewersInRegion(
            UUID viewerUserId,
            String city,
            String province,
            String ward,
            String area,
            String street,
            int page,
            int size) {
        DateRange recentRange = new DateRange(LocalDate.now().minusDays(30).atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay());
        RegionFilter filter = new RegionFilter(city, province, ward, area, street);
        List<ReviewerScoreCard> cards = buildReviewerScoreCards(recentRange)
                .stream()
                .filter(card -> card.reviewCount() > 0)
                .filter(card -> card.regionScore(filter) > 0)
                .sorted(Comparator.comparingDouble((ReviewerScoreCard card) -> regionRankingScore(card, filter)).reversed()
                        .thenComparing(Comparator.comparingDouble((ReviewerScoreCard card) -> card.regionScore(filter)).reversed())
                        .thenComparing(Comparator.comparingInt((ReviewerScoreCard card) -> badgeLevel(card.badge())).reversed())
                        .thenComparing(Comparator.comparingLong(ReviewerScoreCard::followerCount).reversed())
                        .thenComparing(Comparator.comparingLong(ReviewerScoreCard::reviewCount).reversed())
                        .thenComparing(card -> card.reviewer().getReviewerId().toString()))
                .toList();
        return page(cards, page, size, (rank, card) -> toDiscoveryResponse(card, viewerUserId, rank, regionRankingScore(card, filter)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewerDiscoveryResponseDTO> getTrendingReviewers(UUID viewerUserId, String window, int page, int size) {
        DateRange range = dateRangeForTrendingWindow(window);
        List<ReviewerScoreCard> cards = buildReviewerScoreCards(range)
                .stream()
                .filter(card -> card.reviewCount() > 0)
                .sorted(Comparator.comparingDouble(this::trendingRankingScore).reversed()
                        .thenComparing(Comparator.comparingLong(ReviewerScoreCard::recentReviewCount).reversed())
                        .thenComparing(Comparator.comparingLong(ReviewerScoreCard::recentLikeCount).reversed())
                        .thenComparing(card -> card.reviewer().getReviewerId().toString()))
                .toList();
        return page(cards, page, size, (rank, card) -> toDiscoveryResponse(card, viewerUserId, rank, trendingRankingScore(card)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewerDiscoveryResponseDTO> getTopReviewers(UUID viewerUserId, int page, int size) {
        DateRange recentRange = new DateRange(LocalDate.now().minusDays(30).atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay());
        List<ReviewerScoreCard> cards = buildReviewerScoreCards(recentRange)
                .stream()
                .filter(card -> card.reviewCount() > 0)
                .sorted(Comparator.comparingDouble(this::topRankingScore).reversed()
                        .thenComparing(Comparator.comparingInt((ReviewerScoreCard card) -> badgeLevel(card.badge())).reversed())
                        .thenComparing(Comparator.comparingLong(ReviewerScoreCard::followerCount).reversed())
                        .thenComparing(Comparator.comparingLong(ReviewerScoreCard::reviewCount).reversed())
                        .thenComparing(card -> card.reviewer().getReviewerId().toString()))
                .toList();
        return page(cards, page, size, (rank, card) -> toDiscoveryResponse(card, viewerUserId, rank, topRankingScore(card)));
    }

    @Override
    public long calculateReviewerScore(ReviewerStatsResponseDTO stats) {
        return calculateScore(stats.getLikeCount(), stats.getShareCount(), stats.getCommentCount());
    }

    @Override
    public long calculateReviewerPayout(ReviewerStatsResponseDTO stats) {
        return formulaService.calculatePayout(stats.getLikeCount(), stats.getShareCount(), stats.getCommentCount());
    }

    @Override
    public String calculateReviewerBadge(long score) {
        return badgeThresholdService.badgeForScore(score).name();
    }

    @Override
    public String calculateReviewerSegment(long score) {
        if (score == 0) {
            return "inactive";
        }
        if (score < 100) {
            return "new";
        }
        if (score < 300) {
            return "active";
        }
        if (score < 700) {
            return "strong";
        }
        if (score < 1500) {
            return "top";
        }
        return "elite";
    }

    @Override
    @Transactional
    public List<ReviewerPayoutResponseDTO> generateMonthlyPayouts(UUID requesterId, String month, boolean overwrite) {
        validateAdmin(requesterId);
        YearMonth yearMonth = parseMonth(month);
        DateRange range = dateRangeForMonth(yearMonth);
        Map<UUID, EngagementAccumulator> engagement = aggregateEngagementForAllUsers(range.startDate(), range.endDate());
        ReviewerFormula formula = formulaService.getActiveFormula();
        List<ReviewerPayoutResponseDTO> responses = new ArrayList<>();
        for (EngagementAccumulator accumulator : engagement.values()) {
            if (reviewerPayoutRepository.existsByReviewerReviewerIdAndPayoutMonth(accumulator.reviewer().getReviewerId(), month) && !overwrite) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate payout generation");
            }
            ReviewerPayout payout = reviewerPayoutRepository
                    .findByReviewerReviewerIdAndPayoutMonth(accumulator.reviewer().getReviewerId(), month)
                    .orElseGet(ReviewerPayout::new);
            payout.setReviewer(accumulator.reviewer());
            payout.setPayoutMonth(month);
            payout.setLikeCount(accumulator.likeCount());
            payout.setShareCount(accumulator.shareCount());
            payout.setCommentCount(accumulator.commentCount());
            payout.setLikeAmount(accumulator.likeCount() * formula.getLikePayoutAmount());
            payout.setShareAmount(accumulator.shareCount() * formula.getSharePayoutAmount());
            payout.setCommentAmount(accumulator.commentCount() * formula.getCommentPayoutAmount());
            payout.setTotalAmount(payout.getLikeAmount() + payout.getShareAmount() + payout.getCommentAmount());
            payout.setPayoutStatus(PayoutStatus.CALCULATED);
            responses.add(toPayoutResponse(reviewerPayoutRepository.save(payout)));
        }
        return responses;
    }

    @Override
    @Transactional
    public List<ReviewerBadgeResponseDTO> generateMonthlyBadges(UUID requesterId, String month, boolean overwrite) {
        validateAdmin(requesterId);
        YearMonth yearMonth = parseMonth(month);
        DateRange range = dateRangeForMonth(yearMonth);
        Map<UUID, EngagementAccumulator> engagement = aggregateEngagementForAllUsers(range.startDate(), range.endDate());
        List<ReviewerBadgeResponseDTO> responses = new ArrayList<>();
        for (EngagementAccumulator accumulator : engagement.values()) {
            if (reviewerBadgeHistoryRepository.existsByReviewerReviewerIdAndMonth(accumulator.reviewer().getReviewerId(), month) && !overwrite) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate badge generation");
            }
            ReviewerBadgeHistory badgeHistory = reviewerBadgeHistoryRepository
                    .findByReviewerReviewerIdAndMonth(accumulator.reviewer().getReviewerId(), month)
                    .orElseGet(ReviewerBadgeHistory::new);
            badgeHistory.setReviewer(accumulator.reviewer());
            badgeHistory.setMonth(month);
            badgeHistory.setLikeCount(accumulator.likeCount());
            badgeHistory.setShareCount(accumulator.shareCount());
            badgeHistory.setCommentCount(accumulator.commentCount());
            badgeHistory.setScore(accumulator.score());
            badgeHistory.setBadge(badgeThresholdService.badgeForScore(accumulator.score()));
            responses.add(toBadgeResponse(reviewerBadgeHistoryRepository.save(badgeHistory)));
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewerPayoutResponseDTO> getReviewerPayoutHistory(UUID requesterId, UUID reviewerId) {
        validateSelfOrAdmin(requesterId, reviewerId);
        return reviewerPayoutRepository.findByReviewerReviewerIdOrderByPayoutMonthDesc(reviewerId)
                .stream()
                .map(this::toPayoutResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewerBadgeResponseDTO> getReviewerBadgeHistory(UUID requesterId, UUID reviewerId) {
        validateSelfOrAdmin(requesterId, reviewerId);
        return reviewerBadgeHistoryRepository.findByReviewerReviewerIdOrderByMonthDesc(reviewerId)
                .stream()
                .map(this::toBadgeResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewerSegmentResponseDTO> getReviewersBySegment(String month, String segment) {
        YearMonth yearMonth = parseMonth(month);
        validateSegment(segment);
        DateRange range = dateRangeForMonth(yearMonth);
        return aggregateEngagementForAllUsers(range.startDate(), range.endDate())
                .values()
                .stream()
                .map(this::toSegmentResponse)
                .filter(response -> response.getSegment().equals(segment.toLowerCase()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewerGeoAnalyticsResponseDTO> getGeoAnalytics(String period, String groupBy) {
        validateGroupBy(groupBy);
        DateRange range = dateRangeForPeriod(period);
        Map<String, List<EngagementAccumulator>> grouped = new LinkedHashMap<>();
        aggregateEngagementForAllUsers(range.startDate(), range.endDate())
                .values()
                .forEach(accumulator -> grouped.computeIfAbsent(locationValue(accumulator.reviewer(), groupBy), key -> new ArrayList<>()).add(accumulator));
        return grouped.entrySet()
                .stream()
                .map(entry -> toGeoResponse(entry.getKey(), groupBy, entry.getValue()))
                .toList();
    }

    private DateRange dateRangeForPeriod(String period) {
        if (period == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid period");
        }
        LocalDate today = LocalDate.now();
        return switch (period.toLowerCase()) {
            case "day" -> new DateRange(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
            case "week" -> {
                LocalDate start = today.with(DayOfWeek.MONDAY);
                yield new DateRange(start.atStartOfDay(), start.plusWeeks(1).atStartOfDay());
            }
            case "month" -> dateRangeForMonth(YearMonth.from(today));
            case "3months" -> new DateRange(today.minusMonths(3).atStartOfDay(), today.plusDays(1).atStartOfDay());
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid period");
        };
    }

    private DateRange dateRangeForMonth(YearMonth month) {
        return new DateRange(month.atDay(1).atStartOfDay(), month.plusMonths(1).atDay(1).atStartOfDay());
    }

    private YearMonth parseMonth(String month) {
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid month");
        }
    }

    private Map<UUID, EngagementAccumulator> aggregateEngagementForAllUsers(LocalDateTime startDate, LocalDateTime endDate) {
        ReviewerFormula formula = formulaService.getActiveFormula();
        int likeWeight = formula.getLikeWeight();
        int shareWeight = formula.getShareWeight();
        int commentWeight = formula.getCommentWeight();
        Map<UUID, Reviewer> reviewersByUserId = new HashMap<>();
        Map<UUID, EngagementAccumulator> engagement = new HashMap<>();
        for (Reviewer reviewer : reviewerRepository.findAll()) {
            reviewersByUserId.put(reviewer.getUser().getUserId(), reviewer);
            engagement.putIfAbsent(reviewer.getReviewerId(), new EngagementAccumulator(reviewer, likeWeight, shareWeight, commentWeight));
        }
        aggregateEngagement(startDate, endDate, reviewersByUserId, engagement, likeWeight, shareWeight, commentWeight);
        return engagement;
    }

    private void aggregateEngagement(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Map<UUID, Reviewer> reviewersByUserId,
            Map<UUID, EngagementAccumulator> engagement,
            int likeWeight,
            int shareWeight,
            int commentWeight) {
        for (BlogLike like : blogLikeRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(startDate, endDate)) {
            accumulator(engagement, reviewersByUserId.get(like.getUser().getUserId()), likeWeight, shareWeight, commentWeight).incrementLikes();
        }
        for (BlogShare share : blogShareRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(startDate, endDate)) {
            accumulator(engagement, reviewersByUserId.get(share.getUser().getUserId()), likeWeight, shareWeight, commentWeight).incrementShares();
        }
        for (Comment comment : commentRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(startDate, endDate)) {
            accumulator(engagement, reviewersByUserId.get(comment.getUser().getUserId()), likeWeight, shareWeight, commentWeight).incrementComments();
        }
    }

    private EngagementAccumulator accumulator(Map<UUID, EngagementAccumulator> engagement, Reviewer reviewer, int likeWeight, int shareWeight, int commentWeight) {
        if (reviewer == null) {
            return new EngagementAccumulator(null, likeWeight, shareWeight, commentWeight);
        }
        return engagement.computeIfAbsent(reviewer.getReviewerId(), ignored -> new EngagementAccumulator(reviewer, likeWeight, shareWeight, commentWeight));
    }

    private ReviewerRankingResponseDTO toRankingResponse(EngagementAccumulator accumulator) {
        ReviewerRankingResponseDTO response = new ReviewerRankingResponseDTO();
        response.setReviewerId(accumulator.reviewer().getReviewerId());
        response.setLikeCount(accumulator.likeCount());
        response.setShareCount(accumulator.shareCount());
        response.setCommentCount(accumulator.commentCount());
        response.setScore(accumulator.score());
        Region region = accumulator.reviewer().getUser().getRegion();
        response.setLocation(region == null ? "unknown" : firstNonBlank(region.getCity(), region.getProvince(), "unknown"));
        return response;
    }

    private ReviewerSegmentResponseDTO toSegmentResponse(EngagementAccumulator accumulator) {
        ReviewerSegmentResponseDTO response = new ReviewerSegmentResponseDTO();
        response.setReviewerId(accumulator.reviewer().getReviewerId());
        response.setLikeCount(accumulator.likeCount());
        response.setShareCount(accumulator.shareCount());
        response.setCommentCount(accumulator.commentCount());
        response.setScore(accumulator.score());
        response.setSegment(calculateReviewerSegment(accumulator.score()));
        return response;
    }

    private ReviewerGeoAnalyticsResponseDTO toGeoResponse(String locationName, String groupBy, List<EngagementAccumulator> accumulators) {
        long totalLikes = accumulators.stream().mapToLong(EngagementAccumulator::likeCount).sum();
        long totalShares = accumulators.stream().mapToLong(EngagementAccumulator::shareCount).sum();
        long totalComments = accumulators.stream().mapToLong(EngagementAccumulator::commentCount).sum();
        long totalScore = accumulators.stream().mapToLong(EngagementAccumulator::score).sum();
        EngagementAccumulator top = accumulators.stream().max(accumulatorComparator()).orElse(null);
        ReviewerGeoAnalyticsResponseDTO response = new ReviewerGeoAnalyticsResponseDTO();
        response.setLocationName(locationName);
        response.setGroupBy(groupBy.toLowerCase());
        response.setReviewerCount(accumulators.size());
        response.setTotalLikes(totalLikes);
        response.setTotalShares(totalShares);
        response.setTotalComments(totalComments);
        response.setTotalScore(totalScore);
        response.setAverageScore(accumulators.isEmpty() ? 0 : (double) totalScore / accumulators.size());
        response.setTopReviewer(top == null ? null : top.reviewer().getReviewerId());
        return response;
    }

    private ReviewerPayoutResponseDTO toPayoutResponse(ReviewerPayout payout) {
        ReviewerPayoutResponseDTO response = new ReviewerPayoutResponseDTO();
        response.setId(payout.getId());
        response.setReviewerId(payout.getReviewer().getReviewerId());
        response.setPayoutMonth(payout.getPayoutMonth());
        response.setLikeCount(payout.getLikeCount());
        response.setShareCount(payout.getShareCount());
        response.setCommentCount(payout.getCommentCount());
        response.setLikeAmount(payout.getLikeAmount());
        response.setShareAmount(payout.getShareAmount());
        response.setCommentAmount(payout.getCommentAmount());
        response.setTotalAmount(payout.getTotalAmount());
        response.setPayoutStatus(payout.getPayoutStatus());
        return response;
    }

    private ReviewerBadgeResponseDTO toBadgeResponse(ReviewerBadgeHistory badgeHistory) {
        ReviewerBadgeResponseDTO response = new ReviewerBadgeResponseDTO();
        response.setId(badgeHistory.getId());
        response.setReviewerId(badgeHistory.getReviewer().getReviewerId());
        response.setMonth(badgeHistory.getMonth());
        response.setScore(badgeHistory.getScore());
        response.setBadge(badgeHistory.getBadge());
        response.setLikeCount(badgeHistory.getLikeCount());
        response.setShareCount(badgeHistory.getShareCount());
        response.setCommentCount(badgeHistory.getCommentCount());
        return response;
    }

    private ReviewerResponseDTO toReviewerResponse(Reviewer reviewer, UUID viewerUserId) {
        ReviewerResponseDTO response = new ReviewerResponseDTO();
        User user = reviewer.getUser();
        response.setReviewerId(reviewer.getReviewerId());
        response.setUserId(user.getUserId());
        response.setRole(REVIEWER_ROLE);
        response.setAvatar(user.getUserAvatar());
        response.setRegion(toRegionResponse(user.getRegion()));
        response.setName(firstNonBlank(user.getUserFullName(), user.getUserName(), null));
        response.setFollower(defaultInt(user.getUserFollower()));
        response.setFollow(userFollowRepository.findByFollowerUserId(user.getUserId()).size());
        response.setLike(defaultInt(user.getUserLike()));
        ReviewerBadgeHistory latestBadge = reviewerBadgeHistoryRepository
                .findTopByReviewerReviewerIdOrderByMonthDesc(reviewer.getReviewerId())
                .orElse(null);
        response.setBadge(latestBadge == null ? ReviewerBadge.IRON : latestBadge.getBadge());
        response.setScore(latestBadge == null ? 0 : latestBadge.getScore());
        response.setExpireDate(reviewer.getReviewerExpiresAt());
        response.setIsFollowing(viewerUserId != null && user.getUserId() != null
                && userFollowRepository.existsByFollowerUserIdAndFollowingUserId(viewerUserId, user.getUserId()));
        return response;
    }

    private RegionResponseDTO toRegionResponse(Region region) {
        if (region == null) {
            return null;
        }
        RegionResponseDTO response = new RegionResponseDTO();
        response.setRegionId(region.getRegionId());
        response.setCity(region.getCity());
        response.setProvince(region.getProvince());
        response.setWard(region.getWard());
        response.setArea(region.getArea());
        response.setStreet(region.getStreet());
        return response;
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private void validateSelfOrAdmin(UUID requesterId, UUID reviewerId) {
        User requester = userValidator.validateUserExists(requesterId);
        Reviewer reviewer = validateReviewerExists(reviewerId);
        if (!requester.getUserId().equals(reviewer.getUser().getUserId()) && !hasRole(requester.getUserId(), ADMIN_ROLE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized access");
        }
    }

    private Reviewer validateReviewerExists(UUID reviewerId) {
        return reviewerRepository.findById(reviewerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reviewer not found"));
    }

    private void validateAdmin(UUID requesterId) {
        User requester = userValidator.validateUserExists(requesterId);
        if (!hasRole(requester.getUserId(), ADMIN_ROLE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized access");
        }
    }

    private void assignRole(User user, String roleName) {
        Role role = roleRepository.findByName(roleName).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName(roleName);
            return roleRepository.save(newRole);
        });
        if (userRoleAssignmentRepository.existsByUserUserIdAndRoleName(user.getUserId(), roleName)) {
            return;
        }
        UserRoleAssignment assignment = new UserRoleAssignment();
        assignment.setUser(user);
        assignment.setRole(role);
        userRoleAssignmentRepository.save(assignment);
    }

    private boolean hasRole(UUID userId, String roleName) {
        return userRoleAssignmentRepository.existsByUserUserIdAndRoleName(userId, roleName);
    }

    private void validateSegment(String segment) {
        if (!List.of("inactive", "new", "active", "strong", "top", "elite").contains(segment == null ? "" : segment.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid segment");
        }
    }

    private void validateGroupBy(String groupBy) {
        if (!List.of("city", "province", "area").contains(groupBy == null ? "" : groupBy.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid groupBy");
        }
    }

    private boolean matchesLocation(Reviewer reviewer, String city, String province, String area) {
        if (reviewer == null || reviewer.getUser() == null) {
            return false;
        }
        Region region = reviewer.getUser().getRegion();
        return matches(city, region == null ? null : region.getCity())
                && matches(province, region == null ? null : region.getProvince())
                && matches(area, region == null ? null : region.getArea());
    }

    private boolean matches(String expected, String actual) {
        return expected == null || expected.isBlank() || expected.equalsIgnoreCase(nullToUnknown(actual));
    }

    private String locationValue(Reviewer reviewer, String groupBy) {
        Region region = reviewer.getUser().getRegion();
        return switch (groupBy.toLowerCase()) {
            case "city" -> nullToUnknown(region == null ? null : region.getCity());
            case "province" -> nullToUnknown(region == null ? null : region.getProvince());
            case "area" -> nullToUnknown(region == null ? null : region.getArea());
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid groupBy");
        };
    }

    private String nullToUnknown(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }

    private String firstNonBlank(String first, String second, String fallback) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return fallback;
    }

    private long calculateScore(long likeCount, long shareCount, long commentCount) {
        return formulaService.calculateScore(likeCount, shareCount, commentCount);
    }

    private List<ReviewerScoreCard> buildReviewerScoreCards(DateRange recentRange) {
        Map<UUID, ReviewerScoreAccumulator> accumulatorsByUserId = new HashMap<>();
        for (Reviewer reviewer : reviewerRepository.findAll()) {
            if (reviewer.getUser() == null || !Boolean.TRUE.equals(reviewer.getUser().getAccountStatus())) {
                continue;
            }
            ReviewerBadgeHistory latestBadge = reviewerBadgeHistoryRepository
                    .findTopByReviewerReviewerIdOrderByMonthDesc(reviewer.getReviewerId())
                    .orElse(null);
            accumulatorsByUserId.put(
                    reviewer.getUser().getUserId(),
                    new ReviewerScoreAccumulator(reviewer, latestBadge == null ? ReviewerBadge.IRON : latestBadge.getBadge()));
        }

        Map<UUID, ReviewerScoreAccumulator> accumulatorsByBlogId = new HashMap<>();
        List<Blog> publishedBlogs = blogRepository.findAll()
                .stream()
                .filter(blog -> blog.getId() != null)
                .filter(blog -> blog.getAuthor() != null)
                .filter(blog -> blog.getStatus() == PostStatus.PUBLISHED)
                .filter(blog -> accumulatorsByUserId.containsKey(blog.getAuthor().getUserId()))
                .toList();
        for (Blog blog : publishedBlogs) {
            ReviewerScoreAccumulator accumulator = accumulatorsByUserId.get(blog.getAuthor().getUserId());
            accumulator.incrementReviews(blog.getCreatedAt(), recentRange);
            accumulator.addActiveMonth(blog.getCreatedAt());
            accumulatorsByBlogId.put(blog.getId(), accumulator);
        }

        aggregateLikes(accumulatorsByBlogId, recentRange);
        aggregateShares(accumulatorsByBlogId, recentRange);
        aggregateComments(accumulatorsByBlogId, recentRange);
        aggregateSaves(accumulatorsByBlogId, recentRange);

        return accumulatorsByUserId.values()
                .stream()
                .map(ReviewerScoreAccumulator::toScoreCard)
                .toList();
    }

    private void aggregateLikes(Map<UUID, ReviewerScoreAccumulator> accumulatorsByBlogId, DateRange recentRange) {
        for (BlogLike like : blogLikeRepository.findAll()) {
            ReviewerScoreAccumulator accumulator = accumulatorForInteraction(accumulatorsByBlogId, like.getBlog());
            if (accumulator != null) {
                accumulator.incrementLikes(like.getCreatedAt(), recentRange);
            }
        }
    }

    private void aggregateShares(Map<UUID, ReviewerScoreAccumulator> accumulatorsByBlogId, DateRange recentRange) {
        for (BlogShare share : blogShareRepository.findAll()) {
            ReviewerScoreAccumulator accumulator = accumulatorForInteraction(accumulatorsByBlogId, share.getBlog());
            if (accumulator != null) {
                accumulator.incrementShares(share.getCreatedAt(), recentRange);
            }
        }
    }

    private void aggregateComments(Map<UUID, ReviewerScoreAccumulator> accumulatorsByBlogId, DateRange recentRange) {
        for (Comment comment : commentRepository.findAll()) {
            ReviewerScoreAccumulator accumulator = accumulatorForInteraction(accumulatorsByBlogId, comment.getBlog());
            if (accumulator != null) {
                accumulator.incrementComments(comment.getCreatedAt(), recentRange);
            }
        }
    }

    private void aggregateSaves(Map<UUID, ReviewerScoreAccumulator> accumulatorsByBlogId, DateRange recentRange) {
        for (BlogSave save : blogSaveRepository.findAll()) {
            ReviewerScoreAccumulator accumulator = accumulatorForInteraction(accumulatorsByBlogId, save.getBlog());
            if (accumulator != null) {
                accumulator.incrementSaves(save.getCreatedAt(), recentRange);
            }
        }
    }

    private ReviewerScoreAccumulator accumulatorForInteraction(Map<UUID, ReviewerScoreAccumulator> accumulatorsByBlogId, Blog blog) {
        if (blog == null || blog.getId() == null) {
            return null;
        }
        return accumulatorsByBlogId.get(blog.getId());
    }

    private double regionRankingScore(ReviewerScoreCard card, RegionFilter filter) {
        double baseScore = card.regionScore(filter)
                + card.reviewCount() * 2.0
                + card.totalLikeCount() * 1.5
                + card.totalCommentCount() * 2.0
                + card.followerCount() * 3.0
                + card.recentReviewCount() * 4.0
                + badgeBonus(card.badge());
        return baseScore * badgeMultiplier(card.badge());
    }

    private double trendingRankingScore(ReviewerScoreCard card) {
        double recentScore = card.recentReviewCount() * 5.0
                + card.recentLikeCount() * 2.0
                + card.recentCommentCount() * 3.0
                + card.recentShareCount() * 4.0
                + card.recentSaveCount() * 4.0;
        double multiplier = Math.min(badgeMultiplier(card.badge()), 1.08);
        return recentScore * multiplier + badgeBonus(card.badge()) * 0.25;
    }

    private double topRankingScore(ReviewerScoreCard card) {
        double reputationScore = card.followerCount() * 4.0
                + card.reviewCount() * 2.0
                + card.totalLikeCount() * 2.0
                + card.totalCommentCount() * 1.5
                + card.totalShareCount() * 3.0
                + card.activeMonthCount() * 3.0;
        return (reputationScore + badgeBonus(card.badge())) * badgeMultiplier(card.badge());
    }

    private ReviewerDiscoveryResponseDTO toDiscoveryResponse(
            ReviewerScoreCard card,
            UUID viewerUserId,
            int rank,
            double rankingScore) {
        User user = card.reviewer().getUser();
        Region region = user.getRegion();
        ReviewerDiscoveryResponseDTO response = new ReviewerDiscoveryResponseDTO();
        response.setRank(rank);
        response.setReviewerId(card.reviewer().getReviewerId());
        response.setUserId(user.getUserId());
        response.setUserName(user.getUserName());
        response.setUserFullName(user.getUserFullName());
        response.setAvatar(user.getUserAvatar());
        response.setCity(region == null ? null : region.getCity());
        response.setProvince(region == null ? null : region.getProvince());
        response.setWard(region == null ? null : region.getWard());
        response.setArea(region == null ? null : region.getArea());
        response.setStreet(region == null ? null : region.getStreet());
        response.setReviewCount(card.reviewCount());
        response.setRecentReviewCount(card.recentReviewCount());
        response.setFollowerCount(card.followerCount());
        response.setTotalLikeCount(card.totalLikeCount());
        response.setTotalCommentCount(card.totalCommentCount());
        response.setTotalShareCount(card.totalShareCount());
        response.setTotalSaveCount(card.totalSaveCount());
        response.setRecentLikeCount(card.recentLikeCount());
        response.setRecentCommentCount(card.recentCommentCount());
        response.setRecentShareCount(card.recentShareCount());
        response.setRecentSaveCount(card.recentSaveCount());
        response.setBadge(card.badge());
        response.setBadgeLevel(badgeLevel(card.badge()));
        response.setBadgeScore(badgeBonus(card.badge()));
        response.setRankingScore(rankingScore);
        response.setMe(viewerUserId != null && viewerUserId.equals(user.getUserId()));
        response.setFollowing(viewerUserId != null
                && !viewerUserId.equals(user.getUserId())
                && userFollowRepository.existsByFollowerUserIdAndFollowingUserId(viewerUserId, user.getUserId()));
        return response;
    }

    private <T> List<ReviewerDiscoveryResponseDTO> page(
            List<T> items,
            int page,
            int size,
            RankedMapper<T> mapper) {
        int sanitizedPage = Math.max(page, 0);
        int sanitizedSize = Math.max(1, Math.min(size, 100));
        int fromIndex = Math.min(sanitizedPage * sanitizedSize, items.size());
        int toIndex = Math.min(fromIndex + sanitizedSize, items.size());
        List<ReviewerDiscoveryResponseDTO> responses = new ArrayList<>();
        for (int index = fromIndex; index < toIndex; index++) {
            responses.add(mapper.map(index + 1, items.get(index)));
        }
        return responses;
    }

    private DateRange dateRangeForTrendingWindow(String window) {
        LocalDate today = LocalDate.now();
        if (window == null || window.isBlank()) {
            return new DateRange(today.minusDays(7).atStartOfDay(), today.plusDays(1).atStartOfDay());
        }
        return switch (window.toUpperCase()) {
            case "DAY_7", "WEEK" -> new DateRange(today.minusDays(7).atStartOfDay(), today.plusDays(1).atStartOfDay());
            case "DAY_30", "MONTH" -> new DateRange(today.minusDays(30).atStartOfDay(), today.plusDays(1).atStartOfDay());
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reviewer trending window");
        };
    }

    private boolean inRange(LocalDateTime createdAt, DateRange range) {
        return createdAt != null
                && !createdAt.isBefore(range.startDate())
                && createdAt.isBefore(range.endDate());
    }

    private double badgeBonus(ReviewerBadge badge) {
        return switch (badge == null ? ReviewerBadge.IRON : badge) {
            case IRON -> 0.0;
            case BRONZE -> 10.0;
            case SILVER -> 25.0;
            case GOLD -> 45.0;
            case DIAMOND -> 70.0;
        };
    }

    private double badgeMultiplier(ReviewerBadge badge) {
        return switch (badge == null ? ReviewerBadge.IRON : badge) {
            case IRON -> 1.0;
            case BRONZE -> 1.03;
            case SILVER -> 1.06;
            case GOLD -> 1.10;
            case DIAMOND -> 1.15;
        };
    }

    private int badgeLevel(ReviewerBadge badge) {
        return switch (badge == null ? ReviewerBadge.IRON : badge) {
            case IRON -> 1;
            case BRONZE -> 2;
            case SILVER -> 3;
            case GOLD -> 4;
            case DIAMOND -> 5;
        };
    }

    private Comparator<ReviewerRankingResponseDTO> rankingComparator() {
        return Comparator.comparingLong(ReviewerRankingResponseDTO::getScore).reversed()
                .thenComparing(Comparator.comparingLong(ReviewerRankingResponseDTO::getCommentCount).reversed())
                .thenComparing(Comparator.comparingLong(ReviewerRankingResponseDTO::getShareCount).reversed())
                .thenComparing(Comparator.comparingLong(ReviewerRankingResponseDTO::getLikeCount).reversed())
                .thenComparing(response -> response.getReviewerId().toString());
    }

    private Comparator<EngagementAccumulator> accumulatorComparator() {
        return Comparator.comparingLong(EngagementAccumulator::score)
                .thenComparingLong(EngagementAccumulator::commentCount)
                .thenComparingLong(EngagementAccumulator::shareCount)
                .thenComparingLong(EngagementAccumulator::likeCount)
                .thenComparing(accumulator -> accumulator.reviewer().getReviewerId().toString(), Comparator.reverseOrder());
    }

    private record DateRange(LocalDateTime startDate, LocalDateTime endDate) {
    }

    private record RegionFilter(String city, String province, String ward, String area, String street) {
    }

    private interface RankedMapper<T> {
        ReviewerDiscoveryResponseDTO map(int rank, T value);
    }

    private record ReviewerScoreCard(
            Reviewer reviewer,
            ReviewerBadge badge,
            long reviewCount,
            long recentReviewCount,
            long followerCount,
            long totalLikeCount,
            long totalCommentCount,
            long totalShareCount,
            long totalSaveCount,
            long recentLikeCount,
            long recentCommentCount,
            long recentShareCount,
            long recentSaveCount,
            long activeMonthCount) {

        double regionScore(RegionFilter filter) {
            Region region = reviewer.getUser().getRegion();
            return matchScore(filter.city(), region == null ? null : region.getCity(), 50.0)
                    + matchScore(filter.province(), region == null ? null : region.getProvince(), 25.0)
                    + matchScore(filter.area(), region == null ? null : region.getArea(), 15.0)
                    + matchScore(filter.ward(), region == null ? null : region.getWard(), 10.0)
                    + matchScore(filter.street(), region == null ? null : region.getStreet(), 5.0);
        }

        private double matchScore(String expected, String actual, double score) {
            if (expected == null || expected.isBlank()) {
                return 0.0;
            }
            return actual != null && expected.equalsIgnoreCase(actual) ? score : 0.0;
        }
    }

    private class ReviewerScoreAccumulator {
        private final Reviewer reviewer;
        private final ReviewerBadge badge;
        private final Set<YearMonth> activeMonths = new HashSet<>();
        private long reviewCount;
        private long recentReviewCount;
        private long totalLikeCount;
        private long totalCommentCount;
        private long totalShareCount;
        private long totalSaveCount;
        private long recentLikeCount;
        private long recentCommentCount;
        private long recentShareCount;
        private long recentSaveCount;

        ReviewerScoreAccumulator(Reviewer reviewer, ReviewerBadge badge) {
            this.reviewer = reviewer;
            this.badge = badge;
        }

        void incrementReviews(LocalDateTime createdAt, DateRange recentRange) {
            reviewCount++;
            if (inRange(createdAt, recentRange)) {
                recentReviewCount++;
            }
        }

        void addActiveMonth(LocalDateTime createdAt) {
            if (createdAt != null) {
                activeMonths.add(YearMonth.from(createdAt));
            }
        }

        void incrementLikes(LocalDateTime createdAt, DateRange recentRange) {
            totalLikeCount++;
            if (inRange(createdAt, recentRange)) {
                recentLikeCount++;
            }
        }

        void incrementComments(LocalDateTime createdAt, DateRange recentRange) {
            totalCommentCount++;
            if (inRange(createdAt, recentRange)) {
                recentCommentCount++;
            }
        }

        void incrementShares(LocalDateTime createdAt, DateRange recentRange) {
            totalShareCount++;
            if (inRange(createdAt, recentRange)) {
                recentShareCount++;
            }
        }

        void incrementSaves(LocalDateTime createdAt, DateRange recentRange) {
            totalSaveCount++;
            if (inRange(createdAt, recentRange)) {
                recentSaveCount++;
            }
        }

        ReviewerScoreCard toScoreCard() {
            return new ReviewerScoreCard(
                    reviewer,
                    badge,
                    reviewCount,
                    recentReviewCount,
                    defaultInt(reviewer.getUser().getUserFollower()),
                    totalLikeCount,
                    totalCommentCount,
                    totalShareCount,
                    totalSaveCount,
                    recentLikeCount,
                    recentCommentCount,
                    recentShareCount,
                    recentSaveCount,
                    activeMonths.size());
        }
    }

    private static class EngagementAccumulator {
        private final Reviewer reviewer;
        private final int likeWeight;
        private final int shareWeight;
        private final int commentWeight;
        private long likeCount;
        private long shareCount;
        private long commentCount;

        EngagementAccumulator(Reviewer reviewer, int likeWeight, int shareWeight, int commentWeight) {
            this.reviewer = reviewer;
            this.likeWeight = likeWeight;
            this.shareWeight = shareWeight;
            this.commentWeight = commentWeight;
        }

        Reviewer reviewer() {
            return reviewer;
        }

        long likeCount() {
            return likeCount;
        }

        long shareCount() {
            return shareCount;
        }

        long commentCount() {
            return commentCount;
        }

        long score() {
            return likeCount * likeWeight + shareCount * shareWeight + commentCount * commentWeight;
        }

        void incrementLikes() {
            likeCount++;
        }

        void incrementShares() {
            shareCount++;
        }

        void incrementComments() {
            commentCount++;
        }
    }
}
