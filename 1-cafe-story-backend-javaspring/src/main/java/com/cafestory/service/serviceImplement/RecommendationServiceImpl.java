package com.cafestory.service.serviceImplement;

import com.cafestory.config.CacheConfig;
import com.cafestory.dto.responseDTO.RecommendationCardResponseDTO;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Region;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.ReviewerBadgeHistory;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.RecommendationTargetType;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReviewerBadge;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.ContentReportRepository;
import com.cafestory.repository.PageFollowRepository;
import com.cafestory.repository.ReviewerBadgeHistoryRepository;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.repository.UserFollowRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceInterface.RecommendationService;
import com.cafestory.validation.UserValidator;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final int MAX_SIZE = 100;
    private static final List<ReportStatus> ACTIVE_REPORT_STATUSES =
            List.of(ReportStatus.OPEN, ReportStatus.REVIEWING);

    private final UserRepository userRepository;
    private final ReviewerRepository reviewerRepository;
    private final CafePageRepository cafePageRepository;
    private final ContentReportRepository contentReportRepository;
    private final ReviewerBadgeHistoryRepository reviewerBadgeHistoryRepository;
    private final UserFollowRepository userFollowRepository;
    private final PageFollowRepository pageFollowRepository;
    private final UserValidator userValidator;

    public RecommendationServiceImpl(
            UserRepository userRepository,
            ReviewerRepository reviewerRepository,
            CafePageRepository cafePageRepository,
            ContentReportRepository contentReportRepository,
            ReviewerBadgeHistoryRepository reviewerBadgeHistoryRepository,
            UserFollowRepository userFollowRepository,
            PageFollowRepository pageFollowRepository,
            UserValidator userValidator) {
        this.userRepository = userRepository;
        this.reviewerRepository = reviewerRepository;
        this.cafePageRepository = cafePageRepository;
        this.contentReportRepository = contentReportRepository;
        this.reviewerBadgeHistoryRepository = reviewerBadgeHistoryRepository;
        this.userFollowRepository = userFollowRepository;
        this.pageFollowRepository = pageFollowRepository;
        this.userValidator = userValidator;
    }

    /**
     * Gắn badge và trạng thái follow cho một trang thẻ gợi ý.
     *
     * <p>Chạy SAU khi đã cắt trang nên danh sách id truyền vào chỉ cỡ page size,
     * và tốn tối đa 3 query cho cả trang thay vì 2–3 query cho mỗi thẻ — đúng
     * kiểu N+1 khiến các endpoint explore cũ chậm.
     */
    private List<RecommendationCardResponseDTO> enrichCards(
            UUID currentUserId, List<RecommendationCardResponseDTO> cards) {
        if (cards.isEmpty()) {
            return cards;
        }

        List<UUID> reviewerIds = cards.stream()
                .filter(card -> card.getTargetType() == RecommendationTargetType.REVIEWER)
                .map(RecommendationCardResponseDTO::getTargetId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<UUID, ReviewerBadge> badgeByReviewerId = new HashMap<>();
        if (!reviewerIds.isEmpty()) {
            // Kết quả đã sắp theo (reviewerId, month desc) nên bản ghi đầu của mỗi
            // reviewer là tháng mới nhất.
            for (ReviewerBadgeHistory history : reviewerBadgeHistoryRepository
                    .findByReviewerReviewerIdInOrderByReviewerReviewerIdAscMonthDesc(reviewerIds)) {
                if (history.getReviewer() != null) {
                    badgeByReviewerId.putIfAbsent(
                            history.getReviewer().getReviewerId(), history.getBadge());
                }
            }
        }

        List<UUID> followableUserIds = cards.stream()
                .filter(card -> card.getTargetType() != RecommendationTargetType.CAFE_PAGE)
                .map(RecommendationCardResponseDTO::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Set<UUID> followedUserIds = followableUserIds.isEmpty()
                ? Set.of()
                : Set.copyOf(userFollowRepository.findFollowedUserIds(currentUserId, followableUserIds));

        List<UUID> cafePageIds = cards.stream()
                .filter(card -> card.getTargetType() == RecommendationTargetType.CAFE_PAGE)
                .map(RecommendationCardResponseDTO::getTargetId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Set<UUID> followedCafePageIds = cafePageIds.isEmpty()
                ? Set.of()
                : Set.copyOf(pageFollowRepository.findFollowedCafePageIds(currentUserId, cafePageIds));

        for (RecommendationCardResponseDTO card : cards) {
            if (card.getTargetType() == RecommendationTargetType.CAFE_PAGE) {
                card.setFollowing(followedCafePageIds.contains(card.getTargetId()));
            } else {
                card.setFollowing(card.getUserId() != null && followedUserIds.contains(card.getUserId()));
                if (card.getTargetType() == RecommendationTargetType.REVIEWER) {
                    card.setBadge(badgeByReviewerId.get(card.getTargetId()));
                }
            }
        }
        return cards;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.RECOMMENDATION_CARDS_CACHE, key = "'users:' + #p0 + ':' + #p1 + ':' + #p2")
    public List<RecommendationCardResponseDTO> getUserRecommendations(UUID currentUserId, int page, int size) {
        User currentUser = validateCurrentUser(currentUserId);
        List<User> candidates = userRepository.findRecommendationCandidates(
                        currentUserId,
                        regionId(currentUser.getRegion()),
                        normalizedCity(currentUser.getRegion()),
                        candidatePageable(page, size));
        Map<UUID, Long> activeReportCounts = activeUserReportCounts(candidates.stream()
                .map(User::getUserId)
                .toList());
        List<ScoredRecommendation> scored = candidates
                .stream()
                .map(user -> scoreUser(currentUser, user, activeReportCounts.getOrDefault(user.getUserId(), 0L)))
                .sorted(Comparator.comparing(ScoredRecommendation::score).reversed())
                .toList();
        return enrichCards(currentUserId, paginate(scored, page, size).stream()
                .map(ScoredRecommendation::response)
                .toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.RECOMMENDATION_CARDS_CACHE, key = "'reviewers:' + #p0 + ':' + #p1 + ':' + #p2")
    public List<RecommendationCardResponseDTO> getReviewerRecommendations(UUID currentUserId, int page, int size) {
        User currentUser = validateCurrentUser(currentUserId);
        List<Reviewer> candidates = reviewerRepository.findRecommendationCandidates(
                        currentUserId,
                        regionId(currentUser.getRegion()),
                        normalizedCity(currentUser.getRegion()),
                        candidatePageable(page, size));
        Map<UUID, Long> activeReportCounts = activeUserReportCounts(candidates.stream()
                .map(Reviewer::getUser)
                .map(User::getUserId)
                .toList());
        List<ScoredRecommendation> scored = candidates
                .stream()
                .map(reviewer -> scoreReviewer(
                        currentUser,
                        reviewer,
                        activeReportCounts.getOrDefault(reviewer.getUser().getUserId(), 0L)))
                .sorted(Comparator.comparing(ScoredRecommendation::score).reversed())
                .toList();
        return enrichCards(currentUserId, paginate(scored, page, size).stream()
                .map(ScoredRecommendation::response)
                .toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.RECOMMENDATION_CARDS_CACHE, key = "'cafe-pages:' + #p0 + ':' + #p1 + ':' + #p2")
    public List<RecommendationCardResponseDTO> getCafePageRecommendations(UUID currentUserId, int page, int size) {
        User currentUser = validateCurrentUser(currentUserId);
        List<CafePage> candidates = cafePageRepository.findRecommendationCandidates(
                        currentUserId,
                        regionId(currentUser.getRegion()),
                        normalizedCity(currentUser.getRegion()),
                        candidatePageable(page, size));
        Map<UUID, Long> activeReportCounts = activeCafePageReportCounts(candidates.stream()
                .map(CafePage::getId)
                .toList());
        List<ScoredRecommendation> scored = candidates
                .stream()
                .map(cafePage -> scoreCafePage(
                        currentUser,
                        cafePage,
                        activeReportCounts.getOrDefault(cafePage.getId(), 0L)))
                .sorted(Comparator.comparing(ScoredRecommendation::score).reversed())
                .toList();
        return enrichCards(currentUserId, paginate(scored, page, size).stream()
                .map(ScoredRecommendation::response)
                .toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.RECOMMENDATION_CARDS_CACHE, key = "'mixed:' + #p0 + ':' + #p1 + ':' + #p2")
    public List<RecommendationCardResponseDTO> getMixedRecommendations(UUID currentUserId, int page, int size) {
        int normalizedPage = Math.max(0, page);
        int normalizedSize = normalizeSize(size);
        int fetchSize = Math.min(MAX_SIZE, Math.max(normalizedSize * 3, 15));

        List<RecommendationCardResponseDTO> users = getUserRecommendations(currentUserId, 0, fetchSize);
        List<RecommendationCardResponseDTO> reviewers = getReviewerRecommendations(currentUserId, 0, fetchSize);
        List<RecommendationCardResponseDTO> cafePages = getCafePageRecommendations(currentUserId, 0, fetchSize);

        List<RecommendationCardResponseDTO> mixed = interleave(users, reviewers, cafePages);
        int fromIndex = normalizedPage * normalizedSize;
        if (fromIndex >= mixed.size()) {
            return List.of();
        }
        int toIndex = Math.min(fromIndex + normalizedSize, mixed.size());
        return mixed.subList(fromIndex, toIndex);
    }

    private User validateCurrentUser(UUID currentUserId) {
        User currentUser = userValidator.validateUserExists(currentUserId);
        userValidator.validateUserActive(currentUser);
        return currentUser;
    }

    private ScoredRecommendation scoreUser(User currentUser, User candidate, long activeReportCount) {
        double locationScore = locationScore(currentUser.getRegion(), candidate.getRegion(), 40.0, 30.0);
        double popularityScore = Math.min(safe(candidate.getUserFollower()) * 0.5 + safe(candidate.getUserLike()) * 0.2, 30.0);
        double reportPenalty = activeReportCount * 10.0;
        double score = locationScore + popularityScore - reportPenalty;

        RecommendationCardResponseDTO response = baseResponse(
                RecommendationTargetType.USER,
                candidate.getUserId(),
                candidate.getUserId(),
                candidate.getUserAvatar(),
                candidate.getUserName(),
                displayName(candidate),
                city(candidate.getRegion()));
        response.setReason(buildUserReason(locationScore, popularityScore));
        return new ScoredRecommendation(response, score);
    }

    private ScoredRecommendation scoreReviewer(User currentUser, Reviewer reviewer, long activeReportCount) {
        User reviewerUser = reviewer.getUser();
        double locationScore = locationScore(currentUser.getRegion(), reviewerUser.getRegion(), 0.0, 25.0);
        double activeScore = Boolean.TRUE.equals(reviewer.getReviewerActive()) ? 20.0 : 0.0;
        double popularityScore = Math.min(safe(reviewerUser.getUserFollower()) * 0.5 + safe(reviewerUser.getUserLike()) * 0.2, 30.0);
        double reportPenalty = activeReportCount * 10.0;
        double score = locationScore + activeScore + popularityScore - reportPenalty;

        RecommendationCardResponseDTO response = baseResponse(
                RecommendationTargetType.REVIEWER,
                reviewer.getReviewerId(),
                reviewerUser.getUserId(),
                reviewerUser.getUserAvatar(),
                reviewerUser.getUserName(),
                displayName(reviewerUser),
                city(reviewerUser.getRegion()));
        response.setReason(buildReviewerReason(locationScore, popularityScore));
        return new ScoredRecommendation(response, score);
    }

    private ScoredRecommendation scoreCafePage(User currentUser, CafePage cafePage, long activeReportCount) {
        double locationScore = locationScore(currentUser.getRegion(), cafePage.getRegion(), 40.0, 30.0);
        double popularityScore = Math.min(safe(cafePage.getFollowerCount()) * 0.3 + safe(cafePage.getLikeCount()) * 0.2, 30.0);
        double activeScore = Boolean.TRUE.equals(cafePage.getPageActive()) ? 10.0 : 0.0;
        double reportPenalty = activeReportCount * 10.0;
        double score = locationScore + popularityScore + activeScore - reportPenalty;

        RecommendationCardResponseDTO response = baseResponse(
                RecommendationTargetType.CAFE_PAGE,
                cafePage.getId(),
                null,
                cafePage.getAvatarUrl(),
                cafePage.getName(),
                cafePage.getName(),
                city(cafePage.getRegion()));
        response.setReason(buildCafePageReason(locationScore, popularityScore));
        return new ScoredRecommendation(response, score);
    }

    private RecommendationCardResponseDTO baseResponse(
            RecommendationTargetType targetType,
            UUID targetId,
            UUID userId,
            String avatar,
            String username,
            String fullName,
            String city) {
        RecommendationCardResponseDTO response = new RecommendationCardResponseDTO();
        response.setTargetType(targetType);
        response.setTargetId(targetId);
        response.setUserId(userId);
        response.setAvatar(avatar);
        response.setUsername(username);
        response.setFullName(fullName);
        response.setCity(city);
        return response;
    }

    private double locationScore(Region currentRegion, Region candidateRegion, double sameRegionScore, double sameCityScore) {
        if (currentRegion == null || candidateRegion == null) {
            return 0.0;
        }
        if (currentRegion.getRegionId() != null && currentRegion.getRegionId().equals(candidateRegion.getRegionId())) {
            return sameRegionScore;
        }
        if (sameText(currentRegion.getCity(), candidateRegion.getCity())) {
            return sameCityScore;
        }
        return 0.0;
    }

    private Map<UUID, Long> activeUserReportCounts(List<UUID> userIds) {
        return reportCountMap(userIds, ids -> contentReportRepository.countByReportedUserIdsAndStatusIn(
                ids,
                ACTIVE_REPORT_STATUSES));
    }

    private Map<UUID, Long> activeCafePageReportCounts(List<UUID> cafePageIds) {
        return reportCountMap(cafePageIds, ids -> contentReportRepository.countByCafePageIdsAndStatusIn(
                ids,
                ACTIVE_REPORT_STATUSES));
    }

    private Map<UUID, Long> reportCountMap(
            List<UUID> targetIds,
            Function<List<UUID>, List<ContentReportRepository.ReportCountRow>> loader) {
        List<UUID> safeTargetIds = targetIds.stream()
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (safeTargetIds.isEmpty()) {
            return Map.of();
        }
        return loader.apply(safeTargetIds)
                .stream()
                .collect(Collectors.toMap(
                        ContentReportRepository.ReportCountRow::getTargetId,
                        ContentReportRepository.ReportCountRow::getReportCount));
    }

    private String buildUserReason(double locationScore, double popularityScore) {
        String englishReason = locationScore > 0
                ? "In your area"
                : popularityScore >= 10
                ? "Popular in the CafeStory community"
                : "Suggested for your CafeStory circle";
        if (!englishReason.isBlank()) {
            return englishReason;
        }
        if (locationScore > 0) {
            return "Cùng khu vực với bạn";
        }
        if (popularityScore >= 10) {
            return "Được nhiều người quan tâm";
        }
        return "Gợi ý phù hợp với cộng đồng CafeStory";
    }

    private String buildReviewerReason(double locationScore, double popularityScore) {
        String englishReason = locationScore > 0
                ? "Reviewer near you"
                : popularityScore >= 10
                ? "High community engagement"
                : "Active reviewer on CafeStory";
        if (!englishReason.isBlank()) {
            return englishReason;
        }
        if (locationScore > 0) {
            return "Reviewer nổi bật gần bạn";
        }
        if (popularityScore >= 10) {
            return "Được cộng đồng tương tác cao";
        }
        return "Reviewer đang hoạt động trên CafeStory";
    }

    private String buildCafePageReason(double locationScore, double popularityScore) {
        String englishReason = locationScore > 0
                ? "Near your area"
                : popularityScore >= 10
                ? "Followed by many cafe lovers"
                : "Active cafe page on CafeStory";
        if (!englishReason.isBlank()) {
            return englishReason;
        }
        if (locationScore > 0) {
            return "Gần khu vực của bạn";
        }
        if (popularityScore >= 10) {
            return "Được nhiều người theo dõi";
        }
        return "Cafe page đang hoạt động trên CafeStory";
    }

    private List<RecommendationCardResponseDTO> interleave(
            List<RecommendationCardResponseDTO> users,
            List<RecommendationCardResponseDTO> reviewers,
            List<RecommendationCardResponseDTO> cafePages) {
        List<RecommendationCardResponseDTO> result = new ArrayList<>();
        int userIndex = 0;
        int reviewerIndex = 0;
        int pageIndex = 0;
        RecommendationTargetType[] pattern = {
                RecommendationTargetType.CAFE_PAGE,
                RecommendationTargetType.USER,
                RecommendationTargetType.REVIEWER,
                RecommendationTargetType.USER,
                RecommendationTargetType.CAFE_PAGE
        };

        while (userIndex < users.size() || reviewerIndex < reviewers.size() || pageIndex < cafePages.size()) {
            int before = result.size();
            for (RecommendationTargetType targetType : pattern) {
                if (targetType == RecommendationTargetType.CAFE_PAGE && pageIndex < cafePages.size()) {
                    result.add(cafePages.get(pageIndex++));
                } else if (targetType == RecommendationTargetType.USER && userIndex < users.size()) {
                    result.add(users.get(userIndex++));
                } else if (targetType == RecommendationTargetType.REVIEWER && reviewerIndex < reviewers.size()) {
                    result.add(reviewers.get(reviewerIndex++));
                } else {
                    FallbackPick pick = pickFallback(users, userIndex, reviewers, reviewerIndex, cafePages, pageIndex);
                    if (pick == null) {
                        continue;
                    }
                    result.add(pick.item());
                    userIndex += pick.type() == RecommendationTargetType.USER ? 1 : 0;
                    reviewerIndex += pick.type() == RecommendationTargetType.REVIEWER ? 1 : 0;
                    pageIndex += pick.type() == RecommendationTargetType.CAFE_PAGE ? 1 : 0;
                }
            }
            if (result.size() == before) {
                break;
            }
        }
        return result;
    }

    private FallbackPick pickFallback(
            List<RecommendationCardResponseDTO> users,
            int userIndex,
            List<RecommendationCardResponseDTO> reviewers,
            int reviewerIndex,
            List<RecommendationCardResponseDTO> cafePages,
            int pageIndex) {
        if (pageIndex < cafePages.size()) {
            return new FallbackPick(RecommendationTargetType.CAFE_PAGE, cafePages.get(pageIndex));
        }
        if (userIndex < users.size()) {
            return new FallbackPick(RecommendationTargetType.USER, users.get(userIndex));
        }
        if (reviewerIndex < reviewers.size()) {
            return new FallbackPick(RecommendationTargetType.REVIEWER, reviewers.get(reviewerIndex));
        }
        return null;
    }

    private Pageable candidatePageable(int page, int size) {
        int fetchSize = Math.min(MAX_SIZE, Math.max((Math.max(0, page) + 1) * normalizeSize(size) * 3, normalizeSize(size)));
        return PageRequest.of(0, fetchSize);
    }

    private List<ScoredRecommendation> paginate(List<ScoredRecommendation> items, int page, int size) {
        int normalizedPage = Math.max(0, page);
        int normalizedSize = normalizeSize(size);
        int fromIndex = normalizedPage * normalizedSize;
        if (fromIndex >= items.size()) {
            return List.of();
        }
        int toIndex = Math.min(fromIndex + normalizedSize, items.size());
        return items.subList(fromIndex, toIndex);
    }

    private int normalizeSize(int size) {
        return Math.min(Math.max(1, size), MAX_SIZE);
    }

    private boolean sameText(String left, String right) {
        return left != null && right != null && left.trim().equalsIgnoreCase(right.trim());
    }

    private String city(Region region) {
        return region == null ? null : region.getCity();
    }

    private String normalizedCity(Region region) {
        String city = city(region);
        return city == null || city.isBlank() ? null : city.trim().toLowerCase();
    }

    private UUID regionId(Region region) {
        return region == null ? null : region.getRegionId();
    }

    private String displayName(User user) {
        if (user.getUserFullName() == null || user.getUserFullName().isBlank()) {
            return user.getUserName();
        }
        return user.getUserFullName();
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }

    private record ScoredRecommendation(RecommendationCardResponseDTO response, double score) {
    }

    private record FallbackPick(RecommendationTargetType type, RecommendationCardResponseDTO item) {
    }
}
