package com.cafestory.service.serviceImplement;

import com.cafestory.config.CacheConfig;
import com.cafestory.dto.responseDTO.FollowTargetResponseDTO;
import com.cafestory.dto.responseDTO.UserFollowResponseDTO;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.PageFollow;
import com.cafestory.entity.Region;
import com.cafestory.entity.User;
import com.cafestory.entity.UserFollow;
import com.cafestory.entity.enums.FollowTargetFilter;
import com.cafestory.entity.enums.FollowTargetType;
import com.cafestory.mapper.UserFollowMapper;
import com.cafestory.repository.PageFollowRepository;
import com.cafestory.repository.UserFollowRepository;
import com.cafestory.service.serviceInterface.UserFollowService;
import com.cafestory.validation.UserValidator;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

@Service
public class UserFollowServiceImpl implements UserFollowService {

    private final UserFollowRepository userFollowRepository;
    private final PageFollowRepository pageFollowRepository;
    private final UserFollowMapper userFollowMapper;
    private final UserValidator userValidator;

    public UserFollowServiceImpl(
            UserFollowRepository userFollowRepository,
            PageFollowRepository pageFollowRepository,
            UserFollowMapper userFollowMapper,
            UserValidator userValidator) {
        this.userFollowRepository = userFollowRepository;
        this.pageFollowRepository = pageFollowRepository;
        this.userFollowMapper = userFollowMapper;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.USER_PROFILE_BY_ID_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.USER_PROFILE_BY_USERNAME_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.RECOMMENDATION_CARDS_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.PERSONALIZED_FEED_RANKING_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.USER_FOLLOW_LIST_CACHE, key = "'followers:' + #p0"),
            @CacheEvict(cacheNames = CacheConfig.USER_FOLLOW_LIST_CACHE, key = "'following:' + #p1"),
            @CacheEvict(cacheNames = CacheConfig.USER_FOLLOW_LIST_CACHE, key = "'following-targets:' + #p1 + ':ALL'"),
            @CacheEvict(cacheNames = CacheConfig.USER_FOLLOW_LIST_CACHE, key = "'following-targets:' + #p1 + ':USER'"),
            @CacheEvict(cacheNames = CacheConfig.USER_FOLLOW_LIST_CACHE, key = "'following-targets:' + #p1 + ':CAFE_PAGE'"),
            @CacheEvict(cacheNames = CacheConfig.USER_FOLLOWING_COUNT_CACHE, key = "#p1")
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
            @CacheEvict(cacheNames = CacheConfig.RECOMMENDATION_CARDS_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.PERSONALIZED_FEED_RANKING_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.USER_FOLLOW_LIST_CACHE, key = "'followers:' + #p0"),
            @CacheEvict(cacheNames = CacheConfig.USER_FOLLOW_LIST_CACHE, key = "'following:' + #p1"),
            @CacheEvict(cacheNames = CacheConfig.USER_FOLLOW_LIST_CACHE, key = "'following-targets:' + #p1 + ':ALL'"),
            @CacheEvict(cacheNames = CacheConfig.USER_FOLLOW_LIST_CACHE, key = "'following-targets:' + #p1 + ':USER'"),
            @CacheEvict(cacheNames = CacheConfig.USER_FOLLOW_LIST_CACHE, key = "'following-targets:' + #p1 + ':CAFE_PAGE'"),
            @CacheEvict(cacheNames = CacheConfig.USER_FOLLOWING_COUNT_CACHE, key = "#p1")
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
    @Cacheable(cacheNames = CacheConfig.USER_FOLLOW_LIST_CACHE, key = "'followers:' + #p0")
    public List<UserFollowResponseDTO> getFollowersByUserId(UUID followingUserId) {
        userValidator.validateUserExists(followingUserId);
        return userFollowRepository.findByFollowingUserId(followingUserId)
                .stream()
                .map(userFollowMapper::toUserFollowResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.USER_FOLLOW_LIST_CACHE, key = "'following:' + #p0")
    public List<UserFollowResponseDTO> getFollowingByUserId(UUID followerUserId) {
        userValidator.validateUserExists(followerUserId);
        return userFollowRepository.findByFollowerUserId(followerUserId)
                .stream()
                .map(userFollowMapper::toUserFollowResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheConfig.USER_FOLLOW_LIST_CACHE,
            key = "'following-targets:' + #p0 + ':' + (#p1 == null ? 'ALL' : #p1.name())")
    public List<FollowTargetResponseDTO> getFollowingTargetsByUserId(UUID followerUserId, FollowTargetFilter targetType) {
        userValidator.validateUserExists(followerUserId);
        FollowTargetFilter safeTargetType = targetType == null ? FollowTargetFilter.ALL : targetType;
        List<FollowTargetResponseDTO> targets = new ArrayList<>();

        if (safeTargetType == FollowTargetFilter.ALL || safeTargetType == FollowTargetFilter.USER) {
            userFollowRepository.findByFollowerUserId(followerUserId)
                    .stream()
                    .map(this::toUserFollowTarget)
                    .forEach(targets::add);
        }

        if (safeTargetType == FollowTargetFilter.ALL || safeTargetType == FollowTargetFilter.CAFE_PAGE) {
            pageFollowRepository.findByUserUserId(followerUserId)
                    .stream()
                    .map(this::toCafePageFollowTarget)
                    .forEach(targets::add);
        }

        return targets.stream()
                .sorted(Comparator
                        .comparing(
                                FollowTargetResponseDTO::getFollowedAt,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(target -> target.getTargetId() == null ? "" : target.getTargetId().toString()))
                .toList();
    }

    private FollowTargetResponseDTO toUserFollowTarget(UserFollow userFollow) {
        User following = userFollow.getFollowing();
        Region region = following == null ? null : following.getRegion();

        FollowTargetResponseDTO response = new FollowTargetResponseDTO();
        response.setFollowId(userFollow.getId());
        response.setTargetType(FollowTargetType.USER);
        response.setTargetId(following == null ? null : following.getUserId());
        response.setUserId(following == null ? null : following.getUserId());
        response.setUsername(following == null ? null : following.getUserName());
        response.setUserFullName(following == null ? null : following.getUserFullName());
        response.setDisplayName(displayUserName(following));
        response.setAvatar(following == null ? null : following.getUserAvatar());
        response.setCity(region == null ? null : region.getCity());
        response.setFollowedAt(userFollow.getCreatedAt());
        return response;
    }

    private FollowTargetResponseDTO toCafePageFollowTarget(PageFollow pageFollow) {
        CafePage cafePage = pageFollow.getCafePage();
        Region region = cafePage == null ? null : cafePage.getRegion();
        User owner = cafePage == null ? null : cafePage.getOwner();

        FollowTargetResponseDTO response = new FollowTargetResponseDTO();
        response.setFollowId(pageFollow.getId());
        response.setTargetType(FollowTargetType.CAFE_PAGE);
        response.setTargetId(cafePage == null ? null : cafePage.getId());
        response.setCafePageId(cafePage == null ? null : cafePage.getId());
        response.setPageName(cafePage == null ? null : cafePage.getName());
        response.setDisplayName(cafePage == null ? null : cafePage.getName());
        response.setUsername(cafePage == null ? null : cafePage.getName());
        response.setAvatar(cafePage == null ? null : cafePage.getAvatarUrl());
        response.setCity(region == null ? null : region.getCity());
        response.setOwnerUserId(owner == null ? null : owner.getUserId());
        response.setPageStatus(cafePage == null ? null : cafePage.getStatus());
        response.setPageActive(cafePage == null ? null : cafePage.getPageActive());
        response.setFollowedAt(pageFollow.getCreatedAt());
        return response;
    }

    private String displayUserName(User user) {
        if (user == null) {
            return null;
        }
        if (user.getUserFullName() != null && !user.getUserFullName().isBlank()) {
            return user.getUserFullName();
        }
        return user.getUserName();
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
