package com.cafestory.service.serviceImplement;

import com.cafestory.config.CacheConfig;
import com.cafestory.repository.UserFollowRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserProfileCacheService {

    private final UserFollowRepository userFollowRepository;

    public UserProfileCacheService(UserFollowRepository userFollowRepository) {
        this.userFollowRepository = userFollowRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.USER_FOLLOWING_COUNT_CACHE, key = "#p0")
    public Integer getFollowingCount(UUID userId) {
        if (userId == null) {
            return 0;
        }

        long followingCount = userFollowRepository.countByFollowerUserId(userId);
        return followingCount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) followingCount;
    }
}
