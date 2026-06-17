package com.cafestory.service.serviceImplement;

import com.cafestory.config.CacheConfig;
import com.cafestory.dto.responseDTO.PageFollowResponseDTO;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.PageFollow;
import com.cafestory.entity.User;
import com.cafestory.mapper.CafePageInteractionMapper;
import com.cafestory.repository.PageFollowRepository;
import com.cafestory.service.serviceInterface.PageFollowService;
import com.cafestory.validation.CafePageValidator;
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
public class PageFollowServiceImpl implements PageFollowService {

    private final PageFollowRepository pageFollowRepository;
    private final CafePageInteractionMapper cafePageInteractionMapper;
    private final CafePageValidator cafePageValidator;
    private final UserValidator userValidator;

    public PageFollowServiceImpl(
            PageFollowRepository pageFollowRepository,
            CafePageInteractionMapper cafePageInteractionMapper,
            CafePageValidator cafePageValidator,
            UserValidator userValidator) {
        this.pageFollowRepository = pageFollowRepository;
        this.cafePageInteractionMapper = cafePageInteractionMapper;
        this.cafePageValidator = cafePageValidator;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.CAFE_PAGE_DETAIL_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.RECOMMENDATION_CARDS_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.PERSONALIZED_FEED_RANKING_CACHE, allEntries = true)
    })
    public PageFollowResponseDTO followPage(UUID cafePageId, UUID userId) {
        CafePage cafePage = cafePageValidator.validateCafePageExists(cafePageId);
        User user = userValidator.validateUserExists(userId);
        userValidator.validateUserActive(user);

        if (pageFollowRepository.existsByUserUserIdAndCafePageId(userId, cafePageId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cafe page already followed by user");
        }

        PageFollow pageFollow = new PageFollow();
        pageFollow.setCafePage(cafePage);
        pageFollow.setUser(user);

        PageFollow savedPageFollow = pageFollowRepository.save(pageFollow);
        incrementFollowerCount(cafePage);
        return cafePageInteractionMapper.toPageFollowResponseDTO(savedPageFollow);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.CAFE_PAGE_DETAIL_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.RECOMMENDATION_CARDS_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.PERSONALIZED_FEED_RANKING_CACHE, allEntries = true)
    })
    public void unfollowPage(UUID cafePageId, UUID userId) {
        cafePageValidator.validateCafePageExists(cafePageId);
        User user = userValidator.validateUserExists(userId);
        userValidator.validateUserActive(user);

        PageFollow pageFollow = pageFollowRepository.findByUserUserIdAndCafePageId(userId, cafePageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cafe page follow not found"));

        pageFollowRepository.delete(pageFollow);
        decrementFollowerCount(pageFollow.getCafePage());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageFollowResponseDTO> getFollowersByCafePageId(UUID cafePageId) {
        cafePageValidator.validateCafePageExists(cafePageId);
        return pageFollowRepository.findByCafePageId(cafePageId)
                .stream()
                .map(cafePageInteractionMapper::toPageFollowResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageFollowResponseDTO> getFollowedPagesByUserId(UUID userId) {
        userValidator.validateUserExists(userId);
        return pageFollowRepository.findByUserUserId(userId)
                .stream()
                .map(cafePageInteractionMapper::toPageFollowResponseDTO)
                .toList();
    }

    private void incrementFollowerCount(CafePage cafePage) {
        int currentCount = cafePage.getFollowerCount() == null ? 0 : cafePage.getFollowerCount();
        cafePage.setFollowerCount(currentCount + 1);
    }

    private void decrementFollowerCount(CafePage cafePage) {
        int currentCount = cafePage.getFollowerCount() == null ? 0 : cafePage.getFollowerCount();
        cafePage.setFollowerCount(Math.max(0, currentCount - 1));
    }
}
