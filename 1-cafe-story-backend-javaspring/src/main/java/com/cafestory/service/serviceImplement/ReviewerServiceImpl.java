package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.reviewer.ReviewerBadgeResponseDTO;
import com.cafestory.dto.responseDTO.reviewer.ReviewerGeoAnalyticsResponseDTO;
import com.cafestory.dto.responseDTO.reviewer.ReviewerPayoutResponseDTO;
import com.cafestory.dto.responseDTO.reviewer.ReviewerRankingResponseDTO;
import com.cafestory.dto.responseDTO.reviewer.ReviewerResponseDTO;
import com.cafestory.dto.responseDTO.reviewer.ReviewerSegmentResponseDTO;
import com.cafestory.dto.responseDTO.reviewer.ReviewerStatsResponseDTO;
import com.cafestory.entity.BlogLike;
import com.cafestory.entity.BlogShare;
import com.cafestory.entity.Comment;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.ReviewerBadgeHistory;
import com.cafestory.entity.ReviewerPayout;
import com.cafestory.entity.Region;
import com.cafestory.entity.Role;
import com.cafestory.entity.User;
import com.cafestory.entity.UserRoleAssignment;
import com.cafestory.entity.enums.ReviewerBadge;
import com.cafestory.entity.enums.PayoutStatus;
import com.cafestory.repository.BlogLikeRepository;
import com.cafestory.repository.BlogShareRepository;
import com.cafestory.repository.CommentRepository;
import com.cafestory.repository.ReviewerBadgeHistoryRepository;
import com.cafestory.repository.ReviewerPayoutRepository;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.repository.RoleRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.repository.UserRoleAssignmentRepository;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReviewerServiceImpl implements ReviewerService {

    private static final long LIKE_AMOUNT = 100;
    private static final long SHARE_AMOUNT = 300;
    private static final long COMMENT_AMOUNT = 500;
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String REVIEWER_ROLE = "REVIEWER";

    private final BlogLikeRepository blogLikeRepository;
    private final BlogShareRepository blogShareRepository;
    private final CommentRepository commentRepository;
    private final ReviewerPayoutRepository reviewerPayoutRepository;
    private final ReviewerBadgeHistoryRepository reviewerBadgeHistoryRepository;
    private final ReviewerRepository reviewerRepository;
    private final RoleRepository roleRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final UserRepository userRepository;
    private final UserValidator userValidator;

    public ReviewerServiceImpl(
            BlogLikeRepository blogLikeRepository,
            BlogShareRepository blogShareRepository,
            CommentRepository commentRepository,
            ReviewerPayoutRepository reviewerPayoutRepository,
            ReviewerBadgeHistoryRepository reviewerBadgeHistoryRepository,
            ReviewerRepository reviewerRepository,
            RoleRepository roleRepository,
            UserRoleAssignmentRepository userRoleAssignmentRepository,
            UserRepository userRepository,
            UserValidator userValidator) {
        this.blogLikeRepository = blogLikeRepository;
        this.blogShareRepository = blogShareRepository;
        this.commentRepository = commentRepository;
        this.reviewerPayoutRepository = reviewerPayoutRepository;
        this.reviewerBadgeHistoryRepository = reviewerBadgeHistoryRepository;
        this.reviewerRepository = reviewerRepository;
        this.roleRepository = roleRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.userRepository = userRepository;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    public ReviewerResponseDTO createReviewer(UUID userId) {
        User user = userValidator.validateUserExists(userId);
        assignRole(user, REVIEWER_ROLE);
        Reviewer reviewer = reviewerRepository.findByUserUserId(userId).orElseGet(Reviewer::new);
        reviewer.setUser(user);
        return toReviewerResponse(reviewerRepository.save(reviewer));
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
    public long calculateReviewerScore(ReviewerStatsResponseDTO stats) {
        return calculateScore(stats.getLikeCount(), stats.getShareCount(), stats.getCommentCount());
    }

    @Override
    public long calculateReviewerPayout(ReviewerStatsResponseDTO stats) {
        return stats.getLikeCount() * LIKE_AMOUNT
                + stats.getShareCount() * SHARE_AMOUNT
                + stats.getCommentCount() * COMMENT_AMOUNT;
    }

    @Override
    public String calculateReviewerBadge(long score) {
        return badgeForScore(score).name();
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
            payout.setLikeAmount(accumulator.likeCount() * LIKE_AMOUNT);
            payout.setShareAmount(accumulator.shareCount() * SHARE_AMOUNT);
            payout.setCommentAmount(accumulator.commentCount() * COMMENT_AMOUNT);
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
            badgeHistory.setBadge(badgeForScore(accumulator.score()));
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
        Map<UUID, Reviewer> reviewersByUserId = new HashMap<>();
        Map<UUID, EngagementAccumulator> engagement = new HashMap<>();
        for (Reviewer reviewer : reviewerRepository.findAll()) {
            reviewersByUserId.put(reviewer.getUser().getUserId(), reviewer);
            engagement.putIfAbsent(reviewer.getReviewerId(), new EngagementAccumulator(reviewer));
        }
        aggregateEngagement(startDate, endDate, reviewersByUserId, engagement);
        return engagement;
    }

    private void aggregateEngagement(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Map<UUID, Reviewer> reviewersByUserId,
            Map<UUID, EngagementAccumulator> engagement) {
        for (BlogLike like : blogLikeRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(startDate, endDate)) {
            accumulator(engagement, reviewersByUserId.get(like.getUser().getUserId())).incrementLikes();
        }
        for (BlogShare share : blogShareRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(startDate, endDate)) {
            accumulator(engagement, reviewersByUserId.get(share.getUser().getUserId())).incrementShares();
        }
        for (Comment comment : commentRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(startDate, endDate)) {
            accumulator(engagement, reviewersByUserId.get(comment.getUser().getUserId())).incrementComments();
        }
    }

    private EngagementAccumulator accumulator(Map<UUID, EngagementAccumulator> engagement, Reviewer reviewer) {
        if (reviewer == null) {
            return new EngagementAccumulator(null);
        }
        return engagement.computeIfAbsent(reviewer.getReviewerId(), ignored -> new EngagementAccumulator(reviewer));
    }

    private ReviewerRankingResponseDTO toRankingResponse(EngagementAccumulator accumulator) {
        ReviewerRankingResponseDTO response = new ReviewerRankingResponseDTO();
        response.setReviewerId(accumulator.reviewer().getReviewerId());
        response.setLikeCount(accumulator.likeCount());
        response.setShareCount(accumulator.shareCount());
        response.setCommentCount(accumulator.commentCount());
        response.setScore(accumulator.score());
        response.setBadge(badgeForScore(accumulator.score()));
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

    private ReviewerResponseDTO toReviewerResponse(Reviewer reviewer) {
        ReviewerResponseDTO response = new ReviewerResponseDTO();
        response.setReviewerId(reviewer.getReviewerId());
        response.setUserId(reviewer.getUser().getUserId());
        response.setRole(REVIEWER_ROLE);
        return response;
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

    private ReviewerBadge badgeForScore(long score) {
        if (score < 100) {
            return ReviewerBadge.IRON;
        }
        if (score < 300) {
            return ReviewerBadge.BRONZE;
        }
        if (score < 700) {
            return ReviewerBadge.SILVER;
        }
        if (score < 1500) {
            return ReviewerBadge.GOLD;
        }
        return ReviewerBadge.DIAMOND;
    }

    private long calculateScore(long likeCount, long shareCount, long commentCount) {
        return likeCount + shareCount * 3 + commentCount * 5;
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

    private static class EngagementAccumulator {
        private final Reviewer reviewer;
        private long likeCount;
        private long shareCount;
        private long commentCount;

        EngagementAccumulator(Reviewer reviewer) {
            this.reviewer = reviewer;
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
            return likeCount + shareCount * 3 + commentCount * 5;
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
