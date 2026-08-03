package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.RagSnapshotItemResponseDTO;
import com.cafestory.dto.responseDTO.RagSnapshotResponseDTO;
import com.cafestory.entity.AiModerationResult;
import com.cafestory.entity.Blog;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Region;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.ReviewerBadgeHistory;
import com.cafestory.entity.ReviewerFormula;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ReviewerBadge;
import com.cafestory.entity.ReviewerBadgeThreshold;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.ReviewerBadgeHistoryRepository;
import com.cafestory.repository.ReviewerBadgeThresholdRepository;
import com.cafestory.repository.ReviewerFormulaRepository;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.service.serviceInterface.RagSnapshotService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RagSnapshotServiceImpl implements RagSnapshotService {

    public static final String SOURCE_TYPE_BLOG = "blog";
    public static final String SOURCE_TYPE_CAFE_PAGE = "cafe_page";
    public static final String SOURCE_TYPE_REVIEWER = "reviewer";

    private static final int MAX_LIMIT = 500;
    private static final int DEFAULT_LIMIT = 200;

    private final BlogRepository blogRepository;
    private final CafePageRepository cafePageRepository;
    private final ReviewerRepository reviewerRepository;
    private final AiModerationResultRepository aiModerationResultRepository;
    private final ReviewerBadgeHistoryRepository reviewerBadgeHistoryRepository;
    private final ReviewerFormulaRepository reviewerFormulaRepository;
    private final ReviewerBadgeThresholdRepository reviewerBadgeThresholdRepository;

    public RagSnapshotServiceImpl(
            BlogRepository blogRepository,
            CafePageRepository cafePageRepository,
            ReviewerRepository reviewerRepository,
            AiModerationResultRepository aiModerationResultRepository,
            ReviewerBadgeHistoryRepository reviewerBadgeHistoryRepository,
            ReviewerFormulaRepository reviewerFormulaRepository,
            ReviewerBadgeThresholdRepository reviewerBadgeThresholdRepository) {
        this.blogRepository = blogRepository;
        this.cafePageRepository = cafePageRepository;
        this.reviewerRepository = reviewerRepository;
        this.aiModerationResultRepository = aiModerationResultRepository;
        this.reviewerBadgeHistoryRepository = reviewerBadgeHistoryRepository;
        this.reviewerFormulaRepository = reviewerFormulaRepository;
        this.reviewerBadgeThresholdRepository = reviewerBadgeThresholdRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public RagSnapshotResponseDTO getSnapshot(String sourceType, LocalDateTime since, String cursorId, int limit) {
        int normalizedLimit = normalizeLimit(limit);
        LocalDateTime normalizedSince = since == null ? LocalDateTime.of(1970, 1, 1, 0, 0) : since;
        UUID normalizedCursorId = normalizeCursorId(cursorId);
        Pageable pageable = PageRequest.of(0, normalizedLimit + 1);

        return switch (sourceType) {
            case SOURCE_TYPE_BLOG -> blogSnapshot(normalizedSince, normalizedCursorId, normalizedLimit, pageable);
            case SOURCE_TYPE_CAFE_PAGE -> cafePageSnapshot(normalizedSince, normalizedCursorId, normalizedLimit, pageable);
            case SOURCE_TYPE_REVIEWER -> reviewerSnapshot(normalizedSince, normalizedCursorId, normalizedLimit, pageable);
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Unsupported sourceType: " + sourceType);
        };
    }

    private RagSnapshotResponseDTO blogSnapshot(LocalDateTime since, UUID cursorId, int limit, Pageable pageable) {
        List<Blog> blogs = blogRepository.findRagSnapshotBlogs(since, cursorId, pageable);
        Map<UUID, List<String>> tagsByBlogId = loadTagsByBlogId(blogs);

        List<RagSnapshotItemResponseDTO> items = blogs.stream()
                .map(blog -> new RagSnapshotItemResponseDTO(
                        SOURCE_TYPE_BLOG,
                        blog.getId().toString(),
                        effectiveTimestamp(blog.getUpdatedAt(), blog.getCreatedAt()),
                        blogData(blog, tagsByBlogId.get(blog.getId()))))
                .toList();

        List<String> tombstones = blogRepository.findRagTombstoneBlogIds(since).stream()
                .map(UUID::toString)
                .toList();

        return buildResponse(items, tombstones, limit);
    }

    private RagSnapshotResponseDTO cafePageSnapshot(LocalDateTime since, UUID cursorId, int limit, Pageable pageable) {
        List<CafePage> pages = cafePageRepository.findRagSnapshotCafePages(since, cursorId, pageable);

        List<RagSnapshotItemResponseDTO> items = pages.stream()
                .map(page -> new RagSnapshotItemResponseDTO(
                        SOURCE_TYPE_CAFE_PAGE,
                        page.getId().toString(),
                        effectiveTimestamp(page.getUpdatedAt(), page.getCreatedAt()),
                        cafePageData(page)))
                .toList();

        List<String> tombstones = cafePageRepository.findRagTombstoneCafePageIds(since).stream()
                .map(UUID::toString)
                .toList();

        return buildResponse(items, tombstones, limit);
    }

    private RagSnapshotResponseDTO reviewerSnapshot(LocalDateTime since, UUID cursorId, int limit, Pageable pageable) {
        List<Reviewer> reviewers = reviewerRepository.findRagSnapshotReviewers(since, cursorId, pageable);

        // Batch fetch badge history cho toàn bộ reviewers trong page (chống N+1: cũ = 2*N query,
        // mới = 1 query + group in-memory). Sort theo reviewerId, month DESC nên list mỗi reviewer
        // giữ đúng thứ tự "latest first" như findTopByReviewerReviewerIdOrderByMonthDesc trước đây.
        List<UUID> reviewerIds = reviewers.stream().map(Reviewer::getReviewerId).toList();
        Map<UUID, List<ReviewerBadgeHistory>> historyByReviewerId = reviewerIds.isEmpty()
                ? Map.of()
                : reviewerBadgeHistoryRepository
                        .findByReviewerReviewerIdInOrderByReviewerReviewerIdAscMonthDesc(reviewerIds)
                        .stream()
                        .collect(Collectors.groupingBy(h -> h.getReviewer().getReviewerId()));

        // Active formula: cũ gọi 1 lần/reviewer, giờ hoist ra ngoài loop.
        ReviewerFormula activeFormula = reviewerFormulaRepository.findByActiveTrue().orElse(null);

        List<RagSnapshotItemResponseDTO> items = reviewers.stream()
                .map(reviewer -> new RagSnapshotItemResponseDTO(
                        SOURCE_TYPE_REVIEWER,
                        reviewer.getReviewerId().toString(),
                        effectiveTimestamp(reviewer.getUpdatedAt(), reviewer.getCreatedAt()),
                        reviewerData(
                                reviewer,
                                historyByReviewerId.getOrDefault(reviewer.getReviewerId(), List.of()),
                                activeFormula)))
                .toList();

        List<String> tombstones = reviewerRepository.findRagTombstoneReviewerIds(since).stream()
                .map(UUID::toString)
                .toList();

        return buildResponse(items, tombstones, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> formulaData() {
        ReviewerFormula active = reviewerFormulaRepository.findByActiveTrue().orElse(null);
        if (active == null) {
            return Map.of("empty", true);
        }

        // LinkedHashMap để giữ thứ tự IRON→DIAMOND khi serialize JSON, giúp text sinh ra
        // trong chunk_formula() ổn định — hash không đổi giữa 2 lần ingest cùng data.
        Map<String, Object> multipliers = new LinkedHashMap<>();
        for (ReviewerBadge badge : ReviewerBadge.values()) {
            multipliers.put(badge.name(), active.getMultiplierForBadge(badge).toPlainString());
        }

        Map<String, Long> thresholds = new LinkedHashMap<>();
        for (ReviewerBadgeThreshold t : reviewerBadgeThresholdRepository
                .findByFormulaIdOrderByMinScoreAsc(active.getId())) {
            thresholds.put(t.getBadge().name(), t.getMinScore());
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("formulaId", active.getId().toString());
        data.put("likeWeight", active.getLikeWeight());
        data.put("commentWeight", active.getCommentWeight());
        data.put("shareWeight", active.getShareWeight());
        data.put("likePayoutAmount", active.getLikePayoutAmount());
        data.put("commentPayoutAmount", active.getCommentPayoutAmount());
        data.put("sharePayoutAmount", active.getSharePayoutAmount());
        data.put("multipliers", multipliers);
        data.put("thresholds", thresholds);
        data.put("updatedAt", active.getUpdatedAt());
        return data;
    }

    private Map<String, Object> blogData(Blog blog, List<String> tags) {
        Map<String, Object> data = new HashMap<>();
        data.put("content", blog.getContent());
        data.put("imageUrls", blog.getImageUrls());
        data.put("pageId", blog.getPageId() == null ? null : blog.getPageId().toString());
        data.put("pageName", blog.getPage() == null ? null : blog.getPage().getName());
        data.put("regionId", blog.getRegionId() == null ? null : blog.getRegionId().toString());
        data.put("authorUserName", blog.getAuthor() == null ? null : blog.getAuthor().getUserName());
        data.put("likeCount", blog.getLikeCount());
        data.put("shareCount", blog.getShareCount());
        data.put("commentCount", blog.getCommentCount());
        data.put("createdAt", blog.getCreatedAt());
        data.put("tags", tags == null ? List.of() : tags);
        return data;
    }

    private Map<String, Object> cafePageData(CafePage page) {
        Region region = page.getRegion();
        Map<String, Object> data = new HashMap<>();
        data.put("name", page.getName());
        data.put("address", page.getAddress());
        data.put("description", page.getDescription());
        data.put("avatarUrl", page.getAvatarUrl());
        data.put("coverUrl", page.getCoverUrl());
        data.put("likeCount", page.getLikeCount());
        data.put("followerCount", page.getFollowerCount());
        data.put("regionId", region == null ? null : region.getRegionId().toString());
        data.put("city", region == null ? null : region.getCity());
        data.put("area", region == null ? null : region.getArea());
        data.put("province", region == null ? null : region.getProvince());
        data.put("createdAt", page.getCreatedAt());
        return data;
    }

    private Map<String, Object> reviewerData(
            Reviewer reviewer, List<ReviewerBadgeHistory> history, ReviewerFormula activeFormula) {
        User user = reviewer.getUser();
        Region region = user == null ? null : user.getRegion();

        Map<String, Object> data = new HashMap<>();
        data.put("userName", user == null ? null : user.getUserName());
        data.put("userFullName", user == null ? null : user.getUserFullName());
        data.put("userAvatar", user == null ? null : user.getUserAvatar());
        data.put("createdAt", reviewer.getCreatedAt());

        // Public region info — cho câu "reviewer nào ở Cần Thơ"
        data.put("regionId", region == null ? null : region.getRegionId().toString());
        data.put("province", region == null ? null : region.getProvince());
        data.put("city", region == null ? null : region.getCity());
        data.put("area", region == null ? null : region.getArea());

        // Public engagement counters (đã hiển thị công khai trên profile)
        data.put("followerCount", user == null ? 0 : user.getUserFollower());
        data.put("likeCount", user == null ? 0 : user.getUserLike());

        // history được caller sort month DESC, phần tử đầu = latest.
        ReviewerBadgeHistory latest = history.isEmpty() ? null : history.get(0);
        if (latest != null) {
            data.put("latestBadge", latest.getBadge().name());
            data.put("latestBadgeMonth", latest.getMonth());
            data.put("latestBadgeScore", latest.getScore());
        }

        Map<String, Long> badgeCounts = new HashMap<>();
        for (ReviewerBadgeHistory h : history) {
            badgeCounts.merge(h.getBadge().name(), 1L, Long::sum);
        }
        data.put("badgeCounts", badgeCounts);
        data.put("badgeTotalMonths", (long) history.size());

        if (activeFormula != null && latest != null) {
            data.put("formulaMultiplier", activeFormula.getMultiplierForBadge(latest.getBadge()));
        }
        return data;
    }

    private Map<UUID, List<String>> loadTagsByBlogId(List<Blog> blogs) {
        if (blogs.isEmpty()) {
            return Map.of();
        }
        List<UUID> blogIds = blogs.stream().map(Blog::getId).toList();
        Map<UUID, List<String>> tagsByBlogId = new HashMap<>();
        for (AiModerationResult result : aiModerationResultRepository.findWithTagsByBlogIds(blogIds)) {
            UUID blogId = result.getBlog().getId();
            tagsByBlogId.putIfAbsent(blogId, result.getTags());
        }
        return tagsByBlogId;
    }

    private RagSnapshotResponseDTO buildResponse(
            List<RagSnapshotItemResponseDTO> items, List<String> tombstones, int limit) {
        boolean hasMore = items.size() > limit;
        List<RagSnapshotItemResponseDTO> pageItems = hasMore ? items.subList(0, limit) : items;
        RagSnapshotItemResponseDTO lastItem = pageItems.isEmpty() ? null : pageItems.get(pageItems.size() - 1);
        LocalDateTime nextSince = lastItem == null ? null : lastItem.getUpdatedAt();
        String nextSourceId = lastItem == null ? null : lastItem.getSourceId();
        return new RagSnapshotResponseDTO(pageItems, tombstones, nextSince, nextSourceId, hasMore);
    }

    private LocalDateTime effectiveTimestamp(LocalDateTime updatedAt, LocalDateTime createdAt) {
        return updatedAt != null ? updatedAt : createdAt;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private UUID normalizeCursorId(String cursorId) {
        if (cursorId == null || cursorId.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(cursorId);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cursorId must be a valid UUID");
        }
    }
}
