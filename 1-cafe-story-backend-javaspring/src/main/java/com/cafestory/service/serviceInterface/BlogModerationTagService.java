package com.cafestory.service.serviceInterface;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface BlogModerationTagService {

    /**
     * Batch load tag do AI blog moderation cấp cho từng blog (chống N+1: 1 query cho cả trang).
     * Mỗi blog chỉ lấy tag của lần moderation mới nhất; blog không có tag sẽ không xuất hiện trong map.
     */
    Map<UUID, List<String>> getTagsByBlogIds(Collection<UUID> blogIds);
}
