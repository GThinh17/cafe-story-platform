package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.RagSnapshotItemResponseDTO;
import com.cafestory.dto.responseDTO.RagSnapshotResponseDTO;
import com.cafestory.entity.AiModerationResult;
import com.cafestory.entity.Blog;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Region;
import com.cafestory.entity.Reviewer;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.CafePageRepository;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    public RagSnapshotServiceImpl(
            BlogRepository blogRepository,
            CafePageRepository cafePageRepository,
            ReviewerRepository reviewerRepository,
            AiModerationResultRepository aiModerationResultRepository) {
        this.blogRepository = blogRepository;
        this.cafePageRepository = cafePageRepository;
        this.reviewerRepository = reviewerRepository;
        this.aiModerationResultRepository = aiModerationResultRepository;
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

        List<RagSnapshotItemResponseDTO> items = reviewers.stream()
                .map(reviewer -> new RagSnapshotItemResponseDTO(
                        SOURCE_TYPE_REVIEWER,
                        reviewer.getReviewerId().toString(),
                        effectiveTimestamp(reviewer.getUpdatedAt(), reviewer.getCreatedAt()),
                        reviewerData(reviewer)))
                .toList();

        List<String> tombstones = reviewerRepository.findRagTombstoneReviewerIds(since).stream()
                .map(UUID::toString)
                .toList();

        return buildResponse(items, tombstones, limit);
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

    private Map<String, Object> reviewerData(Reviewer reviewer) {
        Map<String, Object> data = new HashMap<>();
        data.put("userName", reviewer.getUser().getUserName());
        data.put("userFullName", reviewer.getUser().getUserFullName());
        data.put("userAvatar", reviewer.getUser().getUserAvatar());
        data.put("createdAt", reviewer.getCreatedAt());
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
