package com.cafestory.service.serviceImplement;

import com.cafestory.entity.AiModerationResult;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.service.serviceInterface.BlogModerationTagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class BlogModerationTagServiceImpl implements BlogModerationTagService {

    private final AiModerationResultRepository moderationResultRepository;

    public BlogModerationTagServiceImpl(AiModerationResultRepository moderationResultRepository) {
        this.moderationResultRepository = moderationResultRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, List<String>> getTagsByBlogIds(Collection<UUID> blogIds) {
        List<UUID> safeBlogIds = blogIds == null
                ? List.of()
                : blogIds.stream().filter(Objects::nonNull).distinct().toList();
        if (safeBlogIds.isEmpty()) {
            return Map.of();
        }

        // Query đã order by blogId, createdAt desc nên row đầu tiên của mỗi blog là lần moderation
        // mới nhất; putIfAbsent giữ đúng row đó và bỏ qua các lần moderate cũ hơn.
        Map<UUID, List<String>> tagsByBlogId = new HashMap<>();
        for (AiModerationResult result : moderationResultRepository.findWithTagsByBlogIds(safeBlogIds)) {
            UUID blogId = result.getBlog() == null ? null : result.getBlog().getId();
            if (blogId == null) {
                continue;
            }
            tagsByBlogId.putIfAbsent(blogId, normalizeTags(result.getTags()));
        }
        tagsByBlogId.values().removeIf(List::isEmpty);
        return Map.copyOf(tagsByBlogId);
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        Set<String> uniqueTags = new LinkedHashSet<>();
        for (String tag : tags) {
            if (tag != null && !tag.isBlank()) {
                uniqueTags.add(tag.trim());
            }
        }
        return List.copyOf(uniqueTags);
    }
}
