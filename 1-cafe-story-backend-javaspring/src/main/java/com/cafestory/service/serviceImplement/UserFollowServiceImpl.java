package com.cafestory.service.serviceImplement;

import com.cafestory.config.CacheConfig;
import com.cafestory.dto.responseDTO.UserFollowResponseDTO;
import com.cafestory.entity.User;
import com.cafestory.entity.UserFollow;
import com.cafestory.mapper.UserFollowMapper;
import com.cafestory.repository.UserFollowRepository;
import com.cafestory.service.serviceInterface.UserFollowService;
import com.cafestory.validation.UserValidator;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class UserFollowServiceImpl implements UserFollowService {

    private final UserFollowRepository userFollowRepository;
    private final UserFollowMapper userFollowMapper;
    private final UserValidator userValidator;

    public UserFollowServiceImpl(
            UserFollowRepository userFollowRepository,
            UserFollowMapper userFollowMapper,
            UserValidator userValidator) {
        this.userFollowRepository = userFollowRepository;
        this.userFollowMapper = userFollowMapper;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.USER_PROFILE_BY_ID_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.USER_PROFILE_BY_USERNAME_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.ORGANIC_FEED_CACHE, allEntries = true)
    })
    public UserFollowResponseDTO followUser(UUID followingUserId, UUID followerUserId) {
        validateNotSelfFollow(followingUserId, followerUserId);
        User followingUser = userValidator.validateUserExists(followingUserId);
        User followerUser = userValidator.validateUserExists(followerUserId);
        userValidator.validateUserActive(followingUser);
        userValidator.validateUserActive(followerUser);

        if (userFollowRepository.existsByFollowerUserIdAndFollowingUserId(followerUserId, followingUserId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already followed");
        }

        UserFollow userFollow = new UserFollow();
        userFollow.setFollower(followerUser);
        userFollow.setFollowing(followingUser);

        UserFollow savedUserFollow = userFollowRepository.save(userFollow);
        incrementFollowerCount(followingUser);
        return userFollowMapper.toUserFollowResponseDTO(savedUserFollow);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.USER_PROFILE_BY_ID_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.USER_PROFILE_BY_USERNAME_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.ORGANIC_FEED_CACHE, allEntries = true)
    })
    public void unfollowUser(UUID followingUserId, UUID followerUserId) {
        validateNotSelfFollow(followingUserId, followerUserId);
        User followingUser = userValidator.validateUserExists(followingUserId);
        User followerUser = userValidator.validateUserExists(followerUserId);
        userValidator.validateUserActive(followingUser);
        userValidator.validateUserActive(followerUser);

        UserFollow userFollow = userFollowRepository
                .findByFollowerUserIdAndFollowingUserId(followerUserId, followingUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User follow not found"));

        userFollowRepository.delete(userFollow);
        decrementFollowerCount(userFollow.getFollowing());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserFollowResponseDTO> getFollowersByUserId(UUID followingUserId) {
        userValidator.validateUserExists(followingUserId);
        return userFollowRepository.findByFollowingUserId(followingUserId)
                .stream()
                .map(userFollowMapper::toUserFollowResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserFollowResponseDTO> getFollowingByUserId(UUID followerUserId) {
        userValidator.validateUserExists(followerUserId);
        return userFollowRepository.findByFollowerUserId(followerUserId)
                .stream()
                .map(userFollowMapper::toUserFollowResponseDTO)
                .toList();
    }

    private void validateNotSelfFollow(UUID followingUserId, UUID followerUserId) {
        if (followingUserId != null && followingUserId.equals(followerUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User cannot follow themself");
        }
    }

    private void incrementFollowerCount(User user) {
        int currentCount = user.getUserFollower() == null ? 0 : user.getUserFollower();
        user.setUserFollower(currentCount + 1);
    }

    private void decrementFollowerCount(User user) {
        if (user == null) {
            return;
        }
        int currentCount = user.getUserFollower() == null ? 0 : user.getUserFollower();
        user.setUserFollower(Math.max(0, currentCount - 1));
    }
}
